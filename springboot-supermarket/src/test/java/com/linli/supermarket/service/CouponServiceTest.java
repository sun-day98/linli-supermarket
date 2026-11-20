package com.linli.supermarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.UserCoupon;
import com.linli.supermarket.mapper.CouponMapper;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.mapper.UserCouponMapper;
import com.linli.supermarket.service.impl.CouponServiceImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** 优惠券测试：抵扣算法、门槛/时效/归属校验、锁定-核销-释放状态机。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponServiceTest {

    private static final Long USER_ID = 1001L;
    private static final Long USER_COUPON_ID = 5001L;
    private static final Long COUPON_ID = 7001L;
    private static final Long ORDER_ID = 3001L;

    @Mock private CouponMapper couponMapper;
    @Mock private UserCouponMapper userCouponMapper;
    @Mock private OrdersMapper ordersMapper;

    @InjectMocks private CouponServiceImpl couponService;

    @Test
    @DisplayName("满减券：固定金额抵扣")
    void calcDiscount_fixedAmount() {
        Coupon coupon = coupon(1, "10.00", "50.00");

        assertEquals(money("10.00"), couponService.calcDiscount(coupon, money("60.00")));
        assertEquals(money("10.00"), couponService.calcDiscount(coupon, money("50.00")));
    }

    @Test
    @DisplayName("折扣券：discount 为抵扣比例，0.10 表示减商品金额 10%，HALF_UP 保留 2 位")
    void calcDiscount_rateHalfUp() {
        Coupon coupon = coupon(2, "0.10", "20.00");

        // 59.90 * 0.10 = 5.99
        assertEquals(money("5.99"), couponService.calcDiscount(coupon, money("59.90")));
        // 33.33 * 0.10 = 3.333 -> 3.33
        assertEquals(money("3.33"), couponService.calcDiscount(coupon, money("33.33")));
    }

    @Test
    @DisplayName("抵扣金额不得超过商品金额")
    void calcDiscount_cappedAtGoodsAmount() {
        Coupon coupon = coupon(1, "100.00", "0.00");

        assertEquals(money("30.00"), couponService.calcDiscount(coupon, money("30.00")));
    }

    @Test
    @DisplayName("空券 / 零商品金额 / 负抵扣均返回 0，不会出现负优惠")
    void calcDiscount_invalidInputsReturnZero() {
        assertEquals(money("0.00"), couponService.calcDiscount(null, money("10.00")));
        Coupon coupon = coupon(1, "10.00", "0.00");
        assertEquals(money("0.00"), couponService.calcDiscount(coupon, money("0.00")));
        assertEquals(money("0.00"), couponService.calcDiscount(coupon, null));
        coupon.setDiscount(money("-5.00"));
        assertEquals(money("0.00"), couponService.calcDiscount(coupon, money("10.00")));
    }

    @Test
    @DisplayName("归属校验：他人券被拒绝")
    void validateForPreview_rejectsOtherUsersCoupon() {
        UserCoupon uc = userCoupon(9999L, 0);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.validateForPreview(USER_ID, USER_COUPON_ID, money("100.00")));

        assertEquals(ResultCode.COUPON_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("不属于"));
    }

    @Test
    @DisplayName("门槛校验：商品金额低于门槛时券不可用")
    void validateForPreview_rejectsBelowThreshold() {
        UserCoupon uc = userCoupon(USER_ID, 0);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);
        Coupon coupon = coupon(1, "10.00", "50.00");
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.validateForPreview(USER_ID, USER_COUPON_ID, money("30.00")));

        assertTrue(ex.getMessage().contains("门槛"));
    }

    @Test
    @DisplayName("时效校验：固定有效期已过期的券被拒绝")
    void validateForPreview_rejectsExpiredCoupon() {
        UserCoupon uc = userCoupon(USER_ID, 0);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);
        Coupon coupon = coupon(1, "10.00", "0.00");
        coupon.setEndTime(LocalDateTime.now().minusDays(1));
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.validateForPreview(USER_ID, USER_COUPON_ID, money("100.00")));

        assertTrue(ex.getMessage().contains("过期"));
    }

    @Test
    @DisplayName("状态校验：已锁定/已使用的券不可再次用于试算")
    void validateForPreview_rejectsLockedOrUsedCoupon() {
        UserCoupon uc = userCoupon(USER_ID, 3);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);
        Coupon coupon = coupon(1, "10.00", "0.00");
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.validateForPreview(USER_ID, USER_COUPON_ID, money("100.00")));

        assertTrue(ex.getMessage().contains("已使用或已锁定"));
    }

    @Test
    @DisplayName("未选券（userCouponId 为 null）返回 null，不触发任何查询")
    void validateForPreview_noCouponReturnsNull() {
        assertEquals(null, couponService.validateForPreview(USER_ID, null, money("100.00")));
        verify(userCouponMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("有效券通过校验并返回券定义")
    void validateForPreview_validCouponReturnsCoupon() {
        UserCoupon uc = userCoupon(USER_ID, 0);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);
        Coupon coupon = coupon(1, "10.00", "50.00");
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon);

        Coupon result = couponService.validateForPreview(USER_ID, USER_COUPON_ID, money("60.00"));

        assertNotNull(result);
        assertEquals(COUPON_ID, result.getId());
    }

    @Test
    @DisplayName("锁券：CAS 失败（并发占用）抛异常")
    void validateAndLock_whenCasFails_throws() {
        UserCoupon uc = userCoupon(USER_ID, 0);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon(1, "10.00", "0.00"));
        when(userCouponMapper.lock(USER_COUPON_ID)).thenReturn(0);

        assertThrows(BusinessException.class,
                () -> couponService.validateAndLock(USER_ID, USER_COUPON_ID, money("100.00")));
    }

    @Test
    @DisplayName("锁券：状态 0→3 成功返回 true")
    void validateAndLock_success() {
        UserCoupon uc = userCoupon(USER_ID, 0);
        when(userCouponMapper.selectById(USER_COUPON_ID)).thenReturn(uc);
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon(1, "10.00", "0.00"));
        when(userCouponMapper.lock(USER_COUPON_ID)).thenReturn(1);

        assertTrue(couponService.validateAndLock(USER_ID, USER_COUPON_ID, money("100.00")));
        verify(userCouponMapper).lock(USER_COUPON_ID);
    }

    @Test
    @DisplayName("领券：新人专享券不允许已有订单的用户领取")
    void claim_newUserOnlyCouponRejectsExistingCustomer() {
        Coupon coupon = coupon(1, "5.00", "30.00");
        coupon.setScope(2); // 新人专享
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(ordersMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(USER_ID, COUPON_ID));

        assertTrue(ex.getMessage().contains("新人"));
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("领券：重复领取被拒绝")
    void claim_rejectsDuplicate() {
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon(1, "5.00", "30.00"));
        when(userCouponMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(USER_ID, COUPON_ID));

        assertTrue(ex.getMessage().contains("已领取"));
    }

    @Test
    @DisplayName("领券：发放量已满（increaseIssued 影响行数 0）被拒绝")
    void claim_rejectsWhenSoldOut() {
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon(1, "5.00", "30.00"));
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(couponMapper.increaseIssued(COUPON_ID)).thenReturn(0);

        assertThrows(BusinessException.class, () -> couponService.claim(USER_ID, COUPON_ID));
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("领券成功：写入未使用状态的用户券记录")
    void claim_success() {
        when(couponMapper.selectById(COUPON_ID)).thenReturn(coupon(1, "5.00", "30.00"));
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(couponMapper.increaseIssued(COUPON_ID)).thenReturn(1);

        couponService.claim(USER_ID, COUPON_ID);

        verify(couponMapper).increaseIssued(COUPON_ID);
        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponMapper).insert(captor.capture());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(COUPON_ID, captor.getValue().getCouponId());
        assertEquals(0, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("核销与释放：null 用户券 ID 为安全空操作")
    void consumeAndRelease_nullIdIsNoOp() {
        couponService.consume(null, ORDER_ID);
        couponService.release(null);

        verify(userCouponMapper, never()).consume(anyLong(), anyLong());
        verify(userCouponMapper, never()).release(anyLong());
    }

    @Test
    @DisplayName("核销与释放：委托 Mapper 的 CAS 更新（3→1 / 1,3→0）")
    void consumeAndRelease_delegateToMapper() {
        when(userCouponMapper.consume(USER_COUPON_ID, ORDER_ID)).thenReturn(1);
        couponService.consume(USER_COUPON_ID, ORDER_ID);
        couponService.release(USER_COUPON_ID);

        verify(userCouponMapper).consume(USER_COUPON_ID, ORDER_ID);
        verify(userCouponMapper).release(USER_COUPON_ID);
    }

    @Test
    @DisplayName("核销失败：Mapper 影响行数为 0 时抛异常，触发支付事务回滚")
    void consume_whenMapperReturnsZero_throws() {
        when(userCouponMapper.consume(USER_COUPON_ID, ORDER_ID)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.consume(USER_COUPON_ID, ORDER_ID));

        assertEquals(ResultCode.COUPON_INVALID.getCode(), ex.getCode());
    }

    // ---------- helpers ----------

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static Coupon coupon(int type, String discount, String threshold) {
        Coupon coupon = new Coupon();
        coupon.setId(COUPON_ID);
        coupon.setType(type);
        coupon.setDiscount(money(discount));
        coupon.setThreshold(money(threshold));
        coupon.setStatus(1);
        coupon.setValidType(1);
        coupon.setScope(1);
        return coupon;
    }

    private static UserCoupon userCoupon(Long userId, int status) {
        UserCoupon uc = new UserCoupon();
        uc.setId(USER_COUPON_ID);
        uc.setUserId(userId);
        uc.setCouponId(COUPON_ID);
        uc.setStatus(status);
        uc.setGetTime(LocalDateTime.now().minusDays(1));
        return uc;
    }
}
