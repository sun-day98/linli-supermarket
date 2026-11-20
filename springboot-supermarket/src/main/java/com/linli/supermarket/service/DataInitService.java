package com.linli.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.entity.Admin;
import com.linli.supermarket.entity.Cart;
import com.linli.supermarket.entity.Category;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.DeliveryArea;
import com.linli.supermarket.entity.Deliveryman;
import com.linli.supermarket.entity.OrderItem;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.entity.PointsRecord;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.entity.Review;
import com.linli.supermarket.entity.StoreSetting;
import com.linli.supermarket.entity.User;
import com.linli.supermarket.entity.UserCoupon;
import com.linli.supermarket.mapper.AddressMapper;
import com.linli.supermarket.mapper.AdminMapper;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.CategoryMapper;
import com.linli.supermarket.mapper.CouponMapper;
import com.linli.supermarket.mapper.DeliveryAreaMapper;
import com.linli.supermarket.mapper.DeliverymanMapper;
import com.linli.supermarket.mapper.OrderItemMapper;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.mapper.PointsRecordMapper;
import com.linli.supermarket.mapper.ProductMapper;
import com.linli.supermarket.mapper.ReviewMapper;
import com.linli.supermarket.mapper.StoreSettingMapper;
import com.linli.supermarket.mapper.UserCouponMapper;
import com.linli.supermarket.mapper.UserMapper;
import com.linli.supermarket.util.PasswordUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动数据初始化：空库时播种分类、商品、配送区域、门店设置、默认管理员与演示会员。
 */
@Slf4j
@Component
@Order(1)
public class DataInitService implements CommandLineRunner {

    private static final String DEMO_PHONE = "13912348765";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Autowired private CategoryMapper categoryMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private CouponMapper couponMapper;
    @Autowired private UserCouponMapper userCouponMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private CartMapper cartMapper;
    @Autowired private OrdersMapper ordersMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private PointsRecordMapper pointsRecordMapper;
    @Autowired private DeliveryAreaMapper deliveryAreaMapper;
    @Autowired private DeliverymanMapper deliverymanMapper;
    @Autowired private AdminMapper adminMapper;
    @Autowired private StoreSettingMapper storeSettingMapper;
    @Autowired private ReviewMapper reviewMapper;

    /** 商品播种数据：分类序号, 名称, 副标题, 售价, 划线价, 库存, 阈值, 单位, 规格, 热门。 */
    private static final Object[][] PRODUCT_SEED = {
        {0, "山东大白菜", "新鲜脆嫩 · 产地直供", "3.99", "5.99", 60, 10, "斤", "约2斤/颗", 0},
        {0, "云南高山蓝莓", "果肉饱满 · 甜度14+", "19.90", "29.90", 5, 10, "盒", "125g/盒", 1},
        {0, "本地西红柿", "沙瓤多汁 · 自然成熟", "6.50", "8.90", 48, 10, "斤", "约1斤/份", 0},
        {0, "新鲜水果黄瓜", "清脆爽口 · 即食优选", "4.80", "6.50", 8, 10, "斤", "约1斤/份", 0},
        {0, "赣南脐橙", "酸甜多汁 · 富含维C", "12.90", "18.90", 72, 10, "斤", "约3斤/袋", 1},
        {0, "进口香蕉", "软糯香甜 · 无籽品种", "8.80", "11.90", 55, 10, "斤", "约2斤/把", 0},
        {1, "农夫山泉天然水", "饮用天然水", "2.00", "2.50", 120, 20, "瓶", "550ml", 1},
        {1, "纯牛奶全脂", "醇香浓郁 · 早餐伴侣", "3.50", "4.50", 96, 20, "盒", "250ml", 0},
        {1, "乐事薯片原味", "酥脆可口", "9.90", "12.90", 36, 10, "袋", "70g", 1},
        {1, "每日坚果混合装", "科学配比 · 独立包装", "29.90", "39.90", 24, 10, "盒", "750g/30包", 0},
        {2, "东北五常大米", "粒粒饱满 · 香气浓郁", "39.90", "49.90", 32, 10, "袋", "5kg", 1},
        {2, "金龙鱼调和油", "均衡营养 · 家常首选", "69.90", "79.90", 18, 10, "桶", "5L", 0},
        {2, "海天生抽酱油", "鲜味醇厚", "12.80", "15.90", 50, 10, "瓶", "500ml", 0},
        {2, "加碘精制食盐", "细盐易溶", "2.50", "3.00", 110, 20, "袋", "500g", 0},
        {3, "原木纯品抽纸", "柔韧亲肤 · 三层加厚", "19.90", "25.90", 45, 10, "提", "3包装", 1},
        {3, "柠檬洗洁精", "强力去油 · 清新柠檬香", "13.90", "17.90", 34, 10, "瓶", "1.5kg", 0},
        {3, "加厚垃圾袋", "韧性十足 · 不易破损", "9.90", "12.90", 60, 10, "卷", "45只装", 0},
        {3, "深层洁净洗衣液", "低泡易漂 · 持久留香", "29.90", "39.90", 16, 10, "瓶", "3kg", 1}
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {
        initSchema();
        seedDemoData();
        log.info("邻里小超演示数据初始化完成：分类={} 商品={} 配送区域={}",
                categoryMapper.selectCount(null), productMapper.selectCount(null),
                deliveryAreaMapper.selectCount(null));
    }

    /** 兜底确保门店设置默认行存在（schema.sql 已创建）。 */
    public void initSchema() {
        if (storeSettingMapper.selectById(1L) != null) {
            return;
        }
        StoreSetting setting = new StoreSetting();
        setting.setId(1L);
        setting.setStoreName("邻里小超·社区优选");
        setting.setSlogan("新鲜到家，邻里共享");
        setting.setNotice("本店支持当日达，满59元免配送费");
        setting.setDeliveryFee(new BigDecimal("5.00"));
        setting.setFreeDeliveryThreshold(new BigDecimal("59.00"));
        setting.setServicePhone("400-800-1234");
        setting.setLogo("");
        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        storeSettingMapper.insert(setting);
    }

    /** 空表才播种，重复启动不会污染数据。 */
    @Transactional(rollbackFor = Exception.class)
    public void seedDemoData() {
        if (categoryMapper.selectCount(null) == 0) {
            seedCategories();
        }
        if (couponMapper.selectCount(null) == 0) {
            seedCoupons();
        }
        if (productMapper.selectCount(null) == 0) {
            seedProducts();
        }
        if (deliveryAreaMapper.selectCount(null) == 0) {
            seedDeliveryAreas();
        }
        if (adminMapper.selectCount(null) == 0) {
            seedAdmin();
        }
        if (deliverymanMapper.selectCount(null) == 0) {
            seedDeliverymen();
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, DEMO_PHONE)) == 0) {
            seedDemoMember();
        }
    }

    private void seedCategories() {
        String[] names = {"蔬菜水果", "饮料零食", "粮油调味", "日用百货"};
        String[] icons = {
            "/static/category/vegetable.png",
            "/static/category/drink.png",
            "/static/category/grain.png",
            "/static/category/daily.png"
        };
        for (int i = 0; i < names.length; i++) {
            Category category = new Category();
            category.setName(names[i]);
            category.setIcon(icons[i]);
            category.setSort(i + 1);
            category.setStatus(1);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            categoryMapper.insert(category);
        }
    }

    private void seedCoupons() {
        LocalDateTime now = LocalDateTime.now();
        Coupon newcomer = baseCoupon("新人专享券 满30减5", 1, "5.00", "30.00", 2, 2, 1000, 1);
        newcomer.setValidDays(30);
        newcomer.setNewUserAuto(1);
        couponMapper.insert(newcomer);

        Coupon fullReduction = baseCoupon("全场满减券 满50减10", 1, "10.00", "50.00", 1, 1, 500, 0);
        fullReduction.setStartTime(now.minusDays(1));
        fullReduction.setEndTime(now.plusDays(365));
        couponMapper.insert(fullReduction);

        Coupon discount = baseCoupon("全场9折券", 2, "0.10", "20.00", 2, 1, 300, 0);
        discount.setValidDays(60);
        couponMapper.insert(discount);
    }

    private Coupon baseCoupon(String name, Integer type, String discount, String threshold,
                              Integer validType, Integer scope, Integer total, Integer newUserAuto) {
        Coupon coupon = new Coupon();
        coupon.setName(name);
        coupon.setType(type);
        coupon.setDiscount(new BigDecimal(discount));
        coupon.setThreshold(new BigDecimal(threshold));
        coupon.setValidType(validType);
        coupon.setScope(scope);
        coupon.setTotal(total);
        coupon.setIssued(0);
        coupon.setValidDays(30);
        coupon.setNewUserAuto(newUserAuto);
        coupon.setStatus(1);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        return coupon;
    }

    private void seedProducts() {
        java.util.List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        int index = 1;
        for (Object[] row : PRODUCT_SEED) {
            int categoryIndex = (Integer) row[0];
            Long categoryId = categories.get(Math.min(categoryIndex, categories.size() - 1)).getId();
            Product product = new Product();
            product.setCategoryId(categoryId);
            product.setName((String) row[1]);
            product.setSubtitle((String) row[2]);
            product.setCode(String.format("SP%05d", index));
            product.setPrice(new BigDecimal((String) row[3]));
            product.setOriginalPrice(new BigDecimal((String) row[4]));
            product.setStock((Integer) row[5]);
            product.setStockAlert((Integer) row[6]);
            product.setSales(0);
            product.setUnit((String) row[7]);
            product.setSpec((String) row[8]);
            String color = index % 2 == 0 ? "67C23A" : "409EFF";
            String image = "https://placehold.co/400x300/" + color + "/ffffff?text=LINLI" + index;
            product.setImage(image);
            product.setImages("[\"" + image + "\"]");
            product.setDescription((String) row[1] + "，" + row[2] + "。邻里小超严选品质，支持当日配送到家。");
            product.setNutrition("主要成分见商品包装；示例数据仅供演示。");
            product.setSort(index);
            product.setIsHot((Integer) row[9]);
            product.setStatus(1);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.insert(product);
            index++;
        }
    }

    private void seedDeliveryAreas() {
        String[] cityNames = {"北京市", "上海市", "广州市", "深圳市"};
        int[] hotFlags = {1, 1, 0, 0};
        String[][] districtNames = {
            {"朝阳区", "海淀区", "丰台区"},
            {"浦东新区", "徐汇区", "静安区"},
            {"天河区", "越秀区", "海珠区"},
            {"南山区", "福田区", "宝安区"}
        };
        for (int i = 0; i < cityNames.length; i++) {
            DeliveryArea city = new DeliveryArea();
            city.setParentId(0L);
            city.setName(cityNames[i]);
            city.setType(1);
            city.setEnabled(1);
            city.setHot(hotFlags[i]);
            city.setSort(i + 1);
            city.setCreatedAt(LocalDateTime.now());
            city.setUpdatedAt(LocalDateTime.now());
            deliveryAreaMapper.insert(city);
            for (int j = 0; j < districtNames[i].length; j++) {
                DeliveryArea district = new DeliveryArea();
                district.setParentId(city.getId());
                district.setName(districtNames[i][j]);
                district.setType(2);
                district.setEnabled(1);
                district.setHot(0);
                district.setSort(j + 1);
                district.setCreatedAt(LocalDateTime.now());
                district.setUpdatedAt(LocalDateTime.now());
                deliveryAreaMapper.insert(district);
            }
        }
    }

    private void seedAdmin() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.encode("admin123"));
        admin.setRealName("门店管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.insert(admin);
    }

    private void seedDeliverymen() {
        String[] names = {"小李师傅", "王师傅"};
        String[] phones = {"13800001111", "13800002222"};
        for (int i = 0; i < names.length; i++) {
            Deliveryman deliveryman = new Deliveryman();
            deliveryman.setName(names[i]);
            deliveryman.setPhone(phones[i]);
            deliveryman.setStatus(1);
            deliveryman.setCreatedAt(LocalDateTime.now());
            deliveryman.setUpdatedAt(LocalDateTime.now());
            deliverymanMapper.insert(deliveryman);
        }
    }

    private void seedDemoMember() {
        User user = new User();
        user.setPhone(DEMO_PHONE);
        user.setPassword(PasswordUtil.encode("123456"));
        user.setNickname("邻里小超会员");
        user.setAvatar("");
        user.setGender(2);
        user.setPoints(128);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now().minusDays(30));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Address address = new Address();
        address.setUserId(user.getId());
        address.setReceiver("张女士");
        address.setPhone(DEMO_PHONE);
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setDistrict("朝阳区");
        address.setDetail("幸福小区1号楼1单元101");
        address.setIsDefault(1);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(address);

        Product cabbage = findProductByName("山东大白菜");
        Product blueberry = findProductByName("云南高山蓝莓");
        if (cabbage != null) {
            addCart(user.getId(), cabbage.getId(), 2);
        }
        if (blueberry != null) {
            addCart(user.getId(), blueberry.getId(), 1);
        }

        // 演示会员持有两张可用券
        Coupon newcomer = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getNewUserAuto, 1).last("LIMIT 1")).stream().findFirst().orElse(null);
        Coupon fullReduction = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getThreshold, new BigDecimal("50.00")).last("LIMIT 1"))
                .stream().findFirst().orElse(null);
        if (newcomer != null) {
            grantCoupon(user.getId(), newcomer.getId());
        }
        if (fullReduction != null) {
            grantCoupon(user.getId(), fullReduction.getId());
        }

        seedHistoricalOrder(user, address, cabbage, blueberry);
    }

    private void addCart(Long userId, Long productId, Integer quantity) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.insert(cart);
    }

    private void grantCoupon(Long userId, Long couponId) {
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        uc.setGetTime(LocalDateTime.now());
        uc.setCreatedAt(LocalDateTime.now());
        uc.setUpdatedAt(LocalDateTime.now());
        userCouponMapper.insert(uc);
        couponMapper.increaseIssued(couponId);
    }

    private void seedHistoricalOrder(User user, Address address, Product cabbage, Product blueberry) {
        if (cabbage == null || blueberry == null) {
            return;
        }
        LocalDateTime finishedAt = LocalDateTime.now().minusDays(2);
        BigDecimal goodsAmount = cabbage.getPrice().multiply(BigDecimal.valueOf(3))
                .add(blueberry.getPrice()).setScale(2, RoundingMode.HALF_UP);
        StoreSetting store = storeSettingMapper.selectById(1L);
        BigDecimal threshold = store != null && store.getFreeDeliveryThreshold() != null
                ? store.getFreeDeliveryThreshold() : ZERO;
        BigDecimal deliveryFee = goodsAmount.compareTo(threshold) >= 0
                ? ZERO : (store == null || store.getDeliveryFee() == null ? ZERO
                : store.getDeliveryFee().setScale(2, RoundingMode.HALF_UP));
        BigDecimal payAmount = goodsAmount.add(deliveryFee).setScale(2, RoundingMode.HALF_UP);

        Orders order = new Orders();
        order.setOrderNo("DEMO" + finishedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        order.setUserId(user.getId());
        order.setAddressId(address.getId());
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddressDetail("北京市北京市朝阳区幸福小区1号楼1单元101");
        order.setGoodsAmount(goodsAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(ZERO);
        order.setPayAmount(payAmount);
        order.setPayType("WECHAT");
        order.setStatus(3);
        order.setPayTime(finishedAt.plusHours(1));
        order.setDeliveryTime(finishedAt.plusHours(6));
        order.setFinishTime(finishedAt.plusDays(1));
        order.setRemark("演示历史订单");
        order.setCreatedAt(finishedAt);
        order.setUpdatedAt(finishedAt.plusDays(1));
        ordersMapper.insert(order);

        insertOrderItem(order.getId(), cabbage, 3);
        insertOrderItem(order.getId(), blueberry, 1);

        int points = payAmount.setScale(0, RoundingMode.DOWN).intValue();
        PointsRecord record = new PointsRecord();
        record.setUserId(user.getId());
        record.setType(1);
        record.setPoints(points);
        record.setBalance(user.getPoints());
        record.setSource("订单完成奖励");
        record.setOrderId(order.getId());
        record.setCreatedAt(finishedAt.plusDays(1));
        record.setUpdatedAt(finishedAt.plusDays(1));
        pointsRecordMapper.insert(record);

        insertReview(user.getId(), cabbage.getId(), order.getId(), 5, "白菜很新鲜，配送也快！");
        insertReview(user.getId(), blueberry.getId(), order.getId(), 4, "蓝莓个头不错，甜度可以。");
    }

    private void insertOrderItem(Long orderId, Product product, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImage());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP));
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        orderItemMapper.insert(item);
    }

    private void insertReview(Long userId, Long productId, Long orderId, Integer rating, String content) {
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages("[]");
        review.setStatus(1);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    private Product findProductByName(String name) {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getName, name).last("LIMIT 1"))
                .stream().findFirst().orElse(null);
    }
}
