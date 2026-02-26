package com.pinguela.rentexpress.rest.api.dto;

/**
 * Cuerpo de respuesta de error (M1/M2). La API devuelve code + message para que el cliente pueda mapear.
 */
public class ErrorResponseDTO {

    private String code;
    private String message;

    public ErrorResponseDTO() {}

    public ErrorResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
