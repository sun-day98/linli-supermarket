package com.linli.supermarket.service;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.dto.ReviewDTO;
import com.linli.supermarket.entity.Review;

/** 商品评价服务。 */
public interface ReviewService {

    /** 商品详情页评价（仅审核通过）。 */
    PageResult<Review> pageByProduct(Long productId, Integer current, Integer size);

    Long submit(Long userId, ReviewDTO dto);

    PageResult<Review> adminPage(Integer status, Integer current, Integer size);

    void audit(Long id, Integer status);

    void delete(Long id);
}
