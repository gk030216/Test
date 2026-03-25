package com.pet.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // JWT 密钥（生产环境建议从配置文件读取）
    private static final String SECRET = "pet-service-system-secret-key-2026-spring-boot-jwt-auth";

    // 过期时间：7天（毫秒）
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return JWT字符串
     */
    public static String generateToken(Integer userId, String username, Integer role) {
        SecretKey key = getSecretKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 Token
     * @param token JWT字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("Token已过期: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("不支持的Token: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Token格式错误: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("Token签名验证失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Token参数异常: " + e.getMessage());
        }
        return false;
    }

    /**
     * 从 Token 中获取用户ID
     */
    public static Integer getUserIdFromToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("username", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取角色
     */
    public static Integer getRoleFromToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取签名密钥
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取 Token 过期时间（毫秒）
     */
    public static long getExpireTime() {
        return EXPIRE_TIME;
    }
}