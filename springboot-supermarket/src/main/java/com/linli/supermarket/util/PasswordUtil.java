package com.linli.supermarket.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希工具：基于 spring-security-crypto 的 BCrypt。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 对明文密码进行 BCrypt 加密。 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验明文密码与 BCrypt 密文是否匹配。 */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
