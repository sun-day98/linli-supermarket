package com.linli.supermarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.util.TokenUtil;
import com.linli.supermarket.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import com.linli.supermarket.util.TokenRevocationStore;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 统一鉴权拦截器：白名单直接放行，其余接口校验 Bearer Token 并写入 {@link UserContext}。
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenRevocationStore tokenRevocationStore;

    @Autowired(required = false)
    private HttpStatusPolicy httpStatusPolicy;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String path = request.getRequestURI();
        if (isWhiteListed(path, method)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeFailure(response, ResultCode.UNAUTHORIZED, "请先登录");
            return false;
        }

        TokenUtil.TokenPayload payload;
        try {
            payload = tokenUtil.parse(authHeader.substring(BEARER_PREFIX.length()).trim());
        } catch (RuntimeException ex) {
            writeFailure(response, ResultCode.UNAUTHORIZED, "登录已失效，请重新登录");
            return false;
        }

        // Token 吊销校验（S1-Lite）：用户被禁用/改密后，吊销其此前签发的所有 Token。
        if (tokenRevocationStore != null
                && tokenRevocationStore.isRevoked(payload.getUserId(), payload.getRole(), payload.getIssuedAt())) {
            writeFailure(response, ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
            return false;
        }

        // 管理端判定：路径规则 + @RequireAdmin 注解叠加（取或）。
        // handler 非 HandlerMethod（如测试中的普通 Object）时注解判定自动跳过，回退到路径判定。
        if ((isAdminEndpoint(method, path) || isAdminAnnotated(handler))
                && !"ADMIN".equalsIgnoreCase(payload.getRole())) {
            writeFailure(response, ResultCode.UNAUTHORIZED, "无管理员权限");
            return false;
        }

        UserContext.set(payload.getUserId(), payload.getRole());
        return true;
    }

    /** 判断处理器方法/类是否标注 {@link RequireAdmin}。 */
    private boolean isAdminAnnotated(Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return false;
        }
        return hm.getMethodAnnotation(RequireAdmin.class) != null
                || hm.getBeanType().isAnnotationPresent(RequireAdmin.class);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // 无需处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    /**
     * 白名单规则：
     * <ul>
     *   <li>/api/auth/** 全部放行（/auth/me 在 Controller 内自行解析 Token）</li>
     *   <li>GET /api/products/**、/api/categories、/api/store、/api/delivery/areas、/api/reviews</li>
     * </ul>
     */
    private boolean isWhiteListed(String path, String method) {
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)) {
            return path.equals("/api/products") || path.startsWith("/api/products/")
                    || path.equals("/api/categories")
                    // /api/categories/all 含停用分类，属管理端视图，不能随前缀误放行
                    || (path.startsWith("/api/categories/") && !path.equals("/api/categories/all"))
                    || path.equals("/api/store")
                    || path.equals("/api/delivery/areas") || path.startsWith("/api/delivery/areas/")
                    || path.equals("/api/reviews") || path.startsWith("/api/reviews/");
        }
        return false;
    }

    /**
     * 管理端接口判定：除统一的 /api/admin/ 前缀外，还覆盖散落在用户端 Controller 中、
     * 语义为管理端的写接口，避免普通 USER Token 越权改价、上下架、发券、改门店配置、代发货。
     * 仅收紧写操作（POST/PUT/DELETE），公开的 GET 读接口仍由白名单/普通鉴权处理。
     */
    private boolean isAdminEndpoint(String method, String path) {
        if (path.startsWith("/api/admin/")) {
            return true;
        }
        boolean write = "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
        if (!write) {
            return false;
        }
        // 优惠券管理：POST /api/coupons/admin、PUT|DELETE /api/coupons/admin/{id}
        // 注意 GET /api/coupons/admin 刻意保留给结算页展示券定义，不在收紧范围
        if (path.equals("/api/coupons/admin") || path.startsWith("/api/coupons/admin/")) {
            return true;
        }
        // 商品写接口：POST /api/products、PUT|DELETE /api/products/{id}、PUT /api/products/{id}/status
        if (path.equals("/api/products") || path.startsWith("/api/products/")) {
            return true;
        }
        // 分类写接口：POST /api/categories、PUT|DELETE /api/categories/{id}
        if (path.equals("/api/categories") || path.startsWith("/api/categories/")) {
            return true;
        }
        // 门店设置写接口：PUT /api/store
        if (path.equals("/api/store")) {
            return true;
        }
        // 后台发货：POST /api/orders/{id}/deliver（用户端 finish/cancel/pay 不在此列）
        if (path.startsWith("/api/orders/") && path.endsWith("/deliver")) {
            return true;
        }
        return false;
    }

    private void writeFailure(HttpServletResponse response, ResultCode resultCode, String message)
            throws IOException {
        int status = httpStatusPolicy == null ? HttpServletResponse.SC_OK : httpStatusPolicy.httpStatus(resultCode);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(resultCode, message)));
    }
}
