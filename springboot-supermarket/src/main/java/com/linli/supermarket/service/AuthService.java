package com.linli.supermarket.service;

import com.linli.supermarket.dto.AdminLoginDTO;
import com.linli.supermarket.dto.LoginDTO;
import com.linli.supermarket.entity.Admin;
import com.linli.supermarket.entity.User;
import com.linli.supermarket.vo.UserVO;

/** 鉴权与会员服务。 */
public interface AuthService {

    /** 手机号 + 验证码登录，新手机号自动注册。 */
    UserVO login(LoginDTO dto);

    /** 管理员账密登录，返回 Token。 */
    String adminLogin(AdminLoginDTO dto);

    /** 新手机号自动注册：建用户 + 默认地址 + 新人券。 */
    User autoRegister(String phone);

    /** 按用户 ID 查询资料。 */
    UserVO me(Long userId);

    /** 按账号查询管理员（登录响应携带基本信息）。 */
    Admin getAdminByUsername(String username);
}
