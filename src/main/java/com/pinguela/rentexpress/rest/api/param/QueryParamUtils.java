package com.pinguela.rentexpress.rest.api.param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class QueryParamUtils {

    private static final Pattern OFFSET_PATTERN = Pattern.compile(".*T.*[+-]\\d{2}:?\\d{2}$");

    private QueryParamUtils() {
    }

    public static LocalDateTime parseDateTime(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            if (trimmed.endsWith("Z") || OFFSET_PATTERN.matcher(trimmed).matches()) {
                return OffsetDateTime.parse(trimmed, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
            }
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(String.format("Invalid date-time format for %s", fieldName), ex);
        }
    }

    public static LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(String.format("Invalid date format for %s", fieldName), ex);
        }
    }
}
