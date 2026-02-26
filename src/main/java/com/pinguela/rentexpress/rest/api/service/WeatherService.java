package com.pinguela.rentexpress.rest.api.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpress.rest.api.dto.WeatherDTO;

public class WeatherService {

    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;

    public WeatherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.apiKey = ConfigManager.getValue("weather.api.key");
        this.apiUrl = ConfigManager.getValue("weather.api.url");
    }

    public WeatherDTO getWeatherByCity(String city, String lang) throws Exception {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name is required");
        }

        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String effectiveLang = (lang != null && !lang.trim().isEmpty()) ? lang : "es";

        String url = String.format("%s?q=%s&units=metric&lang=%s&appid=%s",
                apiUrl, encodedCity, effectiveLang, apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.log(Level.WARNING, "Weather API returned status {0} for city {1}",
                    new Object[]{response.statusCode(), city});
            throw new Exception("No se pudo obtener el clima para la ciudad: " + city);
        }

        return parseResponse(response.body(), city);
    }

    private WeatherDTO parseResponse(String json, String requestedCity) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        String cityName = root.has("name") ? root.get("name").getAsString() : requestedCity;

        JsonObject main = root.getAsJsonObject("main");
        double temp = main.get("temp").getAsDouble();
        double tempMin = main.get("temp_min").getAsDouble();
        double tempMax = main.get("temp_max").getAsDouble();
        int humidity = main.get("humidity").getAsInt();

        JsonObject weatherObj = root.getAsJsonArray("weather").get(0).getAsJsonObject();
        String description = weatherObj.get("description").getAsString();
        String iconCode = weatherObj.get("icon").getAsString();

        return new WeatherDTO(cityName, temp, tempMin, tempMax, humidity, description, iconCode);
    }
}
