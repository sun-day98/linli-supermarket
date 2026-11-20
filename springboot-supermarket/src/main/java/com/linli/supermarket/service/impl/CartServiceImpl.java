package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.CartDTO;
import com.linli.supermarket.entity.Cart;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.mapper.CartMapper;
import com.linli.supermarket.mapper.ProductMapper;
import com.linli.supermarket.service.CartService;
import com.linli.supermarket.vo.CartVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 购物车服务实现。 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<CartVO> list(Long userId) {
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreatedAt));
        if (carts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = carts.stream().map(Cart::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> products = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        List<CartVO> result = new ArrayList<>();
        for (Cart cart : carts) {
            Product product = products.get(cart.getProductId());
            if (product == null) {
                continue;
            }
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
            vo.setSpec(product.getSpec());
            vo.setPrice(product.getPrice());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected());
            vo.setStock(product.getStock());
            vo.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()))
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, CartDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != null && product.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在或已下架");
        }
        Cart existing = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, dto.getProductId())
                .last("LIMIT 1"));
        if (existing == null) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setQuantity(dto.getQuantity());
            cart.setSelected(dto.getSelected() == null ? 1 : dto.getSelected());
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.insert(cart);
            return;
        }
        int quantity = existing.getQuantity() + dto.getQuantity();
        if (product.getStock() != null && quantity > product.getStock()) {
            quantity = product.getStock();
        }
        existing.setQuantity(quantity);
        existing.setSelected(dto.getSelected() == null ? existing.getSelected() : dto.getSelected());
        existing.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long cartId, CartDTO dto) {
        Cart cart = requireOwned(userId, cartId);
        cart.setQuantity(dto.getQuantity());
        if (dto.getSelected() != null) {
            cart.setSelected(dto.getSelected());
        }
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long cartId) {
        requireOwned(userId, cartId);
        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSelected(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1));
    }

    private Cart requireOwned(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "购物车项不存在");
        }
        return cart;
    }
}
