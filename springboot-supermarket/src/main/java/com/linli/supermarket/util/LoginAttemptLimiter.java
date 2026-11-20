package com.linli.supermarket.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 管理员登录失败限流：按 "IP + 用户名" 维度计数，达到阈值后临时锁定。
 *
 * <p>纯内存实现，单实例有效、重启清零，适合演示环境；生产多实例应替换为 Redis。
 * TTL 与阈值由配置注入，便于测试与生产调整。用 ConcurrentHashMap + 惰性清理避免无限增长。</p>
 */
@Component
public class LoginAttemptLimiter {

    private final int maxFailures;
    private final long lockMillis;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(
            @Value("${app.security.login-max-failures:5}") int maxFailures,
            @Value("${app.security.login-lock-minutes:15}") long lockMinutes) {
        this.maxFailures = Math.max(1, maxFailures);
        this.lockMillis = Math.max(1, lockMinutes) * 60L * 1000L;
    }

    /** 校验是否处于锁定期，锁定中抛异常。 */
    public void checkNotLocked(String key) {
        evictExpired();
        Attempt attempt = attempts.get(key);
        if (attempt != null && attempt.lockedUntil > System.currentTimeMillis()) {
            long seconds = (attempt.lockedUntil - System.currentTimeMillis()) / 1000L + 1;
            throw new IllegalStateException("登录失败次数过多，请 " + seconds + " 秒后重试");
        }
    }

    /** 记录一次失败；达到阈值后进入锁定期。 */
    public void recordFailure(String key) {
        attempts.compute(key, (k, old) -> {
            Attempt a;
            if (old == null || old.lockedUntil > System.currentTimeMillis()) {
                // 不存在，或仍处在锁定期内：新建计数（锁定中的额外失败重新计时）
                a = new Attempt();
            } else {
                a = old;
            }
            a.failedCount++;
            a.lastAttemptAt = System.currentTimeMillis();
            if (a.failedCount >= maxFailures) {
                a.lockedUntil = System.currentTimeMillis() + lockMillis;
            }
            return a;
        });
    }

    /** 登录成功后清除计数。 */
    public void clear(String key) {
        attempts.remove(key);
    }

    /** 惰性清理已过期且未锁定的条目，避免 Map 无限增长。 */
    private void evictExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Attempt>> it = attempts.entrySet().iterator();
        while (it.hasNext()) {
            Attempt a = it.next().getValue();
            if (a.lockedUntil <= now && now - a.lastAttemptAt > lockMillis) {
                it.remove();
            }
        }
    }

    private static final class Attempt {
        private int failedCount;
        private long lockedUntil;
        private long lastAttemptAt = System.currentTimeMillis();
    }
}
