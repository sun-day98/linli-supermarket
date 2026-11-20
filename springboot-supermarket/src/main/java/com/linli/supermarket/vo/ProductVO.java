package com.linli.supermarket.vo;

import com.linli.supermarket.entity.Review;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/** 商品详情/列表视图，详情接口附带审核通过的评价。 */
@Data
public class ProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String code;
    private BigDecimal price;
    private BigDecimal originalPrice;
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
    /** 库存是否低于阈值，便于后台突出显示。 */
    private Boolean lowStock;
    private LocalDateTime createdAt;
    private List<Review> reviews = Collections.emptyList();
}
