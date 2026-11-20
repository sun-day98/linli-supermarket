package com.linli.supermarket.util;

import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;

/**
 * 当前请求登录上下文（ThreadLocal）。由 {@code TokenInterceptor} 写入，请求结束后清理。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String role) {
        HOLDER.set(new CurrentUser(userId, role));
    }

    /** 获取当前登录用户 ID，未登录时抛出 401 业务异常。 */
    public static Long getUserId() {
        CurrentUser current = HOLDER.get();
        if (current == null || current.userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return current.userId;
    }

    /** 获取当前登录角色，未登录时返回 null。 */
    public static String getRole() {
        CurrentUser current = HOLDER.get();
        return current == null ? null : current.role;
    }

    /** 当前登录者是否为管理员。 */
    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    /** 获取当前登录用户，未登录时抛出 401 业务异常。 */
    public static CurrentUser get() {
        CurrentUser current = HOLDER.get();
        if (current == null || current.userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return current;
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 登录主体。 */
    public static class CurrentUser {

        private final Long userId;
        private final String role;

        public CurrentUser(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }
    }
}
