package com.linli.supermarket.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Token 吊销表（S1-Lite）测试：吊销前/后签发判定、边界、TTL 清理。 */
class TokenRevocationStoreTest {

    @Test
    @DisplayName("未吊销用户：任何签发时间均放行")
    void isRevoked_notRevoked_false() {
        TokenRevocationStore store = new TokenRevocationStore(7);
        assertFalse(store.isRevoked(1001L, "USER", System.currentTimeMillis()));
    }

    @Test
    @DisplayName("吊销后：吊销前签发的 Token 被拒，吊销后签发的放行")
    void isRevoked_beforeAndAfterRevoke() {
        TokenRevocationStore store = new TokenRevocationStore(7);
        long before = System.currentTimeMillis() - 10_000L;
        store.revoke(1001L, "USER");
        long revokedAt = System.currentTimeMillis();

        assertTrue(store.isRevoked(1001L, "USER", before));
        assertFalse(store.isRevoked(1001L, "USER", revokedAt + 1000L));
    }

    @Test
    @DisplayName("角色隔离：吊销 USER 不影响 ADMIN")
    void isRevoked_roleIsolated() {
        TokenRevocationStore store = new TokenRevocationStore(7);
        store.revoke(1001L, "USER");
        long before = System.currentTimeMillis() - 1000L;
        assertTrue(store.isRevoked(1001L, "USER", before));
        assertFalse(store.isRevoked(1001L, "ADMIN", before));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("边界：签发时间等于吊销时间视为吊销后签发，放行；早 1ms 则拒绝")
    void isRevoked_sameMillisBoundary_passes() {
        TokenRevocationStore store = new TokenRevocationStore(7);
        store.revoke(1001L, "USER");
        // 精确读取内部记录的吊销时间点，避免用外部 currentTimeMillis 造成时序抖动
        Map<String, Long> table = (Map<String, Long>) ReflectionTestUtils.getField(store, "revokedAt");
        long revokedAt = table.get("USER:1001");
        assertFalse(store.isRevoked(1001L, "USER", revokedAt));
        assertTrue(store.isRevoked(1001L, "USER", revokedAt - 1L));
    }

    @Test
    @DisplayName("旧 Token 无签发时间（iat=null）：保守视为已吊销")
    void isRevoked_nullIssuedAt_revoked() {
        TokenRevocationStore store = new TokenRevocationStore(7);
        store.revoke(1001L, "USER");
        assertTrue(store.isRevoked(1001L, "USER", null));
    }
}
