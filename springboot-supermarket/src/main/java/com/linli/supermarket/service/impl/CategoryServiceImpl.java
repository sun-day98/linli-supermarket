package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.CategoryDTO;
import com.linli.supermarket.entity.Cart;
import com.linli.supermarket.entity.Category;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.CategoryMapper;
import com.linli.supermarket.mapper.ProductMapper;
import com.linli.supermarket.service.CategoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 分类服务实现。 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    @Override
    public List<Category> listEnabled() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryDTO dto) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类不存在");
        }
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类不存在");
        }
        // 外键约束：先清理该分类下的商品与购物车项
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        for (Product product : products) {
            cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getProductId, product.getId()));
        }
        productMapper.delete(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        categoryMapper.deleteById(id);
    }
}
