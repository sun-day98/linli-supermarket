package com.linli.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理员账密登录请求。 */
@Data
public class AdminLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
