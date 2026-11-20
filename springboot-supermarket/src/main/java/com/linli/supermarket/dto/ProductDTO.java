package com.linli.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/** 商品新增/编辑请求（管理端）。 */
@Data
public class ProductDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名不能为空")
    private String name;

    private String subtitle;
    private String code;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private Integer stockAlert;
    private Integer sales;
    private String unit;
    private String spec;
    private String image;
    private String images;
    private String description;
    private String nutrition;
    private Integer sort;
    private Integer isHot;
    private Integer status;
}
