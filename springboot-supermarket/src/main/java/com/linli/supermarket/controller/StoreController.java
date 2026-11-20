package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.config.AuditLog;
import com.linli.supermarket.config.RequireAdmin;
import com.linli.supermarket.entity.StoreSetting;
import com.linli.supermarket.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 门店设置接口（读公开 / 写管理端）。 */
@RestController
@RequestMapping("/api/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping
    public Result<StoreSetting> get() {
        return Result.ok(storeService.get());
    }

    @PutMapping
    @RequireAdmin
    @AuditLog("修改门店设置")
    public Result<Void> update(@RequestBody StoreSetting setting) {
        storeService.update(setting);
        return Result.ok();
    }
}
