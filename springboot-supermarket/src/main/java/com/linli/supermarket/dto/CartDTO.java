package com.linli.supermarket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 购物车新增/修改请求。 */
@Data
public class CartDTO {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    /** 修改勾选状态时使用；新增时默认勾选。 */
    private Integer selected;
}
