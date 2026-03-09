package com.pinguela.rentexpress.rest.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpress.rest.api.adapter.GroqApiAdapter;
import com.pinguela.rentexpress.rest.api.adapter.GroqApiException;
import com.pinguela.rentexpress.rest.api.dto.RecommendationRequestDTO;
import com.pinguela.rentexpress.rest.api.dto.RecommendationResponseDTO;
import com.pinguela.rentexpress.rest.api.dto.VehicleSummaryDTO;

public class GroqService {

    private static final Logger logger = Logger.getLogger(GroqService.class.getName());
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_BACKOFF_MS = 1000;
    private static final String GENERIC_ERROR_MESSAGE = "No se pudo obtener la recomendación de vehículos";
    private static final String PARSE_ERROR_MESSAGE = "Error al procesar la recomendación";

    private final GroqApiAdapter groqApiAdapter;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public GroqService(GroqApiAdapter groqApiAdapter) {
        this.groqApiAdapter = groqApiAdapter;
        this.apiKey = getConfigOrNull("groq.api.key");
        this.apiUrl = getConfigOrNull("groq.api.url");
        this.model = getConfigOrNull("groq.model");
    }

    private static String getConfigOrNull(String key) {
        try {
            return ConfigManager.getValue(key);
        } catch (Throwable t) {
            return null;
        }
    }

    public RecommendationResponseDTO recommend(RecommendationRequestDTO request) throws GroqApiException {
        validateRequest(request);
        if (apiKey == null || apiKey.isBlank() || apiUrl == null || apiUrl.isBlank() || model == null || model.isBlank()) {
            throw new IllegalStateException("Groq API no está configurada");
        }

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

        GroqApiException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String responseBody = groqApiAdapter.post(apiUrl, body.toString(), apiKey);
                return parseAiResponse(responseBody);
            } catch (GroqApiException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    int backoffMs = INITIAL_BACKOFF_MS * attempt;
                    logger.log(Level.FINE, "Groq request failed (attempt {0}), retrying in {1} ms", new Object[]{ attempt, backoffMs });
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new GroqApiException(GENERIC_ERROR_MESSAGE, ie);
                    }
                }
            }
        }
        throw lastException != null ? lastException : new GroqApiException(GENERIC_ERROR_MESSAGE);
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

    /**
     * Parsea la respuesta de la IA de forma segura. No expone detalles internos al cliente.
     * Solo se registra en log del servidor (nunca stack en respuesta).
     */
    private RecommendationResponseDTO parseAiResponse(String responseJson) throws GroqApiException {
        if (responseJson == null || responseJson.isBlank()) {
            logger.warning("AI response was null or empty");
            throw new GroqApiException(PARSE_ERROR_MESSAGE);
        }
        try {
            JsonElement rootElement = JsonParser.parseString(responseJson);
            if (rootElement == null || !rootElement.isJsonObject()) {
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            JsonObject root = rootElement.getAsJsonObject();
            if (!root.has("choices") || !root.get("choices").isJsonArray()) {
                logger.warning("AI response had no choices");
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices.isEmpty()) {
                logger.warning("AI response had empty choices");
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            JsonElement firstEl = choices.get(0);
            if (firstEl == null || !firstEl.isJsonObject()) {
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            JsonObject firstChoice = firstEl.getAsJsonObject();
            if (!firstChoice.has("message") || !firstChoice.get("message").isJsonObject()) {
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            JsonObject message = firstChoice.getAsJsonObject("message");
            JsonElement contentEl = message.get("content");
            String content = (contentEl != null && contentEl.isJsonPrimitive()) ? contentEl.getAsString() : null;
            if (content == null) content = "";
            content = content.trim();

            int jsonStart = content.indexOf('{');
            int jsonEnd = content.lastIndexOf('}');
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                logger.warning("AI response content had no JSON object");
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            String jsonContent = content.substring(jsonStart, jsonEnd + 1);
            JsonElement parsedElement = JsonParser.parseString(jsonContent);
            if (parsedElement == null || !parsedElement.isJsonObject()) {
                throw new GroqApiException(PARSE_ERROR_MESSAGE);
            }
            JsonObject parsed = parsedElement.getAsJsonObject();

            List<Integer> ids = new ArrayList<>();
            if (parsed.has("recommendedVehicleIds") && parsed.get("recommendedVehicleIds").isJsonArray()) {
                JsonArray idsArray = parsed.getAsJsonArray("recommendedVehicleIds");
                for (int i = 0; i < idsArray.size(); i++) {
                    JsonElement el = idsArray.get(i);
                    if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                        ids.add(el.getAsInt());
                    }
                }
            }

            String explanation = "Recomendación generada automáticamente";
            if (parsed.has("explanation")) {
                JsonElement explEl = parsed.get("explanation");
                if (explEl != null && explEl.isJsonPrimitive()) {
                    String ex = explEl.getAsString();
                    if (ex != null && !ex.isEmpty()) explanation = ex;
                }
            }

            return new RecommendationResponseDTO(ids, explanation);

        } catch (GroqApiException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing AI response: " + e.getMessage());
            throw new GroqApiException(PARSE_ERROR_MESSAGE);
        }
    }
}
