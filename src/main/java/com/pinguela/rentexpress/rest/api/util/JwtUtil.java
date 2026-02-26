package com.pinguela.rentexpress.rest.api.util;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String SECRET_KEY = "oi48594hrgwe452934nforitertlerqo8q344";
    private static final long RESET_TOKEN_EXPIRY_MS = 3600 * 1000L; // 1 hora

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
     * Genera un JWT para recuperación de contraseña (válido 1 hora).
     * Subject: "RESET:userId"
     */
    public static String generatePasswordResetToken(Integer userId) {
        return Jwts.builder()
                .subject("RESET:" + userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + RESET_TOKEN_EXPIRY_MS))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
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
                    .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
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

    public static String validateToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private static String generateTokenWithSubject(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }
}
