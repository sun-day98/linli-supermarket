package com.linli.supermarket.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Token 吊销表（S1-Lite）：记录被主动吊销用户的最小吊销时间戳。
 *
 * <p>自研 Token 无状态、无 jti，无法精确吊销单个 Token；本方案按"用户 + 角色"维度记录吊销
 * 时间点，校验时若 Token 的签发时间早于吊销时间则拒绝。这样"禁用用户/改密/踢下线"后，
 * 吊销之前签发的所有旧 Token 立即失效，之后签发的新 Token 正常。TTL 到期自动清理，
 * 与 Token 有效期（默认 7 天）对齐，避免无限增长。纯内存单实例方案，重启清零，
 * 生产多实例应替换为 Redis。</p>
 */
@Component
public class TokenRevocationStore {

    private final long ttlMillis;
    private final ConcurrentHashMap<String, Long> revokedAt = new ConcurrentHashMap<>();

    public TokenRevocationStore(@Value("${app.token.expire-days:7}") long expireDays) {
        this.ttlMillis = Math.max(1, expireDays) * 24L * 60L * 60L * 1000L;
    }

    private static String key(Long userId, String role) {
        return (role == null ? "USER" : role) + ":" + userId;
    }

    /** 吊销某用户某角色在此之前签发的所有 Token。 */
    public void revoke(Long userId, String role) {
        revokedAt.put(key(userId, role), System.currentTimeMillis());
    }

    /**
     * 判断 Token 是否已被吊销。
     *
     * @param issuedAt Token 签发时间（毫秒）；早于吊销时间则视为已吊销。
     *                 为 null 时保守视为已吊销（旧 Token 无签发时间字段）。
     */
    public boolean isRevoked(Long userId, String role, Long issuedAt) {
        Long revoked = revokedAt.get(key(userId, role));
        if (revoked == null) {
            return false;
        }
        // 边界：issuedAt == revoked 视为吊销后新签发，放行；早于吊销时间才拒绝。
        return issuedAt == null || issuedAt < revoked;
    }

    /** 惰性清理过期吊销记录。 */
    public void evictExpired() {
        long cutoff = System.currentTimeMillis() - ttlMillis;
        Iterator<Map.Entry<String, Long>> it = revokedAt.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() < cutoff) {
                it.remove();
            }
        }
    }
}
