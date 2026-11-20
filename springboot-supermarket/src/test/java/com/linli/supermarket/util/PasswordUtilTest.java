package com.linli.supermarket.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** BCrypt 密码工具测试：加密不可逆、正确口令匹配、错误口令拒绝。 */
class PasswordUtilTest {

    @Test
    @DisplayName("BCrypt 密文与明文不同，且可校验通过")
    void encode_matchesRawPassword() {
        String hash = PasswordUtil.encode("admin123");

        assertNotNull(hash);
        assertNotEquals("admin123", hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(PasswordUtil.matches("admin123", hash));
    }

    @Test
    @DisplayName("错误口令校验失败")
    void matches_wrongPassword_returnsFalse() {
        String hash = PasswordUtil.encode("admin123");

        assertFalse(PasswordUtil.matches("wrong", hash));
    }

    @Test
    @DisplayName("空输入安全返回 false，不抛异常")
    void matches_nullOrBlank_returnsFalse() {
        String hash = PasswordUtil.encode("admin123");

        assertFalse(PasswordUtil.matches(null, hash));
        assertFalse(PasswordUtil.matches("admin123", null));
        assertFalse(PasswordUtil.matches("admin123", "   "));
        assertFalse(PasswordUtil.matches("", hash));
    }

    @Test
    @DisplayName("相同明文两次加密结果不同（随机盐）")
    void encode_usesRandomSalt() {
        String first = PasswordUtil.encode("123456");
        String second = PasswordUtil.encode("123456");

        assertNotEquals(first, second);
        assertTrue(PasswordUtil.matches("123456", first));
        assertTrue(PasswordUtil.matches("123456", second));
    }
}
