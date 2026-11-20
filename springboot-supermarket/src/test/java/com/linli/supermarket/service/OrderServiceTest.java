package com.linli.supermarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.CheckoutDTO;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.OrderItem;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.entity.StoreSetting;
import com.linli.supermarket.entity.UserCoupon;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.OrderItemMapper;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.service.impl.OrderServiceImpl;
import com.linli.supermarket.vo.CheckoutPreviewVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** 订单生命周期测试：下单校验、支付 CAS 幂等、状态流转越界、取消回滚。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    private static final Long USER_ID = 1001L;
    private static final Long ADDRESS_ID = 2001L;
    private static final Long ORDER_ID = 3001L;
    private static final Long PRODUCT_ID = 4001L;
    private static final Long USER_COUPON_ID = 5001L;

    @Mock private OrdersMapper ordersMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private CartMapper cartMapper;
    @Mock private ProductService productService;
    @Mock private CouponService couponService;
    @Mock private AddressService addressService;
    @Mock private DeliveryService deliveryService;
    @Mock private StoreService storeService;
    @Mock private PointsService pointsService;

    @InjectMocks private OrderServiceImpl orderService;

    @Test
    @DisplayName("提交订单：前端预期金额与后端试算不一致时拒绝，且不扣库存不落库")
    void createOrder_rejectsExpectedAmountMismatch() {
        stubCheckout();
        CheckoutDTO dto = checkoutDto();
        dto.setExpectedPayAmount(money("24.99")); // 后端算得 25.00

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto));

        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(productService, never()).deductStock(anyLong(), any());
        verify(ordersMapper, never()).insert(any(Orders.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }

    @Test
    @DisplayName("提交订单：金额一致时按后端价格落单、条件扣库存、清理购物车")
    void createOrder_successUsesBackendAmountsAndDeductsStock() {
        stubCheckout();
        when(productService.deductStock(PRODUCT_ID, 2)).thenReturn(true);
        CheckoutDTO dto = checkoutDto();
        dto.setExpectedPayAmount(money("25.00"));

        Orders order = orderService.createOrder(USER_ID, dto);

        assertNotNull(order);
        assertEquals(0, order.getStatus());
        assertEquals(money("20.00"), order.getGoodsAmount());
        assertEquals(money("5.00"), order.getDeliveryFee());
        assertEquals(money("0.00"), order.getDiscountAmount());
        assertEquals(money("25.00"), order.getPayAmount());
        assertEquals(USER_ID, order.getUserId());
        assertEquals("北京市北京市朝阳区幸福小区", order.getAddressDetail());
        verify(productService).deductStock(PRODUCT_ID, 2);
        verify(ordersMapper).insert(any(Orders.class));
        verify(orderItemMapper).insert(any(OrderItem.class));
        verify(cartMapper).delete(any());
    }

    @Test
    @DisplayName("提交订单：库存不足抛出 STOCK_NOT_ENOUGH，且不落订单")
    void createOrder_stockInsufficientRejects() {
        stubCheckout();
        when(productService.deductStock(PRODUCT_ID, 2)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, checkoutDto()));

        assertEquals(ResultCode.STOCK_NOT_ENOUGH.getCode(), ex.getCode());
        verify(ordersMapper, never()).insert(any(Orders.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }

    @Test
    @DisplayName("提交订单：有券时锁券并把券 ID 与优惠金额写入订单")
    void createOrder_locksCouponAndRecordsDiscount() {
        stubCheckout();
        when(productService.deductStock(PRODUCT_ID, 2)).thenReturn(true);
        Coupon coupon = new Coupon();
        coupon.setId(7001L);
        coupon.setName("满20减5");
        coupon.setType(1);
        coupon.setDiscount(money("5.00"));
        coupon.setStatus(1);
        coupon.setValidType(1);
        coupon.setThreshold(money("20.00"));
        when(couponService.validateAndLock(USER_ID, USER_COUPON_ID, money("20.00"))).thenReturn(true);
        UserCoupon held = new UserCoupon();
        held.setId(USER_COUPON_ID);
        held.setCouponId(7001L);
        when(couponService.getUserCoupon(USER_COUPON_ID)).thenReturn(held);
        when(couponService.getCoupon(7001L)).thenReturn(coupon);
        when(couponService.calcDiscount(coupon, money("20.00"))).thenReturn(money("5.00"));

        CheckoutDTO dto = checkoutDto();
        dto.setCouponId(USER_COUPON_ID);
        Orders order = orderService.createOrder(USER_ID, dto);

        verify(couponService).validateAndLock(USER_ID, USER_COUPON_ID, money("20.00"));
        assertEquals(7001L, order.getCouponId());
        assertEquals(USER_COUPON_ID, order.getUserCouponId());
        assertEquals(money("5.00"), order.getDiscountAmount());
        // 20 + 5配送 - 5优惠 = 20
        assertEquals(money("20.00"), order.getPayAmount());
    }

    @Test
    @DisplayName("支付：非待付款状态直接返回原订单，不重复 CAS、不重复核销券")
    void pay_whenAlreadyPaid_isIdempotent() {
        Orders paid = order(1);
        paid.setUserCouponId(USER_COUPON_ID);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(paid);

        Orders result = orderService.pay(ORDER_ID, "WECHAT");

        assertSame(paid, result);
        verify(ordersMapper, never()).payCas(anyLong(), any());
        verify(couponService, never()).consume(anyLong(), anyLong());
    }

    @Test
    @DisplayName("支付：待付款状态 CAS 成功后核销优惠券")
    void pay_whenStatusZero_consumesCouponOnce() {
        Orders unpaid = order(0);
        unpaid.setUserCouponId(USER_COUPON_ID);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(unpaid);
        when(ordersMapper.payCas(ORDER_ID, "WECHAT")).thenReturn(1);

        orderService.pay(ORDER_ID, "WECHAT");

        verify(ordersMapper).payCas(ORDER_ID, "WECHAT");
        verify(couponService).consume(USER_COUPON_ID, ORDER_ID);
    }

    @Test
    @DisplayName("支付：并发下 CAS 失败方不核销券（幂等）")
    void pay_whenCasFails_doesNotConsumeCoupon() {
        Orders unpaid = order(0);
        unpaid.setUserCouponId(USER_COUPON_ID);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(unpaid);
        when(ordersMapper.payCas(ORDER_ID, "WECHAT")).thenReturn(0);

        orderService.pay(ORDER_ID, "WECHAT");

        verify(couponService, never()).consume(anyLong(), anyLong());
    }

    @Test
    @DisplayName("支付：未指定支付方式时默认 WECHAT")
    void pay_defaultsToWechat() {
        Orders unpaid = order(0);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(unpaid);
        when(ordersMapper.payCas(ORDER_ID, "WECHAT")).thenReturn(1);

        orderService.pay(ORDER_ID, null);

        verify(ordersMapper).payCas(ORDER_ID, "WECHAT");
    }

    @Test
    @DisplayName("发货：CAS 失败（非待配送）抛状态异常")
    void deliver_whenNotPendingDelivery_throws() {
        when(ordersMapper.deliverCas(ORDER_ID, 99L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.deliver(ORDER_ID, 99L));

        assertEquals(ResultCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("确认收货：CAS 成功后按实付金额发放积分")
    void finish_successAwardsPoints() {
        Orders delivering = order(2);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(delivering);
        when(ordersMapper.finishCas(ORDER_ID)).thenReturn(1);

        orderService.finish(ORDER_ID);

        verify(ordersMapper).finishCas(ORDER_ID);
        verify(pointsService).award(USER_ID, money("36.87"), ORDER_ID);
    }

    @Test
    @DisplayName("确认收货：CAS 失败不发放积分")
    void finish_whenCasFails_doesNotAwardPoints() {
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(order(0));
        when(ordersMapper.finishCas(ORDER_ID)).thenReturn(0);

        assertThrows(BusinessException.class, () -> orderService.finish(ORDER_ID));

        verify(pointsService, never()).award(anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("取消：配送中订单禁止取消")
    void cancel_whenDelivering_throws() {
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(order(2));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancel(ORDER_ID));

        assertEquals(ResultCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
        verify(couponService, never()).release(anyLong());
        verify(productService, never()).rollbackStock(anyLong(), any());
        verify(ordersMapper, never()).cancelCas(anyLong());
    }

    @Test
    @DisplayName("取消：已取消订单幂等返回，不重复回滚")
    void cancel_whenAlreadyCancelled_isNoOp() {
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(order(4));

        orderService.cancel(ORDER_ID);

        verify(couponService, never()).release(anyLong());
        verify(pointsService, never()).rollback(anyLong(), anyLong());
        verify(productService, never()).rollbackStock(anyLong(), any());
        verify(ordersMapper, never()).cancelCas(anyLong());
    }

    @Test
    @DisplayName("取消：已完成订单回滚积分、释放券、回滚库存")
    void cancel_whenCompleted_rollsBackPointsCouponAndStock() {
        Orders finished = order(3);
        finished.setUserCouponId(USER_COUPON_ID);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(finished);
        when(ordersMapper.cancelCas(ORDER_ID)).thenReturn(1);
        OrderItem item = new OrderItem();
        item.setProductId(PRODUCT_ID);
        item.setQuantity(2);
        when(orderItemMapper.selectList(any())).thenReturn(Collections.singletonList(item));

        orderService.cancel(ORDER_ID);

        verify(pointsService).rollback(USER_ID, ORDER_ID);
        verify(couponService).release(USER_COUPON_ID);
        verify(productService).rollbackStock(PRODUCT_ID, 2);
        verify(ordersMapper).cancelCas(ORDER_ID);
    }

    @Test
    @DisplayName("取消并发守卫：CAS 抢占失败（影响 0 行）时绝不执行任何回滚副作用（QA-003 回归）")
    void cancel_whenCasPreemptionFails_doesNotRollbackAnything() {
        // 模拟并发失败方：读到旧状态 status=3，但 cancelCas 已被另一事务抢先执行 → 0 行
        Orders stale = order(3);
        stale.setUserCouponId(USER_COUPON_ID);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(stale);
        when(ordersMapper.cancelCas(ORDER_ID)).thenReturn(0);

        orderService.cancel(ORDER_ID);

        verify(pointsService, never()).rollback(anyLong(), anyLong());
        verify(couponService, never()).release(anyLong());
        verify(productService, never()).rollbackStock(anyLong(), any());
        verify(orderItemMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("取消并发守卫：已完成订单 CAS 成功才回滚库存（QA-003 回归）")
    void cancel_whenCasPreemptionSucceeds_rollsBackStockOnce() {
        Orders finished = order(3);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(finished);
        when(ordersMapper.cancelCas(ORDER_ID)).thenReturn(1);
        OrderItem item = new OrderItem();
        item.setProductId(PRODUCT_ID);
        item.setQuantity(2);
        when(orderItemMapper.selectList(any())).thenReturn(Collections.singletonList(item));

        orderService.cancel(ORDER_ID);

        verify(ordersMapper).cancelCas(ORDER_ID);
        verify(productService, times(1)).rollbackStock(PRODUCT_ID, 2);
    }

    @Test
    @DisplayName("取消：待付款订单只释放券与库存，不回滚积分")
    void cancel_whenUnpaid_doesNotRollbackPoints() {
        Orders unpaid = order(0);
        unpaid.setUserCouponId(USER_COUPON_ID);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(unpaid);
        when(ordersMapper.cancelCas(ORDER_ID)).thenReturn(1);
        OrderItem item = new OrderItem();
        item.setProductId(PRODUCT_ID);
        item.setQuantity(3);
        when(orderItemMapper.selectList(any())).thenReturn(Collections.singletonList(item));

        orderService.cancel(ORDER_ID);

        verify(pointsService, never()).rollback(anyLong(), anyLong());
        verify(couponService).release(USER_COUPON_ID);
        verify(productService).rollbackStock(PRODUCT_ID, 3);
        verify(ordersMapper).cancelCas(ORDER_ID);
    }

    @Test
    @DisplayName("订单状态文案与枚举一致：0待付款 1待配送 2配送中 3已完成 4已取消")
    void detail_statusTextMatchesEnum() {
        Orders order = order(2);
        when(ordersMapper.selectById(ORDER_ID)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertEquals("配送中", orderService.detail(ORDER_ID).getStatusText());
        order.setStatus(3);
        assertEquals("已完成", orderService.detail(ORDER_ID).getStatusText());
        order.setStatus(4);
        assertEquals("已取消", orderService.detail(ORDER_ID).getStatusText());
        order.setStatus(0);
        assertEquals("待付款", orderService.detail(ORDER_ID).getStatusText());
    }

    // ---------- helpers ----------

    private void stubCheckout() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName("矿泉水");
        product.setPrice(money("10.00"));
        product.setStatus(1);
        lenient().when(productService.getById(PRODUCT_ID)).thenReturn(product);

        Address address = new Address();
        address.setId(ADDRESS_ID);
        address.setReceiver("张女士");
        address.setPhone("13912348765");
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setDistrict("朝阳区");
        address.setDetail("幸福小区");
        lenient().when(addressService.getOwned(USER_ID, ADDRESS_ID)).thenReturn(address);
        lenient().when(deliveryService.deliverable("朝阳区")).thenReturn(true);

        StoreSetting store = new StoreSetting();
        store.setDeliveryFee(money("5.00"));
        store.setFreeDeliveryThreshold(money("59.00"));
        lenient().when(storeService.get()).thenReturn(store);
        lenient().when(couponService.calcDiscount(any(), any())).thenReturn(money("0.00"));
    }

    private static CheckoutDTO checkoutDto() {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setAddressId(ADDRESS_ID);
        CheckoutDTO.CheckoutItem item = new CheckoutDTO.CheckoutItem();
        item.setProductId(PRODUCT_ID);
        item.setQuantity(2);
        dto.setItems(Collections.singletonList(item));
        return dto;
    }

    private static Orders order(int status) {
        Orders order = new Orders();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setStatus(status);
        order.setPayAmount(money("36.87"));
        return order;
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
