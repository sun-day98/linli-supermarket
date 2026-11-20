package com.linli.supermarket.controller;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.CouponDTO;
import com.linli.supermarket.service.CouponService;
import com.linli.supermarket.util.UserContext;
import com.linli.supermarket.vo.CouponVO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 优惠券接口（用户端 + 管理端）。 */
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /** 当前金额下可用券。 */
    @GetMapping("/available")
    public Result<List<CouponVO>> available(@RequestParam(required = false) BigDecimal amount) {
        return Result.ok(couponService.available(UserContext.getUserId(),
                amount == null ? BigDecimal.ZERO : amount));
    }

    @GetMapping("/my")
    public Result<List<CouponVO>> my() {
        return Result.ok(couponService.my(UserContext.getUserId()));
    }

    @PostMapping("/claim/{id}")
    public Result<Void> claim(@PathVariable Long id) {
        couponService.claim(UserContext.getUserId(), id);
        return Result.ok();
    }

    @GetMapping("/admin")
    public Result<PageResult<CouponVO>> adminPage(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.ok(couponService.adminPage(current, size));
    }

    @PostMapping("/admin")
    public Result<Map<String, Long>> createAdmin(@Valid @RequestBody CouponDTO dto) {
        return Result.ok(Collections.singletonMap("id", couponService.createAdmin(dto)));
    }

    @PutMapping("/admin/{id}")
    public Result<Void> updateAdmin(@PathVariable Long id, @Valid @RequestBody CouponDTO dto) {
        couponService.updateAdmin(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        couponService.deleteAdmin(id);
        return Result.ok();
    }
}
