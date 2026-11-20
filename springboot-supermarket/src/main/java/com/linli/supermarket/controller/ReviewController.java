package com.linli.supermarket.controller;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.ReviewDTO;
import com.linli.supermarket.entity.Review;
import com.linli.supermarket.service.ReviewService;
import com.linli.supermarket.util.UserContext;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 评价接口。 */
@RestController
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /** 商品评价（仅审核通过）。 */
    @GetMapping("/api/reviews")
    public Result<PageResult<Review>> productReviews(
            @RequestParam Long productId,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.ok(reviewService.pageByProduct(productId, current, size));
    }

    @PostMapping("/api/reviews")
    public Result<Map<String, Long>> submit(@Valid @RequestBody ReviewDTO dto) {
        return Result.ok(Collections.singletonMap("id",
                reviewService.submit(UserContext.getUserId(), dto)));
    }

    @GetMapping("/api/admin/reviews")
    public Result<PageResult<Review>> adminReviews(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.ok(reviewService.adminPage(status, current, size));
    }

    @PutMapping("/api/admin/reviews/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        reviewService.audit(id, status);
        return Result.ok();
    }

    @DeleteMapping("/api/admin/reviews/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.ok();
    }
}
