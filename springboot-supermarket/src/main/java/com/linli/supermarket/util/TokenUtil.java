package com.linli.supermarket.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 自研无状态 Token（HMAC-SHA256）：{@code base64url(header).base64url(payload).base64url(signature)}。
 *
 * <p>Payload 包含 userId / role / exp（毫秒时间戳），有效期 7 天，可配置。</p>
 */
@Component
public class TokenUtil {

    private static final Logger log = LoggerFactory.getLogger(TokenUtil.class);
    private static final String ALGORITHM = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    /** 演示默认密钥，仅用于开箱即用；检测到它时打印告警。 */
    static final String DEMO_SECRET = "linli-supermarket-demo-secret-key-2026";
    /** 密钥最小长度：低于该值视为弱密钥，拒绝启动以避免演示默认值误入生产。 */
    private static final int MIN_SECRET_LENGTH = 16;

    private final String secret;
    private final long expireMillis;
    private final ObjectMapper objectMapper;
    private final ThreadLocal<Mac> macHolder = ThreadLocal.withInitial(this::createMac);

    public TokenUtil(@Value("${app.token.secret:}") String secret,
                     @Value("${app.token.expire-days:7}") long expireDays,
                     ObjectMapper objectMapper) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "未配置 app.token.secret，请通过环境变量 APP_TOKEN_SECRET 注入密钥");
        }
        if (DEMO_SECRET.equals(secret)) {
            log.warn("⚠️ 正在使用演示默认 Token 密钥！生产环境必须通过 APP_TOKEN_SECRET 注入高强度随机密钥，"
                    + "否则任何人都可伪造管理员 Token。");
        } else if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "app.token.secret 长度不足 " + MIN_SECRET_LENGTH + " 位，请使用高强度随机密钥");
        }
        this.secret = secret;
        this.expireMillis = expireDays * 24L * 60L * 60L * 1000L;
        this.objectMapper = objectMapper;
    }

    /** 签发 Token。 */
    public String createToken(Long userId, String role) {
        long exp = Instant.now().toEpochMilli() + expireMillis;
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("userId", userId);
        claims.put("role", role == null ? "USER" : role);
        claims.put("exp", exp);
        try {
            String header = URL_ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
            String payload = URL_ENCODER.encodeToString(
                    objectMapper.writeValueAsBytes(claims));
            String unsigned = header + "." + payload;
            String signature = URL_ENCODER.encodeToString(sign(unsigned));
            return unsigned + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("签发 Token 失败", ex);
        }
    }

    /** 解析并校验 Token，非法或过期时抛出 IllegalArgumentException。 */
    public TokenPayload parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token 为空");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token 格式错误");
        }
        String unsigned = parts[0] + "." + parts[1];
        byte[] expected = sign(unsigned);
        byte[] actual;
        try {
            actual = URL_DECODER.decode(parts[2]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token 签名错误", ex);
        }
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new IllegalArgumentException("Token 签名错误");
        }
        try {
            byte[] payloadBytes = URL_DECODER.decode(parts[1]);
            TokenPayload payload = objectMapper.readValue(payloadBytes, TokenPayload.class);
            if (payload.getExp() == null || payload.getExp() < Instant.now().toEpochMilli()) {
                throw new IllegalArgumentException("Token 已过期");
            }
            return payload;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Token 解析失败", ex);
        }
    }

    private byte[] sign(String unsigned) {
        try {
            Mac mac = macHolder.get();
            return mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("计算 Token 签名失败", ex);
        } finally {
            macHolder.remove();
        }
    }

    private Mac createMac() {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return mac;
        } catch (Exception ex) {
            throw new IllegalStateException("初始化 HMAC 失败", ex);
        }
    }

    /** Token 载荷。公开字段便于 Jackson 直接反序列化。 */
    public static class TokenPayload {

        public Long userId;
        public String role;
        public Long exp;

        public TokenPayload() {
        }

        public Long getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }

        public Long getExp() {
            return exp;
        }
    }
}
