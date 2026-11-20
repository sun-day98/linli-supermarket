package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 优惠券定义表。 */
@Data
@TableName("coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 1满减 2折扣 3无门槛 */
    private Integer type;
    /** 满减金额 / 折扣抵扣比例（0.20 表示减免 20%） */
    private BigDecimal discount;
    /** 使用门槛，0 表示无门槛 */
    private BigDecimal threshold;
    /** 1固定日期 2领取后 N 天 */
    private Integer validType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    /** 1全部 2新人专享 */
    private Integer scope;
    private Integer total;
    private Integer issued;
    private Integer newUserAuto;
    /** 1启用 0停用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
