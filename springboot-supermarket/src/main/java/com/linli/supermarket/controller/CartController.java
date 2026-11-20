package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.CartDTO;
import com.linli.supermarket.service.CartService;
import com.linli.supermarket.util.UserContext;
import com.linli.supermarket.vo.CartVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 购物车接口。 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<List<CartVO>> list() {
        return Result.ok(cartService.list(UserContext.getUserId()));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartDTO dto) {
        cartService.add(UserContext.getUserId(), dto);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CartDTO dto) {
        cartService.update(UserContext.getUserId(), id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(UserContext.getUserId(), id);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clearSelected() {
        cartService.clearSelected(UserContext.getUserId());
        return Result.ok();
    }
}
