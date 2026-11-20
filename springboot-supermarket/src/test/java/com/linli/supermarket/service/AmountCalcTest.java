package com.linli.supermarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linli.supermarket.dto.CheckoutDTO;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.entity.StoreSetting;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.OrderItemMapper;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.service.impl.OrderServiceImpl;
import com.linli.supermarket.util.UserContext;
import com.linli.supermarket.vo.CheckoutPreviewVO;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * 金额试算算法测试：商品金额 + 配送费（达门槛免） − 券优惠，统一 2 位 HALF_UP。
 *
 * <p>calcAmount 为私有方法，此处通过 preview 公开入口验证；preview 与 createOrder 共用该算法。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AmountCalcTest {

    private static final Long USER_ID = 1001L;
    private static final Long ADDRESS_ID = 2001L;

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

    @BeforeEach
    void setUp() {
        UserContext.set(USER_ID, "USER");
        Address address = new Address();
        address.setId(ADDRESS_ID);
        address.setDistrict("朝阳区");
        lenient().when(addressService.getOwned(USER_ID, ADDRESS_ID)).thenReturn(address);
        lenient().when(deliveryService.deliverable("朝阳区")).thenReturn(true);
        lenient().when(couponService.calcDiscount(any(), any())).thenReturn(BigDecimal.ZERO.setScale(2));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("商品金额由后端单价×数量累加，单价 3 位小数按 HALF_UP 取 2 位")
    void preview_usesBackendPriceAndHalfUp() {
        // Arrange
        whenProduct(1L, "3.995", "大白菜");
        whenProduct(2L, "2.00", "矿泉水");
        StoreSetting store = store("5.00", "59.00");
        lenient().when(storeService.get()).thenReturn(store);

        // Act
        CheckoutPreviewVO vo = orderService.preview(dto(item(1L, 3), item(2L, 1)));

        // Assert: 3.995 -> 4.00, 4.00*3=12.00; 2.00*1=2.00 => 14.00; +5.00 配送费 = 19.00
        assertEquals(money("14.00"), vo.getGoodsAmount());
        assertEquals(money("5.00"), vo.getDeliveryFee());
        assertEquals(money("0.00"), vo.getDiscountAmount());
        assertEquals(money("19.00"), vo.getPayAmount());
        assertFalse(vo.getFreeDelivery());
        assertEquals("4.00", vo.getItems().get(0).getPrice().toPlainString());
        assertEquals("12.00", vo.getItems().get(0).getTotalAmount().toPlainString());
        // 证明单价来自后端而非前端传参（CheckoutDTO 根本不含价格字段）
        verify(productService).getById(1L);
        verify(productService).getById(2L);
    }

    @Test
    @DisplayName("商品金额恰好等于免配送门槛时免配送费（>= 边界）")
    void preview_freeDeliveryAtThresholdBoundary() {
        whenProduct(1L, "59.00", "大米");
        when(storeService.get()).thenReturn(store("5.00", "59.00"));

        CheckoutPreviewVO vo = orderService.preview(dto(item(1L, 1)));

        assertTrue(vo.getFreeDelivery());
        assertEquals(money("0.00"), vo.getDeliveryFee());
        assertEquals(money("59.00"), vo.getPayAmount());
    }

    @Test
    @DisplayName("商品金额低于免配送门槛时收取配送费")
    void preview_chargesDeliveryBelowThreshold() {
        whenProduct(1L, "58.99", "大米");
        when(storeService.get()).thenReturn(store("5.00", "59.00"));

        CheckoutPreviewVO vo = orderService.preview(dto(item(1L, 1)));

        assertFalse(vo.getFreeDelivery());
        assertEquals(money("5.00"), vo.getDeliveryFee());
        assertEquals(money("63.99"), vo.getPayAmount());
    }

    @Test
    @DisplayName("券优惠从应付金额中扣减，且全额抵扣时应付金额不低于 0")
    void preview_appliesDiscountAndNeverNegative() {
        whenProduct(1L, "10.00", "抽纸");
        when(storeService.get()).thenReturn(store("0.00", "0.00"));
        Coupon coupon = coupon("满减券", 1, "10.00");
        lenient().when(couponService.validateForPreview(USER_ID, 55L, money("10.00"))).thenReturn(coupon);
        lenient().when(couponService.calcDiscount(coupon, money("10.00"))).thenReturn(money("10.00"));

        CheckoutDTO dto = dto(item(1L, 1));
        dto.setCouponId(55L);
        CheckoutPreviewVO vo = orderService.preview(dto);

        assertEquals(money("10.00"), vo.getGoodsAmount());
        assertEquals(money("10.00"), vo.getDiscountAmount());
        assertEquals(money("0.00"), vo.getPayAmount());
        assertEquals(55L, vo.getUserCouponId());
        assertEquals("满减券", vo.getCouponName());
    }

    @Test
    @DisplayName("配送门槛为 0 时不误判为免配送，仍按门店配送费收取")
    void preview_zeroThresholdDoesNotMeanFreeDelivery() {
        whenProduct(1L, "1.00", "矿泉水");
        when(storeService.get()).thenReturn(store("5.00", "0.00"));

        CheckoutPreviewVO vo = orderService.preview(dto(item(1L, 1)));

        assertFalse(vo.getFreeDelivery());
        assertEquals(money("5.00"), vo.getDeliveryFee());
        assertEquals(money("6.00"), vo.getPayAmount());
    }

    @Test
    @DisplayName("不可配送区县在试算中标记 deliverable=false")
    void preview_marksUndeliverableDistrict() {
        whenProduct(1L, "1.00", "矿泉水");
        when(storeService.get()).thenReturn(store("5.00", "59.00"));
        lenient().when(deliveryService.deliverable("朝阳区")).thenReturn(false);

        CheckoutPreviewVO vo = orderService.preview(dto(item(1L, 1)));

        assertFalse(vo.getDeliverable());
        assertEquals("朝阳区", vo.getDistrict());
    }

    @Test
    @DisplayName("非法结算参数（数量 <= 0）被拒绝，不读取商品")
    void preview_rejectsNonPositiveQuantity() {
        CheckoutDTO dto = dto(item(1L, 0));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.linli.supermarket.common.BusinessException.class,
                () -> orderService.preview(dto));
        verify(productService, org.mockito.Mockito.never()).getById(anyLong());
    }

    // ---------- helpers ----------

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static StoreSetting store(String fee, String threshold) {
        StoreSetting setting = new StoreSetting();
        setting.setDeliveryFee(money(fee));
        setting.setFreeDeliveryThreshold(money(threshold));
        return setting;
    }

    private static Coupon coupon(String name, int type, String discount) {
        Coupon coupon = new Coupon();
        coupon.setName(name);
        coupon.setType(type);
        coupon.setDiscount(money(discount));
        coupon.setStatus(1);
        coupon.setValidType(1);
        coupon.setThreshold(BigDecimal.ZERO);
        return coupon;
    }

    private void whenProduct(long id, String price, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStatus(1);
        org.mockito.Mockito.when(productService.getById(id)).thenReturn(product);
    }

    private static CheckoutDTO.CheckoutItem item(long productId, int quantity) {
        CheckoutDTO.CheckoutItem item = new CheckoutDTO.CheckoutItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private static CheckoutDTO dto(CheckoutDTO.CheckoutItem... items) {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setAddressId(ADDRESS_ID);
        List<CheckoutDTO.CheckoutItem> list = Arrays.asList(items);
        dto.setItems(list);
        return dto;
    }
}
