package com.pinguela.rentexpress.rest.api.dto;

import java.util.Collections;
import java.util.Map;

/**
 * Cuerpo de respuesta de error (M1/M2). La API devuelve code + message para que el cliente pueda mapear.
 * Opcionalmente fieldErrors para validación por campo (clave = nombre del campo, valor = mensaje).
 */
public class ErrorResponseDTO {

    private String code;
    private String message;
    private Map<String, String> fieldErrors;

    public ErrorResponseDTO() {}

    public ErrorResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
        this.fieldErrors = null;
    }

    public ErrorResponseDTO(String code, String message, Map<String, String> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors == null || fieldErrors.isEmpty() ? null : Collections.unmodifiableMap(fieldErrors);
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors == null || fieldErrors.isEmpty() ? null : Collections.unmodifiableMap(fieldErrors);
    }
}
