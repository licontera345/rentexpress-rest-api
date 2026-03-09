package com.pinguela.rentexpress.rest.api.adapter;

/**
 * Interfaz para la llamada HTTP a la API Groq.
 * La implementación debe usar timeouts configurados y mapear errores a GroqApiException.
 */
public interface GroqApiAdapter {

    /**
     * Envía una petición POST con cuerpo JSON.
     * @param url URL del endpoint
     * @param jsonBody cuerpo en JSON
     * @param apiKey Bearer token
     * @return cuerpo de la respuesta en texto
     * @throws GroqApiException si hay timeout, 5xx, error de red o respuesta no OK
     */
    String post(String url, String jsonBody, String apiKey) throws GroqApiException;
}
