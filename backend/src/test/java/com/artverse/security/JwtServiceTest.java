package com.artverse.security;

import com.artverse.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "artverse-test-secret-key-with-at-least-32-characters";

    @Test
    void accessAndRefreshTokensContainCorrectTypes() {
        JwtService service = new JwtService(SECRET, 15, 30);
        User user = new User();
        user.setEmail("nandi@example.com");

        String access = service.generateAccessToken(user);
        String refresh = service.generateRefreshToken(user);

        assertEquals("nandi@example.com", service.extractEmail(access));
        assertEquals("access", service.extractTokenType(access));
        assertEquals("refresh", service.extractTokenType(refresh));
        assertTrue(service.isTokenValid(access, user));
        assertTrue(service.isTokenValid(refresh, user));
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        JwtService service = new JwtService(SECRET, 15, 30);
        User first = new User();
        first.setEmail("first@example.com");
        User second = new User();
        second.setEmail("second@example.com");

        String token = service.generateAccessToken(first);

        assertFalse(service.isTokenValid(token, second));
    }
}
