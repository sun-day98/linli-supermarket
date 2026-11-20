package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.ReviewDTO;
import com.linli.supermarket.entity.Review;
import com.linli.supermarket.mapper.ReviewMapper;
import com.linli.supermarket.service.ReviewService;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 评价服务实现。 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public PageResult<Review> pageByProduct(Long productId, Integer current, Integer size) {
        IPage<Review> page = reviewMapper.selectPage(
                new Page<>(current == null ? 1 : current, size == null ? 10 : size),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreatedAt));
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(Long userId, ReviewDTO dto) {
        Review review = new Review();
        BeanUtils.copyProperties(dto, review);
        review.setUserId(userId);
        review.setStatus(0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        return review.getId();
    }

    @Override
    public PageResult<Review> adminPage(Integer status, Integer current, Integer size) {
        IPage<Review> page = reviewMapper.selectPage(
                new Page<>(current == null ? 1 : current, size == null ? 10 : size),
                new LambdaQueryWrapper<Review>()
                        .eq(status != null, Review::getStatus, status)
                        .orderByDesc(Review::getCreatedAt));
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审核状态错误");
        }
        Review review = new Review();
        review.setId(id);
        review.setStatus(status);
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        reviewMapper.deleteById(id);
    }
}
