package com.linli.supermarket.dto;

import lombok.Data;

/** 商品列表查询条件。 */
@Data
public class ProductQueryDTO {

    private Long categoryId;
    private String keyword;
    /** 1上架 0下架；前端默认只看上架。 */
    private Integer status;
    /** 1只看热门。 */
    private Integer isHot;
    /** 1只看库存紧张（stock &lt;= stock_alert）。 */
    private Integer lowStock;
    private Integer current = 1;
    private Integer size = 10;
}
