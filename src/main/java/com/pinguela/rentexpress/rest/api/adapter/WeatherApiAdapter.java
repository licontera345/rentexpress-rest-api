package com.pinguela.rentexpress.rest.api.adapter;

/**
 * Interfaz para la llamada HTTP a la API del tiempo.
 * La implementación debe usar timeouts configurados y mapear errores a WeatherApiException.
 */
public interface WeatherApiAdapter {

    /**
     * Realiza una petición GET a la URL dada.
     * @param url URL completa con parámetros
     * @return cuerpo de la respuesta en texto
     * @throws WeatherApiException si hay timeout, 5xx, error de red o respuesta no OK
     */
    String fetch(String url) throws WeatherApiException;
}
