package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 商品评价。状态：0待审核 1通过 2拒绝。 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    /** 1~5 星 */
    private Integer rating;
    private String content;
    /** 图片 JSON 数组字符串 */
    private String images;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
