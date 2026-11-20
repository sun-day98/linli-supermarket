package com.linli.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linli.supermarket.entity.UserCoupon;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 用户优惠券 Mapper，含状态 CAS 操作。 */
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /** 下单锁券：未使用(0) → 锁定(3)。 */
    @Update("UPDATE user_coupon SET status = 3 WHERE id = #{id} AND status = 0")
    int lock(@Param("id") Long id);

    /** 支付核销：锁定(3) → 已使用(1)。 */
    @Update("UPDATE user_coupon SET status = 1, use_time = NOW(), order_id = #{orderId} "
            + "WHERE id = #{id} AND status = 3")
    int consume(@Param("id") Long id, @Param("orderId") Long orderId);

    /** 取消释放：已使用/锁定 → 未使用(0)。 */
    @Update("UPDATE user_coupon SET status = 0, use_time = NULL, order_id = NULL "
            + "WHERE id = #{id} AND status IN (1, 3)")
    int release(@Param("id") Long id);
}
