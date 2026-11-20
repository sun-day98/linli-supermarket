package com.linli.supermarket.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 订单明细视图。 */
@Data
public class OrderItemVO {

    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
