package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 商品分类表。 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    /** 1启用 0停用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
