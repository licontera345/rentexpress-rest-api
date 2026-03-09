package com.pinguela.rentexpress.rest.api.dto;

public class Verify2FARequestDTO {
    private String tempToken;
    private String code;

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
