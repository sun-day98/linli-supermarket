package com.linli.supermarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * 启动数据初始化测试：空库播种的品类/商品/券/区域/管理员/演示会员数量；重复启动不污染。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataInitTest {

    @Mock private CategoryMapper categoryMapper;
    @Mock private ProductMapper productMapper;
    @Mock private CouponMapper couponMapper;
    @Mock private UserCouponMapper userCouponMapper;
    @Mock private UserMapper userMapper;
    @Mock private AddressMapper addressMapper;
    @Mock private CartMapper cartMapper;
    @Mock private OrdersMapper ordersMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private PointsRecordMapper pointsRecordMapper;
    @Mock private DeliveryAreaMapper deliveryAreaMapper;
    @Mock private DeliverymanMapper deliverymanMapper;
    @Mock private AdminMapper adminMapper;
    @Mock private StoreSettingMapper storeSettingMapper;
    @Mock private ReviewMapper reviewMapper;

    @InjectMocks private DataInitService dataInitService;

    @Test
    @DisplayName("重复启动：各表非空时不再播种，不产生重复数据")
    void seedDemoData_whenTablesNotEmpty_doesNothing() {
        when(categoryMapper.selectCount(null)).thenReturn(4L);
        when(couponMapper.selectCount(null)).thenReturn(3L);
        when(productMapper.selectCount(null)).thenReturn(18L);
        when(deliveryAreaMapper.selectCount(null)).thenReturn(16L);
        when(adminMapper.selectCount(null)).thenReturn(1L);
        when(deliverymanMapper.selectCount(null)).thenReturn(2L);
        when(userMapper.selectCount(any())).thenReturn(1L);

        dataInitService.seedDemoData();

        verify(categoryMapper, never()).insert(any(Category.class));
        verify(productMapper, never()).insert(any(Product.class));
        verify(couponMapper, never()).insert(any(Coupon.class));
        verify(deliveryAreaMapper, never()).insert(any(DeliveryArea.class));
        verify(adminMapper, never()).insert(any(Admin.class));
        verify(deliverymanMapper, never()).insert(any(Deliveryman.class));
        verify(userMapper, never()).insert(any(User.class));
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("空库播种：4 分类 / 18 商品 / 3 券 / 16 区域 / 2 配送员 / 1 管理员 / 1 演示会员")
    void seedDemoData_onEmptyDb_seedsExpectedCounts() {
        stubAllEmpty();

        dataInitService.seedDemoData();

        verify(categoryMapper, times(4)).insert(any(Category.class));
        verify(productMapper, times(18)).insert(any(Product.class));
        verify(couponMapper, times(3)).insert(any(Coupon.class));
        // 4 个城市 + 每城 3 个区县 = 16
        verify(deliveryAreaMapper, times(16)).insert(any(DeliveryArea.class));
        verify(deliverymanMapper, times(2)).insert(any(Deliveryman.class));
        verify(adminMapper, times(1)).insert(any(Admin.class));
        verify(userMapper, times(1)).insert(any(User.class));
        verify(addressMapper, times(1)).insert(any(Address.class));
    }

    @Test
    @DisplayName("券种子：含新人自动发放券、满减券、折扣券")
    void seedCoupons_containsRequiredCouponTypes() {
        stubAllEmpty();

        dataInitService.seedDemoData();

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponMapper, times(3)).insert(captor.capture());
        List<Coupon> coupons = captor.getAllValues();
        assertTrue(coupons.stream().anyMatch(c -> Integer.valueOf(1).equals(c.getNewUserAuto())
                && Integer.valueOf(2).equals(c.getScope())));
        assertTrue(coupons.stream().anyMatch(c -> Integer.valueOf(1).equals(c.getType())
                && new BigDecimal("50.00").compareTo(c.getThreshold()) == 0));
        assertTrue(coupons.stream().anyMatch(c -> Integer.valueOf(2).equals(c.getType())
                && new BigDecimal("0.10").compareTo(c.getDiscount()) == 0));
    }

    @Test
    @DisplayName("商品种子：包含库存紧张样本（stock <= stockAlert）")
    void seedProducts_containsLowStockSamples() {
        stubAllEmpty();

        dataInitService.seedDemoData();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(18)).insert(captor.capture());
        long lowStockCount = captor.getAllValues().stream()
                .filter(p -> p.getStock() != null && p.getStockAlert() != null
                        && p.getStock() <= p.getStockAlert())
                .count();
        assertTrue(lowStockCount >= 2, "应至少有 2 个库存紧张商品，实际 " + lowStockCount);
    }

    @Test
    @DisplayName("管理员种子：admin 密码为 BCrypt 密文，且可校验 admin123")
    void seedAdmin_usesBcryptHash() {
        stubAllEmpty();

        dataInitService.seedDemoData();

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminMapper).insert(captor.capture());
        Admin admin = captor.getValue();
        assertEquals("admin", admin.getUsername());
        assertEquals("ADMIN", admin.getRole());
        assertNotNull(admin.getPassword());
        assertTrue(!admin.getPassword().equals("admin123"));
        assertTrue(PasswordUtil.matches("admin123", admin.getPassword()));
    }

    @Test
    @DisplayName("演示会员：手机号 13912348765、初始积分 128、状态启用")
    void seedDemoMember_hasExpectedPhoneAndPoints() {
        stubAllEmpty();

        dataInitService.seedDemoData();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("13912348765", captor.getValue().getPhone());
        assertEquals(128, captor.getValue().getPoints());
        assertEquals(1, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("演示历史订单：已完成(status=3)、金额 36.87、发放 36 积分、含 2 条评价")
    void seedHistoricalOrder_hasCompletedOrderAndPoints() {
        stubAllEmpty();
        Product cabbage = product(1L, "3.99", "山东大白菜");
        Product blueberry = product(2L, "19.90", "云南高山蓝莓");
        // findProductByName 依次查大白菜、蓝莓；按调用顺序返回对应商品
        when(productMapper.selectList(any())).thenReturn(
                Collections.singletonList(cabbage), Collections.singletonList(blueberry));
        when(couponMapper.selectList(any())).thenReturn(Collections.emptyList());
        StoreSetting store = new StoreSetting();
        store.setDeliveryFee(new BigDecimal("5.00"));
        store.setFreeDeliveryThreshold(new BigDecimal("59.00"));
        when(storeSettingMapper.selectById(1L)).thenReturn(store);

        dataInitService.seedDemoData();

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersMapper).insert(orderCaptor.capture());
        assertEquals(3, orderCaptor.getValue().getStatus());
        assertEquals(new BigDecimal("36.87").setScale(2), orderCaptor.getValue().getPayAmount().setScale(2));
        verify(orderItemMapper, times(2)).insert(any(OrderItem.class));

        ArgumentCaptor<PointsRecord> pointsCaptor = ArgumentCaptor.forClass(PointsRecord.class);
        verify(pointsRecordMapper).insert(pointsCaptor.capture());
        assertEquals(1, pointsCaptor.getValue().getType());
        assertEquals(36, pointsCaptor.getValue().getPoints());

        verify(reviewMapper, times(2)).insert(any(Review.class));
        verify(cartMapper, times(2)).insert(any(Cart.class));
    }

    @Test
    @DisplayName("门店设置：默认行已存在时不重复插入")
    void initSchema_whenStoreSettingExists_doesNothing() {
        when(storeSettingMapper.selectById(1L)).thenReturn(new StoreSetting());

        dataInitService.initSchema();

        verify(storeSettingMapper, never()).insert(any(StoreSetting.class));
    }

    @Test
    @DisplayName("门店设置：缺失时创建配送费 5.00、免配送门槛 59.00 的默认行")
    void initSchema_whenMissing_createsDefault() {
        when(storeSettingMapper.selectById(1L)).thenReturn(null);

        dataInitService.initSchema();

        ArgumentCaptor<StoreSetting> captor = ArgumentCaptor.forClass(StoreSetting.class);
        verify(storeSettingMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(new BigDecimal("5.00").setScale(2), captor.getValue().getDeliveryFee().setScale(2));
        assertEquals(new BigDecimal("59.00").setScale(2),
                captor.getValue().getFreeDeliveryThreshold().setScale(2));
    }

    // ---------- helpers ----------

    private void stubAllEmpty() {
        when(categoryMapper.selectCount(null)).thenReturn(0L);
        when(couponMapper.selectCount(null)).thenReturn(0L);
        when(productMapper.selectCount(null)).thenReturn(0L);
        when(deliveryAreaMapper.selectCount(null)).thenReturn(0L);
        when(adminMapper.selectCount(null)).thenReturn(0L);
        when(deliverymanMapper.selectCount(null)).thenReturn(0L);
        when(userMapper.selectCount(any())).thenReturn(0L);
        // seedProducts() 会读取已插入的分类列表为每个商品分配 categoryId；
        // 空库播种场景下需模拟 seedCategories() 已写入的 4 个分类，否则 get(-1) 越界。
        when(categoryMapper.selectList(any())).thenReturn(Arrays.asList(
                category(1L), category(2L), category(3L), category(4L)));
    }

    private static Category category(long id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }

    private static Product product(long id, String price, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStock(60);
        p.setStockAlert(10);
        p.setStatus(1);
        return p;
    }
}
