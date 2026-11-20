package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.CheckoutDTO;
import com.linli.supermarket.dto.OrderQueryDTO;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.entity.Cart;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.OrderItem;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.entity.StoreSetting;
import com.linli.supermarket.entity.UserCoupon;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.OrderItemMapper;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.service.AddressService;
import com.linli.supermarket.service.CouponService;
import com.linli.supermarket.service.DeliveryService;
import com.linli.supermarket.service.OrderService;
import com.linli.supermarket.service.PointsService;
import com.linli.supermarket.service.ProductService;
import com.linli.supermarket.service.StoreService;
import com.linli.supermarket.util.UserContext;
import com.linli.supermarket.vo.CheckoutPreviewVO;
import com.linli.supermarket.vo.OrderItemVO;
import com.linli.supermarket.vo.OrderVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 订单与履约服务实现。所有一致性操作都收敛在本类的 @Transactional 边界内。 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private PointsService pointsService;

    @Override
    public CheckoutPreviewVO preview(CheckoutDTO dto) {
        Long userId = UserContext.getUserId();
        List<OrderItem> items = buildItems(dto.getItems());
        Address address = addressService.getOwned(userId, dto.getAddressId());
        boolean deliverable = deliveryService.deliverable(address.getDistrict());

        Long userCouponId = resolveCouponId(dto);
        BigDecimal goodsAmount = sumGoodsAmount(items);
        Coupon coupon = couponService.validateForPreview(userId, userCouponId, goodsAmount);
        StoreSetting store = storeService.get();

        CheckoutPreviewVO vo = calcAmount(items, coupon, store);
        vo.setDeliverable(deliverable);
        vo.setDistrict(address.getDistrict());
        if (coupon != null) {
            vo.setUserCouponId(userCouponId);
            vo.setCouponName(coupon.getName());
        }
        List<CheckoutPreviewVO.Item> previewItems = new ArrayList<>();
        for (OrderItem item : items) {
            CheckoutPreviewVO.Item previewItem = new CheckoutPreviewVO.Item();
            previewItem.setProductId(item.getProductId());
            previewItem.setProductName(item.getProductName());
            previewItem.setProductImage(item.getProductImage());
            previewItem.setPrice(item.getPrice());
            previewItem.setQuantity(item.getQuantity());
            previewItem.setTotalAmount(item.getTotalAmount());
            previewItems.add(previewItem);
        }
        vo.setItems(previewItems);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(Long userId, CheckoutDTO dto) {
        // 1. 后端重新读取商品单价与名称，忽略前端传入价格
        List<OrderItem> items = buildItems(dto.getItems());
        BigDecimal goodsAmount = sumGoodsAmount(items);

        // 2. 地址与配送区县校验
        Address address = addressService.getOwned(userId, dto.getAddressId());
        if (!deliveryService.deliverable(address.getDistrict())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该区域暂不支持配送，请更换收货地址");
        }

        // 3. 锁券（校验归属/时效/门槛，状态 0→3）
        Long userCouponId = resolveCouponId(dto);
        Coupon coupon = null;
        if (userCouponId != null && couponService.validateAndLock(userId, userCouponId, goodsAmount)) {
            UserCoupon held = couponService.getUserCoupon(userCouponId);
            coupon = couponService.getCoupon(held == null ? null : held.getCouponId());
        }

        // 4. 读取门店配送配置并复用同一金额算法
        StoreSetting store = storeService.get();
        CheckoutPreviewVO preview = calcAmount(items, coupon, store);

        if (dto.getExpectedPayAmount() != null
                && dto.getExpectedPayAmount().setScale(2, RoundingMode.HALF_UP)
                .compareTo(preview.getPayAmount()) != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "金额已变化，请返回结算页重新确认");
        }

        // 5. 条件扣库存，防超卖；失败抛异常触发事务回滚（含锁券回滚）
        for (OrderItem item : items) {
            if (!productService.deductStock(item.getProductId(), item.getQuantity())) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH,
                        "商品【" + item.getProductName() + "】库存不足");
            }
        }

        // 6. 落订单与订单项
        Orders order = new Orders();
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddressDetail(buildAddressDetail(address));
        order.setGoodsAmount(preview.getGoodsAmount());
        order.setDeliveryFee(preview.getDeliveryFee());
        order.setDiscountAmount(preview.getDiscountAmount());
        order.setPayAmount(preview.getPayAmount());
        order.setCouponId(coupon == null ? null : coupon.getId());
        order.setUserCouponId(userCouponId);
        order.setStatus(0);
        order.setRemark(dto.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        insertOrderWithRetry(order, userId);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            orderItemMapper.insert(item);
        }

        // 7. 清理已下单的购物车商品
        List<Long> productIds = items.stream().map(OrderItem::getProductId).distinct().collect(Collectors.toList());
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId, productIds));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders pay(Long orderId, String payType) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        // CAS 幂等：重复支付直接返回当前订单，不重复核销券
        if (order.getStatus() == null || order.getStatus() != 0) {
            return order;
        }
        String type = StringUtils.hasText(payType) ? payType : "WECHAT";
        if (ordersMapper.payCas(orderId, type) == 0) {
            return ordersMapper.selectById(orderId);
        }
        couponService.consume(order.getUserCouponId(), orderId);
        return ordersMapper.selectById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliver(Long orderId, Long deliverymanId) {
        if (ordersMapper.deliverCas(orderId, deliverymanId) == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "仅待配送订单可以发货");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (ordersMapper.finishCas(orderId) == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "仅配送中订单可以确认收货");
        }
        pointsService.award(order.getUserId(), order.getPayAmount(), orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (order.getStatus() != null && order.getStatus() == 4) {
            return;
        }
        if (order.getStatus() != null && order.getStatus() == 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "配送中的订单无法取消");
        }
        // CAS 抢占：先把订单从非取消态原子流转为取消态，只有抢占成功的请求才执行回滚副作用。
        // 并发重复取消时失败方 cancelCas 影响 0 行并直接返回，避免库存/券/积分被重复回滚（超发）。
        // cancelCas 的 WHERE status <> 4 保证并发下仅一个事务抢到，未抢到者读到新状态后影响 0 行。
        if (ordersMapper.cancelCas(orderId) == 0) {
            return;
        }
        // 已完成订单取消需回滚已发积分
        if (order.getStatus() != null && order.getStatus() == 3) {
            pointsService.rollback(order.getUserId(), orderId);
        }
        // 释放优惠券（锁定或已使用）
        couponService.release(order.getUserCouponId());
        // 逐项回滚库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            productService.rollbackStock(item.getProductId(), item.getQuantity());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OrderVO> pageMine(Long userId, OrderQueryDTO dto) {
        return queryPage(dto, new LambdaQueryWrapper<Orders>().eq(Orders::getUserId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OrderVO> pageAdmin(OrderQueryDTO dto) {
        return queryPage(dto, new LambdaQueryWrapper<>());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderVO detail(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        return toVO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> detailBatch(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Orders> orders = ordersMapper.selectBatchIds(orderIds);
        return assembleVOs(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderVO detailForUser(Long userId, Long orderId) {
        Orders order = requireOwned(userId, orderId);
        return toVO(order);
    }

    @Override
    public void assertOwned(Long userId, Long orderId) {
        requireOwned(userId, orderId);
    }

    private Orders requireOwned(Long userId, Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<Orders> timeoutOrders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, 0)
                .lt(Orders::getCreatedAt, deadline));
        for (Orders order : timeoutOrders) {
            cancel(order.getId());
        }
    }

    /** 金额算法：商品金额 + 配送费（达门槛免）− 券优惠，统一 2 位小数 HALF_UP。 */
    private CheckoutPreviewVO calcAmount(List<OrderItem> items, Coupon coupon, StoreSetting store) {
        BigDecimal goodsAmount = sumGoodsAmount(items);
        BigDecimal discount = couponService.calcDiscount(coupon, goodsAmount);

        BigDecimal threshold = store != null && store.getFreeDeliveryThreshold() != null
                ? store.getFreeDeliveryThreshold().setScale(2, RoundingMode.HALF_UP) : ZERO;
        boolean freeDelivery = threshold.compareTo(BigDecimal.ZERO) > 0 && goodsAmount.compareTo(threshold) >= 0;
        BigDecimal deliveryFee = freeDelivery || store == null || store.getDeliveryFee() == null
                ? ZERO : store.getDeliveryFee().setScale(2, RoundingMode.HALF_UP);

        BigDecimal payAmount = goodsAmount.add(deliveryFee).subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = ZERO;
        }

        CheckoutPreviewVO vo = new CheckoutPreviewVO();
        vo.setGoodsAmount(goodsAmount);
        vo.setDeliveryFee(deliveryFee);
        vo.setDiscountAmount(discount);
        vo.setPayAmount(payAmount);
        vo.setFreeDelivery(freeDelivery);
        return vo;
    }

    private BigDecimal sumGoodsAmount(List<OrderItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(item.getTotalAmount());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private List<OrderItem> buildItems(List<CheckoutDTO.CheckoutItem> source) {
        List<OrderItem> items = new ArrayList<>();
        // 批量预取商品；映射缺失时回退单查，兼容 mock 场景与极端并发下架。
        List<Long> productIds = source.stream()
                .map(CheckoutDTO.CheckoutItem::getProductId)
                .filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productService.getByIds(productIds);
        if (productMap == null) {
            productMap = Collections.emptyMap();
        }
        for (CheckoutDTO.CheckoutItem src : source) {
            if (src.getProductId() == null || src.getQuantity() == null || src.getQuantity() <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "结算商品参数错误");
            }
            Product product = productMap.get(src.getProductId());
            if (product == null) {
                product = productService.getById(src.getProductId());
            }
            if (product == null || product.getStatus() != null && product.getStatus() == 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在或已下架");
            }
            BigDecimal price = product.getPrice().setScale(2, RoundingMode.HALF_UP);
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(price);
            item.setQuantity(src.getQuantity());
            item.setTotalAmount(price.multiply(BigDecimal.valueOf(src.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP));
            items.add(item);
        }
        return items;
    }

    private Long resolveCouponId(CheckoutDTO dto) {
        return dto.getUserCouponId() != null ? dto.getUserCouponId() : dto.getCouponId();
    }

    private String buildAddressDetail(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) {
            sb.append(address.getProvince());
        }
        if (address.getCity() != null) {
            sb.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            sb.append(address.getDistrict());
        }
        if (address.getDetail() != null) {
            sb.append(address.getDetail());
        }
        return sb.toString();
    }

    /**
     * 插入订单，遇到订单号碰撞（唯一键冲突）时重新生成并重试，最多 5 次。
     * 订单号带毫秒级熵，碰撞概率极低；重试是为极端并发下的最后防线。
     */
    private void insertOrderWithRetry(Orders order, Long userId) {
        final int maxAttempts = 5;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            order.setOrderNo(generateOrderNo(userId));
            try {
                ordersMapper.insert(order);
                return;
            } catch (org.springframework.dao.DuplicateKeyException ex) {
                if (attempt == maxAttempts - 1) {
                    throw new BusinessException(ResultCode.INTERNAL_ERROR, "订单号生成失败，请稍后重试");
                }
            }
        }
    }

    private String generateOrderNo(Long userId) {
        int suffix = RANDOM.nextInt(1_000_000);
        return LocalDateTime.now().format(ORDER_NO_FORMAT)
                + String.format("%04d", userId % 10000)
                + String.format("%03d", System.currentTimeMillis() % 1000)
                + String.format("%06d", suffix);
    }

    private PageResult<OrderVO> queryPage(OrderQueryDTO dto, LambdaQueryWrapper<Orders> wrapper) {
        if (dto.getStatus() != null) {
            wrapper.eq(Orders::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(Orders::getOrderNo, keyword)
                    .or().like(Orders::getReceiver, keyword)
                    .or().like(Orders::getPhone, keyword));
        }
        if (StringUtils.hasText(dto.getStartTime())) {
            try {
                wrapper.ge(Orders::getCreatedAt, LocalDateTime.parse(dto.getStartTime(), DATE_TIME_FORMAT));
            } catch (RuntimeException ex) {
                throw new BusinessException(ResultCode.PARAM_ERROR,
                        "起始时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        if (StringUtils.hasText(dto.getEndTime())) {
            try {
                wrapper.le(Orders::getCreatedAt, LocalDateTime.parse(dto.getEndTime(), DATE_TIME_FORMAT));
            } catch (RuntimeException ex) {
                throw new BusinessException(ResultCode.PARAM_ERROR,
                        "结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        wrapper.orderByDesc(Orders::getCreatedAt);
        int current = dto.getCurrent() == null ? 1 : Math.max(1, dto.getCurrent());
        int size = dto.getSize() == null ? 10 : Math.min(100, Math.max(1, dto.getSize()));
        IPage<Orders> page = ordersMapper.selectPage(new Page<>(current, size), wrapper);

        List<OrderVO> vos = assembleVOs(page.getRecords());
        return new PageResult<>(vos, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 批量装配订单 VO：一次 IN 查询取回所有订单项，再按 orderId 分组，消除逐单查询的 N+1。
     */
    private List<OrderVO> assembleVOs(List<Orders> records) {
        Map<Long, List<OrderItem>> itemsByOrder = new HashMap<>();
        if (records != null && !records.isEmpty()) {
            List<Long> orderIds = records.stream().map(Orders::getId).collect(Collectors.toList());
            List<OrderItem> allItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .in(OrderItem::getOrderId, orderIds)
                    .orderByAsc(OrderItem::getId));
            for (OrderItem item : allItems) {
                itemsByOrder.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
            }
        }
        List<OrderVO> vos = new ArrayList<>();
        for (Orders order : records == null ? Collections.<Orders>emptyList() : records) {
            vos.add(toVO(order, itemsByOrder.getOrDefault(order.getId(), Collections.emptyList())));
        }
        return vos;
    }

    private OrderVO toVO(Orders order) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId));
        return toVO(order, items);
    }

    private OrderVO toVO(Orders order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(statusText(order.getStatus()));
        List<OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待配送";
            case 2 -> "配送中";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }
}
