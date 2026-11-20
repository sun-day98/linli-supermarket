package com.linli.supermarket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 无状态 HMAC-SHA256 Token 测试：签发/解析、篡改检测、过期检测、格式校验。 */
class TokenUtilTest {

    private static final String SECRET = "linli-supermarket-unit-test-secret-2026";

    @Test
    @DisplayName("签发后解析：userId / role / exp 正确回读")
    void createToken_parse_roundTrip() {
        TokenUtil util = new TokenUtil(SECRET, 7, new ObjectMapper());

        String token = util.createToken(1001L, "ADMIN");
        TokenUtil.TokenPayload payload = util.parse(token);

        assertEquals(3, token.split("\\.").length);
        assertEquals(1001L, payload.getUserId());
        assertEquals("ADMIN", payload.getRole());
        assertNotNull(payload.getExp());
        assertTrue(payload.getExp() > System.currentTimeMillis());
    }

    @Test
    @DisplayName("role 为 null 时默认签发 USER")
    void createToken_nullRoleDefaultsToUser() {
        TokenUtil util = new TokenUtil(SECRET, 7, new ObjectMapper());

        assertEquals("USER", util.parse(util.createToken(1001L, null)).getRole());
    }

    @Test
    @DisplayName("篡改 payload 后签名校验失败")
    void parse_tamperedPayload_throws() {
        TokenUtil util = new TokenUtil(SECRET, 7, new ObjectMapper());
        String token = util.createToken(1001L, "USER");
        String[] parts = token.split("\\.", -1);
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"userId\":9999,\"role\":\"ADMIN\",\"exp\":" + (System.currentTimeMillis() + 60000) + "}")
                        .getBytes(StandardCharsets.UTF_8));
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThrows(IllegalArgumentException.class, () -> util.parse(forged));
    }

    @Test
    @DisplayName("使用其他密钥签发的 Token 被拒绝")
    void parse_wrongSecret_throws() {
        TokenUtil signer = new TokenUtil(SECRET, 7, new ObjectMapper());
        TokenUtil verifier = new TokenUtil("another-completely-different-secret", 7, new ObjectMapper());
        String token = signer.createToken(1001L, "USER");

        assertThrows(IllegalArgumentException.class, () -> verifier.parse(token));
    }

    @Test
    @DisplayName("过期 Token 被拒绝")
    void parse_expiredToken_throws() {
        TokenUtil util = new TokenUtil(SECRET, -1, new ObjectMapper());
        String token = util.createToken(1001L, "USER");

        assertThrows(IllegalArgumentException.class, () -> util.parse(token));
    }

    @Test
    @DisplayName("格式非法 / 空 Token 被拒绝")
    void parse_malformedToken_throws() {
        TokenUtil util = new TokenUtil(SECRET, 7, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> util.parse(null));
        assertThrows(IllegalArgumentException.class, () -> util.parse(""));
        assertThrows(IllegalArgumentException.class, () -> util.parse("only.two"));
        assertThrows(IllegalArgumentException.class, () -> util.parse("a.b.c"));
    }
}
