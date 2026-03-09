package com.pinguela.rentexpress.rest.api.adapter;

/**
 * Excepción de dominio para fallos de la API del tiempo (timeout, 5xx, red).
 * No expone detalles internos al cliente.
 */
public class WeatherApiException extends Exception {

    public WeatherApiException(String message) {
        super(message);
    }

    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
