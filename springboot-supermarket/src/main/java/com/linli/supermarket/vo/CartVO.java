package com.linli.supermarket.vo;

import java.math.BigDecimal;
import lombok.Data;

/** 购物车项视图（含商品快照与小计）。 */
@Data
public class CartVO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private String spec;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private Integer stock;
    private BigDecimal subtotal;
}
