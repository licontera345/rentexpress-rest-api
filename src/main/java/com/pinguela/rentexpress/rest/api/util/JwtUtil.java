package com.pinguela.rentexpress.rest.api.util;

import java.util.Date;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private JwtUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateToken(String userId) {
        return generateTokenWithSubject(userId);
    }

    public static String generateUserToken(Integer userId) {
        return generateTokenWithSubject("USER:" + userId);
    }

    public static String generateEmployeeToken(Integer employeeId) {
        return generateTokenWithSubject("EMPLOYEE:" + employeeId);
    }

    /**
     * Genera un JWT para recuperación de contraseña (expiración configurable).
     * Subject: "RESET:userId"
     */
    public static String generatePasswordResetToken(Integer userId) {
        long expiryMs = JwtConfig.getResetExpirationSeconds() * 1000L;
        return Jwts.builder()
                .subject("RESET:" + userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(Keys.hmacShaKeyFor(JwtConfig.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * Valida un token de recuperación de contraseña y devuelve el userId, o null si no es válido.
     */
    public static Integer validatePasswordResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            String subject = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JwtConfig.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload()
                    .getSubject();
            if (subject != null && subject.startsWith("RESET:")) {
                return Integer.parseInt(subject.substring(6));
            }
        } catch (Exception ignored) {
            // token inválido o expirado
        }
        return null;
    }

    /**
     * Devuelve el hash SHA-256 en hexadecimal del token (para guardar en BD y comprobar uso único).
     */
    public static String sha256Hex(String token) {
        if (token == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String validateToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(JwtConfig.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private static String generateTokenWithSubject(String subject) {
        long expiryMs = JwtConfig.getExpirationSeconds() * 1000L;
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(Keys.hmacShaKeyFor(JwtConfig.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    /** Token temporal para completar 2FA (corta expiración, p. ej. 5 min). Subject: "2FA:" + userId */
    public static String generateTemp2FAToken(Integer userId) {
        if (userId == null) return null;
        long expiryMs = 5 * 60 * 1000L; // 5 minutos
        return Jwts.builder()
                .subject("2FA:" + userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(Keys.hmacShaKeyFor(JwtConfig.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    /** Valida token temporal 2FA y devuelve el userId, o null si no es válido. */
    public static Integer validateTemp2FAToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        try {
            String subject = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JwtConfig.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload()
                    .getSubject();
            if (subject != null && subject.startsWith("2FA:")) {
                return Integer.parseInt(subject.substring(4));
            }
        } catch (Exception ignored) {
            // token inválido o expirado
        }
        return null;
    }
}
