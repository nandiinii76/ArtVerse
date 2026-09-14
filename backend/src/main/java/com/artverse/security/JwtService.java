package com.artverse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;

    public JwtService(
            @Value("${artverse.jwt.secret}") String secret,
            @Value("${artverse.jwt.access-ttl-minutes:15}") long accessTtlMinutes,
            @Value("${artverse.jwt.refresh-ttl-days:30}") long refreshTtlDays
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMs = accessTtlMinutes * 60_000;
        this.refreshTokenTtlMs = refreshTtlDays * 24 * 60 * 60_000;
    }

    public String generateAccessToken(UserDetails user) {
        return buildToken(user, accessTokenTtlMs, "access");
    }

    public String generateRefreshToken(UserDetails user) {
        return buildToken(user, refreshTokenTtlMs, "refresh");
    }

    private String buildToken(UserDetails user, long ttlMs, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public boolean isTokenValid(String token, UserDetails user) {
        try {
            String email = extractEmail(token);
            return email.equalsIgnoreCase(user.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
