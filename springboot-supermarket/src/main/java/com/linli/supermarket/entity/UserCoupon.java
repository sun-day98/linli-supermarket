package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 用户持有的优惠券。状态：0未使用 1已使用 2已过期 3锁定。 */
@Data
@TableName("user_coupon")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private LocalDateTime getTime;
    private LocalDateTime useTime;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
