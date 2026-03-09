package com.pinguela.rentexpress.rest.api.util;

import com.pinguela.rentexpres.config.ConfigManager;

/**
 * Configuración JWT externalizada: variable de entorno → system property → config.properties.
 * Nunca hardcodear secretos en código.
 */
public final class JwtConfig {

    private static final int MIN_SECRET_LENGTH = 32;

    private static volatile String secret;
    private static volatile Long expirationSeconds;
    private static volatile Long resetExpirationSeconds;

    private JwtConfig() {}

    /**
     * Clave secreta para firmar JWT (mínimo 32 caracteres para HS256).
     * Origen: JWT_SECRET → rentexpress.jwt.secret → config.properties jwt.secret.
     */
    public static String getSecret() {
        if (secret == null) {
            synchronized (JwtConfig.class) {
                if (secret == null) {
                    String v = System.getenv("JWT_SECRET");
                    if (v == null || v.trim().isEmpty()) {
                        v = System.getProperty("rentexpress.jwt.secret");
                    }
                    if (v == null || v.trim().isEmpty()) {
                        try {
                            v = ConfigManager.getValue("jwt.secret");
                        } catch (Throwable t) {
                            // ConfigManager puede no estar inicializado
                        }
                    }
                    if (v == null || v.trim().isEmpty() || v.length() < MIN_SECRET_LENGTH) {
                        throw new IllegalStateException(
                                "JWT secret must be configured (JWT_SECRET, rentexpress.jwt.secret or jwt.secret in config.properties) and at least " + MIN_SECRET_LENGTH + " characters.");
                    }
                    secret = v.trim();
                }
            }
        }
        return secret;
    }

    /** Expiración del token de sesión en segundos. Por defecto 3600. */
    public static long getExpirationSeconds() {
        if (expirationSeconds == null) {
            synchronized (JwtConfig.class) {
                if (expirationSeconds == null) {
                    String v = System.getenv("JWT_EXPIRATION_SECONDS");
                    if (v == null || v.trim().isEmpty()) {
                        v = System.getProperty("rentexpress.jwt.expiration.seconds");
                    }
                    if (v == null || v.trim().isEmpty()) {
                        try {
                            v = ConfigManager.getValue("jwt.expiration.seconds");
                        } catch (Throwable t) {
                            // ignore
                        }
                    }
                    expirationSeconds = parsePositiveLong(v, 3600L);
                }
            }
        }
        return expirationSeconds;
    }

    /** Expiración del token de recuperación de contraseña en segundos. Por defecto 3600. */
    public static long getResetExpirationSeconds() {
        if (resetExpirationSeconds == null) {
            synchronized (JwtConfig.class) {
                if (resetExpirationSeconds == null) {
                    String v = System.getenv("JWT_RESET_EXPIRATION_SECONDS");
                    if (v == null || v.trim().isEmpty()) {
                        v = System.getProperty("rentexpress.jwt.reset.expiration.seconds");
                    }
                    if (v == null || v.trim().isEmpty()) {
                        try {
                            v = ConfigManager.getValue("jwt.reset.expiration.seconds");
                        } catch (Throwable t) {
                            // ignore
                        }
                    }
                    resetExpirationSeconds = parsePositiveLong(v, 3600L);
                }
            }
        }
        return resetExpirationSeconds;
    }

    private static long parsePositiveLong(String v, long defaultValue) {
        if (v == null || v.trim().isEmpty()) return defaultValue;
        try {
            long n = Long.parseLong(v.trim());
            return n > 0 ? n : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
