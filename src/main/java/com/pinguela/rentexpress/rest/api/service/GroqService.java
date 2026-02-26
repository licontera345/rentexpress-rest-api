package com.pinguela.rentexpress.rest.api.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpress.rest.api.dto.RecommendationRequestDTO;
import com.pinguela.rentexpress.rest.api.dto.RecommendationResponseDTO;
import com.pinguela.rentexpress.rest.api.dto.VehicleSummaryDTO;

public class GroqService {

    private static final Logger logger = Logger.getLogger(GroqService.class.getName());
    private static final Gson gson = new Gson();

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public GroqService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.apiKey = ConfigManager.getValue("groq.api.key");
        this.apiUrl = ConfigManager.getValue("groq.api.url");
        this.model = ConfigManager.getValue("groq.model");
    }

    public RecommendationResponseDTO recommend(RecommendationRequestDTO request) throws Exception {
        validateRequest(request);

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("temperature", 0.3);
        body.addProperty("max_tokens", 1024);

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messages.add(userMsg);

        body.add("messages", messages);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.log(Level.WARNING, "AI API returned status {0}", response.statusCode());
            throw new Exception("No se pudo obtener la recomendación de vehículos");
        }

        return parseAiResponse(response.body());
    }

    private void validateRequest(RecommendationRequestDTO request) {
        if (request.getDestination() == null || request.getPassengers() == null
                || request.getTripDuration() == null || request.getRoadCondition() == null) {
            throw new IllegalArgumentException("Todas las preferencias son obligatorias");
        }
        if (request.getVehicles() == null || request.getVehicles().isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un vehículo disponible");
        }
    }

    private String buildSystemPrompt() {
        return "Eres un experto asesor de alquiler de vehículos. "
                + "Tu tarea es recomendar los vehículos más adecuados de una lista disponible "
                + "según las necesidades del cliente. "
                + "DEBES responder ÚNICAMENTE con un JSON válido con esta estructura exacta:\n"
                + "{\"recommendedVehicleIds\": [1, 2, 3], \"explanation\": \"texto explicativo\"}\n"
                + "Selecciona entre 1 y 3 vehículos como máximo. "
                + "La explicación debe ser breve, en español, y justificar por qué cada vehículo es adecuado. "
                + "No incluyas texto fuera del JSON.";
    }

    private String buildUserPrompt(RecommendationRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Necesidades del cliente:\n");
        sb.append("- Destino: ").append(request.getDestination()).append("\n");
        sb.append("- Pasajeros: ").append(request.getPassengers()).append("\n");
        sb.append("- Duración del viaje: ").append(request.getTripDuration()).append("\n");
        sb.append("- Estado de las carreteras: ").append(request.getRoadCondition()).append("\n\n");
        sb.append("Vehículos disponibles:\n");

        for (VehicleSummaryDTO v : request.getVehicles()) {
            sb.append(String.format("- ID: %d | %s %s | Categoría: %s | Precio/día: %.2f€\n",
                    v.getVehicleId(), v.getBrand(), v.getModel(),
                    v.getCategoryName(), v.getDailyPrice()));
        }

        sb.append("\nRecomienda los vehículos más adecuados en formato JSON.");
        return sb.toString();
    }

    private RecommendationResponseDTO parseAiResponse(String responseJson) throws Exception {
        try {
            JsonObject root = JsonParser.parseString(responseJson).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            String content = choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString()
                    .trim();

            int jsonStart = content.indexOf('{');
            int jsonEnd = content.lastIndexOf('}');
            if (jsonStart == -1 || jsonEnd == -1) {
                throw new Exception("La IA no devolvió un JSON válido");
            }
            String jsonContent = content.substring(jsonStart, jsonEnd + 1);

            JsonObject parsed = JsonParser.parseString(jsonContent).getAsJsonObject();

            List<Integer> ids = new ArrayList<>();
            JsonArray idsArray = parsed.getAsJsonArray("recommendedVehicleIds");
            for (int i = 0; i < idsArray.size(); i++) {
                ids.add(idsArray.get(i).getAsInt());
            }

            String explanation = parsed.has("explanation")
                    ? parsed.get("explanation").getAsString()
                    : "Recomendación generada automáticamente";

            return new RecommendationResponseDTO(ids, explanation);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing AI response", e);
            throw new Exception("Error al procesar la recomendación");
        }
    }
}
