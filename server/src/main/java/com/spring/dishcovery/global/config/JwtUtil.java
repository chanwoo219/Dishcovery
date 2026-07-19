package com.spring.dishcovery.global.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtUtil {

    //JWT 생성 & 검증 유틸리티

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    private void init() {
        try {
            // HS512 needs a >=64 byte key; hashing the configured secret guarantees that
            // regardless of its length, and keeps the key stable across restarts
            // (a randomly generated key would invalidate every existing session on each restart).
            byte[] hashed = MessageDigest.getInstance("SHA-512").digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.key = Keys.hmacShaKeyFor(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String generateToken(String userId, String userName) {
        return generateToken(userId, userName, expirationMs);
    }

    // 자동 로그인 등 커스텀 만료시간이 필요한 경우
    public String generateToken(String userId, String userName, long customExpirationMs) {

        return Jwts.builder()
                .setSubject(userId)
                .claim("userName", userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + customExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // userId 추출
    public String getUserIdFromToken(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.getSubject();
    }

    // userName 추출
    public String getUserNameFromToken(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get("userName", String.class);
    }

    public boolean validateToken(String token) {
        return parse(token) != null;
    }

    private Claims parse(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
