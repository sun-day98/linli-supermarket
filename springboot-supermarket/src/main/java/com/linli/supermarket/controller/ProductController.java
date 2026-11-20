package com.linli.supermarket.controller;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.config.AuditLog;
import com.linli.supermarket.config.RequireAdmin;
import com.linli.supermarket.dto.ProductDTO;
import com.linli.supermarket.dto.ProductQueryDTO;
import com.linli.supermarket.service.ProductService;
import com.linli.supermarket.vo.ProductVO;
import jakarta.validation.Valid;
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

/** 商品接口（读接口公开，写接口管理端）。 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> page(ProductQueryDTO query) {
        return Result.ok(productService.page(query));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @PostMapping
    @RequireAdmin
    @AuditLog("新增商品")
    public Result<Map<String, Long>> create(@Valid @RequestBody ProductDTO dto) {
        return Result.ok(java.util.Collections.singletonMap("id", productService.create(dto)));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    @AuditLog("编辑商品")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        productService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequireAdmin
    @AuditLog("上下架商品")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.changeStatus(id, status);
        return Result.ok();
    }
}
