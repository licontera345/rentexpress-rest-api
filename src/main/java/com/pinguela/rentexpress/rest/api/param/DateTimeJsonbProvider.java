package com.pinguela.rentexpress.rest.api.param;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/*
 * TimeDateUtil actúa como proveedor global (@Provider) para JSON-B.
 * Registra un adaptador personalizado para convertir cadenas ISO 8601
 * con o sin zona horaria a LocalDateTime, evitando errores de parseo
 * cuando el JSON incluye "Z" u offsets como "+01:00".
 */
@Provider
public class DateTimeJsonbProvider implements ContextResolver<Jsonb> {

    // Instancia de Jsonb configurada con el adaptador personalizado
    private final Jsonb jsonb;

    public DateTimeJsonbProvider() {

        /*
         * JsonbConfig: configuración de JSON-B.
         * Registramos el adaptador LocalDateTimeAdapter, que controla
         * cómo se leen y escriben fechas LocalDateTime desde/hacia JSON.
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

    /*
     * Adaptador LocalDateTimeAdapter:
     * Controla cómo JSON-B convierte LocalDateTime <-> String.
     *
     * Problema original:
     * LocalDateTime NO soporta formatos con zona horaria (Z, +01:00...).
     *
     * Solución:
     * Detectar si la cadena incluye Z u offset.
     * - Si incluye zona/offset → parseo con OffsetDateTime y luego paso a LocalDateTime.
     * - Si no incluye offset → parseo directo como LocalDateTime.
     */
    public static class LocalDateTimeAdapter implements JsonbAdapter<LocalDateTime, String> {

        /*
         * adaptToJson:
         * Convierte LocalDateTime en String para enviarlo en JSON.
         * Se usa el formato ISO estándar.
         */
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
        @Override
        public LocalDateTime adaptFromJson(String value) {

            // Si la cadena está vacía o es nula, devolvemos null directamente
            if (value == null || value.isEmpty()) return null;

            /*
             * Si la fecha contiene:
             * - "Z" (UTC)
             * - "+" (offset positivo)
             * - "-" (offset negativo, evitando confusión con la fecha misma)
             * entonces utilizamos OffsetDateTime.
             */
            if (value.endsWith("Z") || value.contains("+") || value.contains("-")) {
                OffsetDateTime odt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                return odt.toLocalDateTime();
            }

            /*
             * Si no trae zona horaria ni offset, es LocalDateTime puro.
             * Se parsea con el formato ISO_LOCAL_DATE_TIME.
             */
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
