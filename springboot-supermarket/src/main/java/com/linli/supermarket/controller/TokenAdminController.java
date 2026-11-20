package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.config.AuditLog;
import com.linli.supermarket.config.RequireAdmin;
import com.linli.supermarket.util.TokenRevocationStore;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Token 吊销管理接口：禁用用户/改密/踢下线时，使该用户此前签发的 Token 立即失效。
 * 路径位于 /api/admin/** 下，仅 ADMIN 可访问。
 */
@RestController
@RequestMapping("/api/admin/tokens")
public class TokenAdminController {

    @Autowired
    private TokenRevocationStore tokenRevocationStore;

    /** 吊销指定用户某角色此前签发的所有 Token。 */
    @PostMapping("/revoke")
    @RequireAdmin
    @AuditLog("吊销用户Token")
    public Result<Void> revoke(@RequestBody Map<String, Object> body) {
        Object uid = body == null ? null : body.get("userId");
        if (uid == null) {
            return Result.fail(400, "userId 不能为空");
        }
        Long userId = uid instanceof Number ? ((Number) uid).longValue() : Long.valueOf(uid.toString());
        Object role = body.get("role");
        tokenRevocationStore.revoke(userId, role == null ? null : role.toString());
        return Result.ok();
    }
}
