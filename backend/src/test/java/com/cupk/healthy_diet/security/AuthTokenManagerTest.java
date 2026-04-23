package com.cupk.healthy_diet.security;

import com.cupk.healthy_diet.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTokenManagerTest {

    @Test
    void createAndParseToken() {
        AuthTokenManager manager = newManager(60);

        String token = manager.createToken(12, "admin");
        AuthTokenManager.AuthUser user = manager.parseToken(token);

        assertEquals(12, user.id());
        assertEquals("admin", user.role());
    }

    @Test
    void rejectsTamperedToken() {
        AuthTokenManager manager = newManager(60);
        String token = manager.createToken(12, "user");

        assertThrows(BusinessException.class, () -> manager.parseToken(token + "x"));
    }

    @Test
    void rejectsExpiredToken() {
        AuthTokenManager manager = newManager(-1);
        String token = manager.createToken(12, "user");

        assertThrows(BusinessException.class, () -> manager.parseToken(token));
    }

    private AuthTokenManager newManager(long expireSeconds) {
        AuthTokenManager manager = new AuthTokenManager();
        ReflectionTestUtils.setField(manager, "tokenSecret", "test-secret");
        ReflectionTestUtils.setField(manager, "expireSeconds", expireSeconds);
        return manager;
    }
}
