package com.cupk.healthy_diet.security;

import com.cupk.healthy_diet.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class AuthTokenManager {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    @Value("${auth.token.secret:${AUTH_TOKEN_SECRET:healthy-diet-local-dev-secret}}")
    private String tokenSecret;

    @Value("${auth.token.expire-seconds:${AUTH_TOKEN_EXPIRE_SECONDS:604800}}")
    private long expireSeconds;

    public String createToken(Integer userId, String role) {
        long expiresAt = Instant.now().getEpochSecond() + expireSeconds;
        String payload = userId + "|" + role + "|" + expiresAt;
        String encodedPayload = URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public AuthUser parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(401, "请先登录");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2 || !sign(parts[0]).equals(parts[1])) {
            throw new BusinessException(401, "登录状态无效，请重新登录");
        }

        String payload = new String(URL_DECODER.decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|");
        if (values.length != 3) {
            throw new BusinessException(401, "登录状态无效，请重新登录");
        }

        long expiresAt = Long.parseLong(values[2]);
        if (expiresAt < Instant.now().getEpochSecond()) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        return new AuthUser(Integer.parseInt(values[0]), values[1]);
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return URL_ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Token signature failed", e);
        }
    }

    public record AuthUser(Integer id, String role) {
        public boolean isAdmin() {
            return "admin".equals(role);
        }
    }
}
