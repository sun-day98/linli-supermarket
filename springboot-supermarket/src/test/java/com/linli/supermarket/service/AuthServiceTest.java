package com.linli.supermarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.AdminLoginDTO;
import com.linli.supermarket.dto.LoginDTO;
import com.linli.supermarket.entity.Admin;
import com.linli.supermarket.entity.User;
import com.linli.supermarket.mapper.AdminMapper;
import com.linli.supermarket.mapper.AddressMapper;
import com.linli.supermarket.mapper.UserMapper;
import com.linli.supermarket.service.impl.AuthServiceImpl;
import com.linli.supermarket.util.PasswordUtil;
import com.linli.supermarket.util.TokenUtil;
import com.linli.supermarket.vo.UserVO;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/** 鉴权测试：验证码校验、自动注册、BCrypt 校验、禁用账号、管理员角色签发。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final Long USER_ID = 1001L;
    private static final Long ADMIN_ID = 1L;

    @Mock private UserMapper userMapper;
    @Mock private AdminMapper adminMapper;
    @Mock private AddressMapper addressMapper;
    @Mock private CouponService couponService;
    @Mock private TokenUtil tokenUtil;

    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    void injectVerifyCode() {
        // demoVerifyCode 由 @Value 注入，纯 Mockito 单元测试需手动赋值
        ReflectionTestUtils.setField(authService, "demoVerifyCode", "123456");
    }

    @Test
    @DisplayName("用户端：验证码错误被拒绝")
    void login_wrongVerifyCode_throws() {
        LoginDTO dto = loginDto("13912348765", "000000");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));

        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("用户端：已存在用户登录返回 USER 角色 Token")
    void login_existingUser_returnsUserToken() {
        User user = user(1);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(tokenUtil.createToken(USER_ID, "USER")).thenReturn("user-token");
        LoginDTO dto = loginDto("13912348765", "123456");

        UserVO vo = authService.login(dto);

        assertEquals("user-token", vo.getToken());
        assertEquals(USER_ID, vo.getId());
        verify(tokenUtil).createToken(USER_ID, "USER");
    }

    @Test
    @DisplayName("用户端：被禁用账号无法登录")
    void login_disabledUser_throws() {
        User user = user(0);
        when(userMapper.selectOne(any())).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginDto("13912348765", "123456")));

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("用户端：新手机号自动注册并初始化地址与新人券")
    void login_newPhone_autoRegisters() {
        when(userMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", USER_ID);
            return 1;
        }).when(userMapper).insert(any(User.class));

        UserVO vo = authService.login(loginDto("13900009999", "123456"));

        assertNotNull(vo);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertEquals("13900009999", userCaptor.getValue().getPhone());
        assertEquals(0, userCaptor.getValue().getPoints());
        assertEquals(1, userCaptor.getValue().getStatus());
        assertTrue(userCaptor.getValue().getNickname().contains("9999"));
        verify(addressMapper).insert(any(com.linli.supermarket.entity.Address.class));
        verify(couponService).grantNewUserCoupons(USER_ID);
        verify(tokenUtil).createToken(USER_ID, "USER");
    }

    @Test
    @DisplayName("管理端：密码正确签发 ADMIN 角色 Token")
    void adminLogin_success() {
        Admin admin = admin("admin123", 1);
        when(adminMapper.selectList(any())).thenReturn(Collections.singletonList(admin));
        when(tokenUtil.createToken(ADMIN_ID, "ADMIN")).thenReturn("admin-token");
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        assertEquals("admin-token", authService.adminLogin(dto));
        verify(tokenUtil).createToken(ADMIN_ID, "ADMIN");
    }

    @Test
    @DisplayName("管理端：BCrypt 密码错误被拒绝")
    void adminLogin_wrongPassword_throws() {
        Admin admin = admin("admin123", 1);
        when(adminMapper.selectList(any())).thenReturn(Collections.singletonList(admin));
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong-password");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.adminLogin(dto));

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), ex.getCode());
        verify(tokenUtil, never()).createToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("管理端：被禁用管理员无法登录")
    void adminLogin_disabledAdmin_throws() {
        when(adminMapper.selectList(any())).thenReturn(
                Collections.singletonList(admin("admin123", 0)));
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        assertEquals(ResultCode.UNAUTHORIZED.getCode(),
                assertThrows(BusinessException.class, () -> authService.adminLogin(dto)).getCode());
    }

    @Test
    @DisplayName("管理端：数据库中明文密码无法通过 BCrypt 校验（安全回归）")
    void adminLogin_plainTextPasswordCannotPassBcrypt() {
        Admin admin = admin("admin123", 1);
        admin.setPassword("admin123"); // 误存明文
        when(adminMapper.selectList(any())).thenReturn(Collections.singletonList(admin));
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        assertThrows(BusinessException.class, () -> authService.adminLogin(dto));
    }

    @Test
    @DisplayName("me：用户不存在抛 401")
    void me_whenUserMissing_throws() {
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(),
                assertThrows(BusinessException.class, () -> authService.me(USER_ID)).getCode());
    }

    // ---------- helpers ----------

    private static LoginDTO loginDto(String phone, String code) {
        LoginDTO dto = new LoginDTO();
        dto.setPhone(phone);
        dto.setCode(code);
        return dto;
    }

    private static User user(int status) {
        User user = new User();
        user.setId(USER_ID);
        user.setPhone("13912348765");
        user.setPoints(128);
        user.setStatus(status);
        return user;
    }

    private static Admin admin(String rawPassword, int status) {
        Admin admin = new Admin();
        admin.setId(ADMIN_ID);
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.encode(rawPassword));
        admin.setRole("ADMIN");
        admin.setStatus(status);
        return admin;
    }
}
