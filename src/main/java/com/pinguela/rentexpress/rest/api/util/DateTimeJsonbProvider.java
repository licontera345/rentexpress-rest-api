package com.pinguela.rentexpress.rest.api.util;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;


@Provider
public class DateTimeJsonbProvider implements ContextResolver<Jsonb> {

    private final Jsonb jsonb;

    public DateTimeJsonbProvider() {

        /*
         * leen y escriben fechas LocalDateTime desde/hacia JSON.
         */
        JsonbConfig config = new JsonbConfig()
                .withAdapters(new LocalDateTimeAdapter());

        // Construcción del motor JSON-B con la configuración personalizada
        this.jsonb = JsonbBuilder.create(config);
    }

    /*
     * getContext: método obligatorio del ContextResolver.
     * Jersey lo llama automáticamente para obtener el Jsonb configurado.
     */
    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }

   
    public static class LocalDateTimeAdapter implements JsonbAdapter<LocalDateTime, String> {

        @Override
        public String adaptToJson(LocalDateTime value) {
            return value == null ? null : value.toString();
        }

        /*
         * adaptFromJson:
         * Convierte una cadena JSON en un LocalDateTime.
         *
         * Soporta:
         * - "2025-11-17T10:20:30"
         * - "2025-11-17T10:20:30.123"
         * - "2025-11-17T10:20:30Z"
         * - "2025-11-17T10:20:30.123Z"
         * - "2025-11-17T10:20:30+01:00"
         * - "2025-11-17T10:20:30.123+01:00"
         */
        private static final Pattern OFFSET_PATTERN = Pattern.compile(".*T.*[+-]\\d{2}:?\\d{2}$");

        @Override
        public LocalDateTime adaptFromJson(String value) {

            // Si la cadena está vacía o es nula, devolvemos null directamente
            if (value == null || value.isEmpty()) return null;

            String trimmedValue = value.trim();

            /*
             * Si la fecha contiene:
             * - "Z" (UTC)
             * - "±HH:mm" o "±HHmm" DESPUÉS de la 'T' (offset)
             * entonces utilizamos OffsetDateTime.
             */
            if (trimmedValue.endsWith("Z") || OFFSET_PATTERN.matcher(trimmedValue).matches()) {
                OffsetDateTime odt = OffsetDateTime.parse(trimmedValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                return odt.toLocalDateTime();
            }

            /*
             * Si no trae zona horaria ni offset, es LocalDateTime puro.
             * Se parsea con el formato ISO_LOCAL_DATE_TIME.
             */
            return LocalDateTime.parse(trimmedValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
