package com.linli.supermarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 结算试算 / 提交订单请求。 */
@Data
public class CheckoutDTO {

    /** 结算商品清单，单价由后端重新读取，前端传入价格不参与计算。 */
    @Valid
    @NotEmpty(message = "结算商品不能为空")
    private List<CheckoutItem> items = new ArrayList<>();

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 用户持有的优惠券记录 ID（user_coupon.id）。 */
    private Long couponId;

    /** 优先使用此字段；为空时回退到 couponId。 */
    private Long userCouponId;

    private String remark;

    /** 前端试算金额，可选；传入时后端校验一致性，防止展示与提交口径不一致。 */
    private BigDecimal expectedPayAmount;

    /** 结算明细项。 */
    @Data
    public static class CheckoutItem {

        @NotNull(message = "商品 ID 不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        private Integer quantity;
    }
}
