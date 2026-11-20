package com.linli.supermarket.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 登录失败限流测试：阈值锁定、成功清零、key 隔离。 */
class LoginAttemptLimiterTest {

    @Test
    @DisplayName("连续失败达到阈值后锁定")
    void recordFailure_locksAfterThreshold() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(3, 15);
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> limiter.recordFailure("admin:hacker"));
        }
        assertThrows(IllegalStateException.class, () -> limiter.checkNotLocked("admin:hacker"));
    }

    @Test
    @DisplayName("未达阈值不锁定")
    void checkNotLocked_belowThreshold_passes() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(3, 15);
        limiter.recordFailure("admin:user1");
        limiter.recordFailure("admin:user1");
        assertDoesNotThrow(() -> limiter.checkNotLocked("admin:user1"));
    }

    @Test
    @DisplayName("成功后清零计数")
    void clear_resetsFailures() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(2, 15);
        limiter.recordFailure("admin:user2");
        limiter.clear("admin:user2");
        limiter.recordFailure("admin:user2");
        assertDoesNotThrow(() -> limiter.checkNotLocked("admin:user2"));
    }

    @Test
    @DisplayName("不同 key 互不影响")
    void limitersAreIsolatedByKey() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(1, 15);
        limiter.recordFailure("admin:a");
        assertThrows(IllegalStateException.class, () -> limiter.checkNotLocked("admin:a"));
        assertDoesNotThrow(() -> limiter.checkNotLocked("admin:b"));
    }
}
