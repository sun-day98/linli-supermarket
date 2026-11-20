package com.linli.supermarket.controller;

import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.AdminLoginDTO;
import com.linli.supermarket.dto.LoginDTO;
import com.linli.supermarket.entity.Admin;
import com.linli.supermarket.service.AuthService;
import com.linli.supermarket.util.TokenUtil;
import com.linli.supermarket.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 鉴权接口。 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenUtil tokenUtil;

    /** 手机号 + 验证码登录；新手机号自动注册。 */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /** 管理员账密登录。 */
    @PostMapping("/admin-login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody AdminLoginDTO dto) {
        String token = authService.adminLogin(dto);
        Admin admin = authService.getAdminByUsername(dto.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", admin == null ? dto.getUsername() : admin.getUsername());
        data.put("realName", admin == null ? "" : admin.getRealName());
        data.put("role", admin == null ? "ADMIN" : admin.getRole());
        return Result.ok(data);
    }

    /**
     * 当前登录信息。/api/auth/** 在白名单内，故此处自行解析 Token。
     */
    @GetMapping("/me")
    public Result<UserVO> me(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        TokenUtil.TokenPayload payload = tokenUtil.parse(header.substring(7).trim());
        return Result.ok(authService.me(payload.getUserId()));
    }
}
