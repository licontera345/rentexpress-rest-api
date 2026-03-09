package com.pinguela.rentexpress.rest.api.dto;

public class Confirm2FARequestDTO {
    private String secret;
    private String code;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
