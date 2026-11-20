package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 订单主表。状态：0待付款 1已付款/待配送 2配送中 3已完成 4已取消。
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long addressId;
    private String receiver;
    private String phone;
    private String addressDetail;
    private BigDecimal goodsAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Long couponId;
    private Long userCouponId;
    private String payType;
    private Integer status;
    private Long deliverymanId;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
