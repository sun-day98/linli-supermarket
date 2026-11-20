package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.CategoryDTO;
import com.linli.supermarket.entity.Category;
import com.linli.supermarket.service.CategoryService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

/** 分类接口。 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** 启用分类（前端）。 */
    @GetMapping
    public Result<List<Category>> listEnabled() {
        return Result.ok(categoryService.listEnabled());
    }

    /** 全部分类（管理端）。 */
    @GetMapping("/all")
    public Result<List<Category>> listAll() {
        return Result.ok(categoryService.listAll());
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody CategoryDTO dto) {
        return Result.ok(Collections.singletonMap("id", categoryService.create(dto)));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
