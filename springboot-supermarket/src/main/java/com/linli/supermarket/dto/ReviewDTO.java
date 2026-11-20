package com.linli.supermarket.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 商品评价提交请求。 */
@Data
public class ReviewDTO {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低 1 星")
    @Max(value = 5, message = "评分最高 5 星")
    private Integer rating;

    private String content;

    /** 图片 URL JSON 数组字符串。 */
    private String images;
}
