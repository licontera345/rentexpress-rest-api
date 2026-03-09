package com.pinguela.rentexpress.rest.api.adapter.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.adapter.WeatherApiAdapter;
import com.pinguela.rentexpress.rest.api.adapter.WeatherApiException;
import com.pinguela.rentexpress.rest.api.util.AdapterConfigUtil;

/**
 * Implementación del adapter de la API del tiempo con timeouts configurados.
 * Por defecto: connect 10s, read 15s. Configurable vía weather.api.connect.timeout.seconds y weather.api.read.timeout.seconds.
 */
public class WeatherApiAdapterImpl implements WeatherApiAdapter {

    private static final Logger logger = Logger.getLogger(WeatherApiAdapterImpl.class.getName());
    private static final int DEFAULT_CONNECT_TIMEOUT_SEC = 10;
    private static final int DEFAULT_READ_TIMEOUT_SEC = 15;

    private final HttpClient httpClient;
    private final int readTimeoutSec;

    public WeatherApiAdapterImpl() {
        int connectSec = AdapterConfigUtil.getIntConfig("weather.api.connect.timeout.seconds", DEFAULT_CONNECT_TIMEOUT_SEC);
        int readSec = AdapterConfigUtil.getIntConfig("weather.api.read.timeout.seconds", DEFAULT_READ_TIMEOUT_SEC);
        this.readTimeoutSec = readSec;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectSec))
                .build();
    }

    @Override
    public String fetch(String url) throws WeatherApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(readTimeoutSec))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) {
                logger.log(Level.WARNING, "Weather API returned 5xx: {0}", response.statusCode());
                throw new WeatherApiException("Servicio de clima no disponible");
            }
            if (response.statusCode() != 200) {
                logger.log(Level.WARNING, "Weather API returned status {0}", response.statusCode());
                throw new WeatherApiException("No se pudo obtener el clima");
            }
            return response.body();
        } catch (WeatherApiException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Weather API request failed: " + e.getMessage());
            throw new WeatherApiException("Servicio de clima no disponible", e);
        }
    }
}
