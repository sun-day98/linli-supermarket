package com.linli.supermarket.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 金额试算结果：商品金额 + 配送费 − 优惠 = 应付金额（均保留 2 位小数）。 */
@Data
public class CheckoutPreviewVO {

    private BigDecimal goodsAmount = BigDecimal.ZERO.setScale(2);
    private BigDecimal deliveryFee = BigDecimal.ZERO.setScale(2);
    private BigDecimal discountAmount = BigDecimal.ZERO.setScale(2);
    private BigDecimal payAmount = BigDecimal.ZERO.setScale(2);
    /** 是否命中免配送门槛。 */
    private Boolean freeDelivery = false;
    private Long userCouponId;
    private String couponName;
    /** 地址所属区县是否可配送。 */
    private Boolean deliverable = true;
    private String district;
    private List<Item> items = new ArrayList<>();

    /** 试算明细项。 */
    @Data
    public static class Item {

        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price = BigDecimal.ZERO.setScale(2);
        private Integer quantity;
        private BigDecimal totalAmount = BigDecimal.ZERO.setScale(2);
    }
}
