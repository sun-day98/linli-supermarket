package com.linli.supermarket.service;

import com.linli.supermarket.dto.CategoryDTO;
import com.linli.supermarket.entity.Category;
import java.util.List;

/** 商品分类服务。 */
public interface CategoryService {

    /** 启用分类（前端），按 sort 升序。 */
    List<Category> listEnabled();

    /** 全部分类（管理端）。 */
    List<Category> listAll();

    Long create(CategoryDTO dto);

    void update(Long id, CategoryDTO dto);

    void delete(Long id);
}
