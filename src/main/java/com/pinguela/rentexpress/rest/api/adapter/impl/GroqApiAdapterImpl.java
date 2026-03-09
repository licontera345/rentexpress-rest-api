package com.pinguela.rentexpress.rest.api.adapter.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.adapter.GroqApiAdapter;
import com.pinguela.rentexpress.rest.api.adapter.GroqApiException;
import com.pinguela.rentexpress.rest.api.util.AdapterConfigUtil;

/**
 * Implementación del adapter de la API Groq con timeouts configurados.
 * Por defecto: connect 10s, read 15s. Configurable vía groq.api.connect.timeout.seconds y groq.api.read.timeout.seconds.
 */
public class GroqApiAdapterImpl implements GroqApiAdapter {

    private static final Logger logger = Logger.getLogger(GroqApiAdapterImpl.class.getName());
    private static final int DEFAULT_CONNECT_TIMEOUT_SEC = 10;
    private static final int DEFAULT_READ_TIMEOUT_SEC = 15;

    private final HttpClient httpClient;
    private final int readTimeoutSec;

    public GroqApiAdapterImpl() {
        int connectSec = AdapterConfigUtil.getIntConfig("groq.api.connect.timeout.seconds", DEFAULT_CONNECT_TIMEOUT_SEC);
        int readSec = AdapterConfigUtil.getIntConfig("groq.api.read.timeout.seconds", DEFAULT_READ_TIMEOUT_SEC);
        this.readTimeoutSec = readSec;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectSec))
                .build();
    }

    @Override
    public String post(String url, String jsonBody, String apiKey) throws GroqApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(readTimeoutSec))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + (apiKey != null ? apiKey : ""))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : "{}"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) {
                logger.log(Level.WARNING, "Groq API returned 5xx: {0}", response.statusCode());
                throw new GroqApiException("Servicio no disponible");
            }
            if (response.statusCode() != 200) {
                logger.log(Level.WARNING, "Groq API returned status {0}", response.statusCode());
                throw new GroqApiException("No se pudo obtener la recomendación");
            }
            return response.body();
        } catch (GroqApiException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Groq API request failed: " + e.getMessage());
            throw new GroqApiException("Servicio no disponible", e);
        }
    }
}
