package com.pinguela.rentexpress.rest.api.adapter;

/**
 * Excepción de dominio para fallos de la API Groq (timeout, 5xx, red, parseo).
 * No expone detalles internos al cliente.
 */
public class GroqApiException extends Exception {

    public GroqApiException(String message) {
        super(message);
    }

    public GroqApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
