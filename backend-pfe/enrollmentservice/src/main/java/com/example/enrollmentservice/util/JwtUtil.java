package com.example.enrollmentservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {

    private Key secretKey;
    private final long expiration = 1000 * 60 * 60; // 1 hour

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        // ✅ Use plain bytes directly (NOT Base64-decoded!)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // ✅ Consistent with how JwtAuthFilter expects it


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Token invalid: " + e.getMessage());
            return false;
        }
    }
}
