package com.pinguela.rentexpress.rest.api.dto;

/**
 * Respuesta de 2FA setup: secreto para configurar en la app de autenticación (p. ej. Google Authenticator).
 */
public class TwoFactorSetupDTO {
    private String secret;

    public TwoFactorSetupDTO() {}
    public TwoFactorSetupDTO(String secret) { this.secret = secret; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
