package com.spring.dishcovery.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    //JWT 생성 & 검증 유틸리티

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    /*
        private final Key key;

        public JwtUtil(@Value("${jwt.secret}") String secret) {
            // 외부에서 읽은 문자열 secret → Key 객체로 변환
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    */
    public String generateToken(String userId, String userName) {

        return Jwts.builder()
                .setSubject(userId)
                .claim("userName", userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                //.signWith(SignatureAlgorithm.HS512, secretKey)
                .signWith(key, SignatureAlgorithm.HS512) //서명을 만듬
                .compact();
    }

    // userId 추출
    public String getUserIdFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // userName 추출
    public String getUserNameFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userName", String.class);
    }

    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // JWT 생성할 때 사용한 비밀키
                    .build()
                    .parseClaimsJws(token); // 여기서 서명 검증 + 만료 확인
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
