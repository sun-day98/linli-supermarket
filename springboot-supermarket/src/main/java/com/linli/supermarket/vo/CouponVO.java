package com.linli.supermarket.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 优惠券视图，同时承载「券定义」与「用户持有记录」信息。 */
@Data
public class CouponVO {

    private Long id;
    private String name;
    private Integer type;
    private String typeName;
    private BigDecimal discount;
    private BigDecimal threshold;
    private Integer validType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer scope;
    private Integer total;
    private Integer issued;
    private Integer newUserAuto;
    /** 券定义状态：1启用 0停用。 */
    private Integer status;
    /** 用户持有记录 ID（/coupons/available 与 /coupons/my 返回）。 */
    private Long userCouponId;
    /** 用户持有记录状态：0未使用 1已使用 2已过期 3锁定。 */
    private Integer userCouponStatus;
    private LocalDateTime getTime;
    private LocalDateTime useTime;
    /** 当前金额下是否可用。 */
    private Boolean usable = false;
    /** 不可用原因提示。 */
    private String unusableReason;
}
