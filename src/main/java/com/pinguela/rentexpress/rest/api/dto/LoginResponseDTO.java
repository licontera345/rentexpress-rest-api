package com.pinguela.rentexpress.rest.api.dto;

/**
 * Respuesta de login: o bien token de sesión, o bien indicación de que se requiere 2FA con token temporal.
 */
public class LoginResponseDTO {

    private String token;
    private Boolean requiresTwoFactor;
    private String tempToken;

    public LoginResponseDTO() {}

    public static LoginResponseDTO withToken(String token) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setToken(token);
        dto.setRequiresTwoFactor(false);
        return dto;
    }

    public static LoginResponseDTO withRequiresTwoFactor(String tempToken) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setRequiresTwoFactor(true);
        dto.setTempToken(tempToken);
        return dto;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Boolean getRequiresTwoFactor() { return requiresTwoFactor; }
    public void setRequiresTwoFactor(Boolean requiresTwoFactor) { this.requiresTwoFactor = requiresTwoFactor; }
    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }
}
