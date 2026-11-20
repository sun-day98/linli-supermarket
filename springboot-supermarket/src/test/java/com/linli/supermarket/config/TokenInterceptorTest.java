package com.linli.supermarket.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linli.supermarket.util.TokenUtil;
import com.linli.supermarket.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** 鉴权拦截器测试：白名单、Bearer 校验、管理员角色校验、上下文清理。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenInterceptorTest {

    private static final Long USER_ID = 1001L;

    @Mock private TokenUtil tokenUtil;
    @Mock private ObjectMapper objectMapper;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks private TokenInterceptor interceptor;

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    @DisplayName("OPTIONS 预检请求直接放行")
    void preHandle_optionsPasses() throws IOException {
        when(request.getMethod()).thenReturn("OPTIONS");

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(tokenUtil, never()).parse(anyString());
    }

    @Test
    @DisplayName("白名单：公开 GET 商品/分类/门店/配送区域/评价无需 Token")
    void preHandle_publicGetPathsPassWithoutToken() throws IOException {
        String[] paths = {"/api/products", "/api/products/1", "/api/categories",
                "/api/store", "/api/delivery/areas", "/api/reviews", "/api/reviews/1"};
        for (String path : paths) {
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn(path);
            assertTrue(interceptor.preHandle(request, response, new Object()), path + " 应放行");
        }
        verify(tokenUtil, never()).parse(anyString());
    }

    @Test
    @DisplayName("安全回归：GET /api/categories/all 含停用分类，不再随前缀白名单放行")
    void preHandle_categoriesAllRequiresToken() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/categories/all");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        assertFalse(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("白名单：/api/auth/** 全部放行（登录接口）")
    void preHandle_authPathsPassWithoutToken() throws IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("受保护 GET /api/orders：无 Authorization 头被拒绝")
    void preHandle_protectedPathWithoutToken_rejected() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(200);
        verify(tokenUtil, never()).parse(anyString());
    }

    @Test
    @DisplayName("受保护接口：非 Bearer 前缀的 Authorization 被拒绝")
    void preHandle_nonBearerHeader_rejected() throws IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn("Basic abcdef");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        assertFalse(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("管理员路径：USER 角色 Token 被拒绝")
    void preHandle_userRoleOnAdminPath_rejected() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/admin/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(tokenUtil.parse("user-token")).thenReturn(payload("USER"));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(null, UserContext.getRole());
    }

    @Test
    @DisplayName("管理员路径：ADMIN 角色 Token 放行并写入 UserContext")
    void preHandle_adminRoleOnAdminPath_passes() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/admin/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer admin-token");
        when(tokenUtil.parse("admin-token")).thenReturn(payload("ADMIN"));

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(USER_ID, UserContext.getUserId());
        assertEquals("ADMIN", UserContext.getRole());
    }

    @Test
    @DisplayName("非法/过期 Token 被拒绝，不写入 UserContext")
    void preHandle_invalidToken_rejected() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(tokenUtil.parse("bad-token")).thenThrow(new IllegalArgumentException("Token 已过期"));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertThrows(com.linli.supermarket.common.BusinessException.class, UserContext::getUserId);
    }

    @Test
    @DisplayName("QA-002 回归：散落在用户端 Controller 的管理端写接口，USER 角色全部拒绝")
    void preHandle_userRoleOnScatteredAdminWrites_rejected() throws IOException {
        String[][] cases = {
                {"POST", "/api/coupons/admin"},
                {"PUT", "/api/coupons/admin/7001"},
                {"DELETE", "/api/coupons/admin/7001"},
                {"POST", "/api/products"},
                {"PUT", "/api/products/4001"},
                {"DELETE", "/api/products/4001"},
                {"PUT", "/api/products/4001/status"},
                {"POST", "/api/categories"},
                {"PUT", "/api/categories/100"},
                {"DELETE", "/api/categories/100"},
                {"PUT", "/api/store"},
                {"POST", "/api/orders/3001/deliver"}
        };
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(tokenUtil.parse("user-token")).thenReturn(payload("USER"));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        for (String[] c : cases) {
            when(request.getMethod()).thenReturn(c[0]);
            when(request.getRequestURI()).thenReturn(c[1]);
            assertFalse(interceptor.preHandle(request, response, new Object()),
                    c[0] + " " + c[1] + " 应拒绝 USER 角色");
        }
    }

    @Test
    @DisplayName("QA-002 回归：管理端写接口对 ADMIN 角色放行")
    void preHandle_adminRoleOnScatteredAdminWrites_passes() throws IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer admin-token");
        when(tokenUtil.parse("admin-token")).thenReturn(payload("ADMIN"));

        String[][] cases = {
                {"POST", "/api/products"},
                {"PUT", "/api/store"},
                {"POST", "/api/orders/3001/deliver"}
        };
        for (String[] c : cases) {
            when(request.getMethod()).thenReturn(c[0]);
            when(request.getRequestURI()).thenReturn(c[1]);
            assertTrue(interceptor.preHandle(request, response, new Object()),
                    c[0] + " " + c[1] + " 应放行 ADMIN 角色");
        }
    }

    @Test
    @DisplayName("QA-002 回归：用户自身写操作不被误伤（下单/领券/评价）")
    void preHandle_userOwnWrites_pass() throws IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(tokenUtil.parse("user-token")).thenReturn(payload("USER"));

        String[][] cases = {
                {"POST", "/api/orders"},
                {"POST", "/api/orders/3001/pay"},
                {"POST", "/api/orders/3001/finish"},
                {"POST", "/api/orders/3001/cancel"},
                {"POST", "/api/coupons/claim/7001"},
                {"POST", "/api/reviews"}
        };
        for (String[] c : cases) {
            when(request.getMethod()).thenReturn(c[0]);
            when(request.getRequestURI()).thenReturn(c[1]);
            assertTrue(interceptor.preHandle(request, response, new Object()),
                    c[0] + " " + c[1] + " 是用户操作，不应被拦截");
        }
    }

    @Test
    @DisplayName("QA-002 回归：GET /api/coupons/admin 按设计保留给结算页读取")
    void preHandle_getCouponDefinitionsStillAllowedForUser() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/coupons/admin");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(tokenUtil.parse("user-token")).thenReturn(payload("USER"));

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("请求结束后 afterCompletion 清理 ThreadLocal，避免上下文串号")
    void afterCompletion_clearsUserContext() {
        UserContext.set(USER_ID, "USER");

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThrows(com.linli.supermarket.common.BusinessException.class, UserContext::getUserId);
    }

    private static TokenUtil.TokenPayload payload(String role) {
        TokenUtil.TokenPayload payload = new TokenUtil.TokenPayload();
        payload.userId = USER_ID;
        payload.role = role;
        payload.exp = System.currentTimeMillis() + 60_000L;
        return payload;
    }
}
