package com.pinguela.rentexpress.rest.api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpress.rest.api.adapter.WeatherApiAdapter;
import com.pinguela.rentexpress.rest.api.adapter.WeatherApiException;
import com.pinguela.rentexpress.rest.api.dto.WeatherDTO;

/**
 * Servicio de clima. Usa WeatherApiAdapter inyectado con timeouts configurados.
 * En caso de fallo (timeout, 5xx) el adapter lanza WeatherApiException; el Resource
 * puede devolver respuesta controlada (graceful degradation) sin propagar 500.
 */
public class WeatherService {

    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    private final WeatherApiAdapter weatherApiAdapter;
    private final String apiKey;
    private final String apiUrl;

    public WeatherService(WeatherApiAdapter weatherApiAdapter) {
        this.weatherApiAdapter = weatherApiAdapter;
        this.apiKey = ConfigManager.getValue("weather.api.key");
        this.apiUrl = ConfigManager.getValue("weather.api.url");
    }

    /**
     * Obtiene el clima para la ciudad. Lanza IllegalArgumentException si city es nulo/vacío.
     * Lanza WeatherApiException si la API externa falla (timeout, 5xx, red).
     */
    public WeatherDTO getWeatherByCity(String city, String lang) throws WeatherApiException {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name is required");
        }

        String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
        String effectiveLang = (lang != null && !lang.trim().isEmpty()) ? lang.trim() : "es";

        String url = String.format("%s?q=%s&units=metric&lang=%s&appid=%s",
                apiUrl, encodedCity, effectiveLang, apiKey);

        String json = weatherApiAdapter.fetch(url);
        return parseResponse(json, city.trim());
    }

    private WeatherDTO parseResponse(String json, String requestedCity) throws WeatherApiException {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String cityName = root.has("name") ? root.get("name").getAsString() : requestedCity;

            if (!root.has("main") || !root.get("main").isJsonObject()) {
                logger.warning("Weather API response missing main object");
                throw new WeatherApiException("No se pudo obtener el clima");
            }
            JsonObject main = root.getAsJsonObject("main");
            double temp = main.has("temp") ? main.get("temp").getAsDouble() : 0;
            double tempMin = main.has("temp_min") ? main.get("temp_min").getAsDouble() : temp;
            double tempMax = main.has("temp_max") ? main.get("temp_max").getAsDouble() : temp;
            int humidity = main.has("humidity") ? main.get("humidity").getAsInt() : 0;

            String description = "";
            String iconCode = "";
            if (root.has("weather") && root.get("weather").isJsonArray() && root.getAsJsonArray("weather").size() > 0) {
                JsonObject weatherObj = root.getAsJsonArray("weather").get(0).getAsJsonObject();
                description = weatherObj.has("description") ? weatherObj.get("description").getAsString() : "";
                iconCode = weatherObj.has("icon") ? weatherObj.get("icon").getAsString() : "";
            }

            return new WeatherDTO(cityName, temp, tempMin, tempMax, humidity, description, iconCode);
        } catch (WeatherApiException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing weather response", e);
            throw new WeatherApiException("No se pudo obtener el clima", e);
        }
    }
}
