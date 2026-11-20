package com.linli.supermarket.service;

import com.linli.supermarket.dto.CartDTO;
import com.linli.supermarket.vo.CartVO;
import java.util.List;

/** 购物车服务。 */
public interface CartService {

    List<CartVO> list(Long userId);

    void add(Long userId, CartDTO dto);

    void update(Long userId, Long cartId, CartDTO dto);

    void remove(Long userId, Long cartId);

    /** 清空当前用户已勾选的购物车项（下单成功后调用）。 */
    void clearSelected(Long userId);
}
