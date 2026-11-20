package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 购物车表。 */
@Data
@TableName("cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    /** 1勾选 0未勾选 */
    private Integer selected;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
