package com.linli.supermarket.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 订单详情/列表视图。 */
@Data
public class OrderVO {

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
    /** 状态中文名：待付款/待配送/配送中/已完成/已取消。 */
    private String statusText;
    private Long deliverymanId;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items = new ArrayList<>();
}
