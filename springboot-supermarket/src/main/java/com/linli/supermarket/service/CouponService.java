package com.linli.supermarket.service;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.dto.CouponDTO;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.UserCoupon;
import com.linli.supermarket.vo.CouponVO;
import java.math.BigDecimal;
import java.util.List;

/** 优惠券服务：发放、门槛校验、锁定、核销与释放。 */
public interface CouponService {

    /** 当前用户在指定商品金额下可用的券。 */
    List<CouponVO> available(Long userId, BigDecimal amount);

    /** 我的优惠券（全部状态）。 */
    List<CouponVO> my(Long userId);

    /** 领取券。 */
    void claim(Long userId, Long couponId);

    PageResult<CouponVO> adminPage(Integer current, Integer size);

    Long createAdmin(CouponDTO dto);

    void updateAdmin(Long id, CouponDTO dto);

    void deleteAdmin(Long id);

    /** 校验券归属/时效/门槛并锁定（0→3）；无券时返回 false。 */
    boolean validateAndLock(Long userId, Long userCouponId, BigDecimal amount);

    /** 只校验不锁定，返回券定义；无券时返回 null，非法时抛 COUPON_INVALID。 */
    Coupon validateForPreview(Long userId, Long userCouponId, BigDecimal amount);

    /** 支付核销：3→1。 */
    void consume(Long userCouponId, Long orderId);

    /** 取消释放：1/3→0。 */
    void release(Long userCouponId);

    Coupon getCoupon(Long couponId);

    UserCoupon getUserCoupon(Long userCouponId);

    /** 按券类型计算抵扣额（2 位小数，不超过商品金额）。 */
    BigDecimal calcDiscount(Coupon coupon, BigDecimal goodsAmount);

    /** 新人自动发券（注册时调用）。 */
    void grantNewUserCoupons(Long userId);
}
