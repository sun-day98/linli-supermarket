package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.AddressDTO;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.service.AddressService;
import com.linli.supermarket.util.UserContext;
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

/** 收货地址接口。 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Result<List<Address>> list() {
        return Result.ok(addressService.list(UserContext.getUserId()));
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody AddressDTO dto) {
        return Result.ok(Collections.singletonMap("id",
                addressService.create(UserContext.getUserId(), dto)));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        addressService.update(UserContext.getUserId(), id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(UserContext.getUserId(), id);
        return Result.ok();
    }
}
