package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.ProductDTO;
import com.linli.supermarket.dto.ProductQueryDTO;
import com.linli.supermarket.entity.Category;
import com.linli.supermarket.entity.Cart;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.entity.Review;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.CategoryMapper;
import com.linli.supermarket.mapper.ProductMapper;
import com.linli.supermarket.mapper.ReviewMapper;
import com.linli.supermarket.service.ProductService;
import com.linli.supermarket.vo.ProductVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 商品服务实现。 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductVO> page(ProductQueryDTO dto) {
        int current = dto.getCurrent() == null ? 1 : Math.max(1, dto.getCurrent());
        int size = dto.getSize() == null ? 10 : Math.min(100, Math.max(1, dto.getSize()));
        IPage<Product> page = productMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<Product>()
                        .eq(dto.getCategoryId() != null, Product::getCategoryId, dto.getCategoryId())
                        .eq(dto.getStatus() != null, Product::getStatus, dto.getStatus())
                        .eq(dto.getIsHot() != null, Product::getIsHot, dto.getIsHot())
                        .like(StringUtils.hasText(dto.getKeyword()), Product::getName, dto.getKeyword())
                        .apply(dto.getLowStock() != null && dto.getLowStock() == 1, "stock <= stock_alert")
                        .orderByAsc(Product::getSort)
                        .orderByDesc(Product::getId));
        Map<Long, String> categoryNames = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        Function<Product, ProductVO> converter = product -> toVO(product, categoryNames.get(product.getCategoryId()));
        return PageResult.of(page, converter);
    }

    @Override
    public Map<Long, Product> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() != null && product.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在或已下架");
        }
        Category category = categoryMapper.selectById(product.getCategoryId());
        ProductVO vo = toVO(product, category == null ? null : category.getName());
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, id)
                .eq(Review::getStatus, 1)
                .orderByDesc(Review::getCreatedAt));
        vo.setReviews(reviews);
        return vo;
    }

    @Override
    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public boolean deductStock(Long id, Integer quantity) {
        return productMapper.deductStock(id, quantity) > 0;
    }

    @Override
    public void rollbackStock(Long id, Integer quantity) {
        productMapper.rollbackStock(id, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        applyDefaults(product);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductDTO dto) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在");
        }
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setId(id);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在");
        }
        productMapper.deleteById(id);
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getProductId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    private void applyDefaults(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getIsHot() == null) {
            product.setIsHot(0);
        }
        if (product.getStockAlert() == null) {
            product.setStockAlert(10);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        if (product.getSort() == null) {
            product.setSort(0);
        }
        if (product.getOriginalPrice() == null) {
            product.setOriginalPrice(product.getPrice());
        }
    }

    private ProductVO toVO(Product product, String categoryName) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        vo.setCategoryName(categoryName);
        vo.setLowStock(product.getStock() != null && product.getStockAlert() != null
                && product.getStock() <= product.getStockAlert());
        return vo;
    }
}
