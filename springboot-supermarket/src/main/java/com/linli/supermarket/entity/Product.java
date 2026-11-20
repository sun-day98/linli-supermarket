package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 商品表。 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
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
    /** 轮播图 JSON 数组字符串 */
    private String images;
    private String description;
    private String nutrition;
    private Integer sort;
    /** 1热门 0普通 */
    private Integer isHot;
    /** 1上架 0下架 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
