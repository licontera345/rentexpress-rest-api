package com.pinguela.rentexpress.rest.api.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JavaTimeParamConverterProvider implements ParamConverterProvider {

        private static final DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
        private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        private static final Pattern OFFSET_PATTERN = Pattern.compile(".*T.*[+-]\\d{2}:?\\d{2}$");

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
                if (LocalDate.class.equals(rawType)) {
                        return (ParamConverter<T>) new LocalDateParamConverter();
                }
                if (LocalDateTime.class.equals(rawType)) {
                        return (ParamConverter<T>) new LocalDateTimeParamConverter();
                }
                return null;
        }

        private static class LocalDateParamConverter implements ParamConverter<LocalDate> {
                @Override
                public LocalDate fromString(String value) {
                        if (value == null) {
                                return null;
                        }
                        String trimmed = value.trim();
                        return trimmed.isEmpty() ? null : LocalDate.parse(trimmed, ISO_LOCAL_DATE);
                }

                @Override
                public String toString(LocalDate value) {
                        return value == null ? null : value.format(ISO_LOCAL_DATE);
                }
        }

        private static class LocalDateTimeParamConverter implements ParamConverter<LocalDateTime> {
                @Override
                public LocalDateTime fromString(String value) {
                        if (value == null) {
                                return null;
                        }

                        String trimmed = value.trim();
                        if (trimmed.isEmpty()) {
                                return null;
                        }

                        if (trimmed.endsWith("Z") || OFFSET_PATTERN.matcher(trimmed).matches()) {
                                return OffsetDateTime.parse(trimmed, ISO_OFFSET_DATE_TIME).toLocalDateTime();
                        }

                        return LocalDateTime.parse(trimmed, ISO_LOCAL_DATE_TIME);
                }

                @Override
                public String toString(LocalDateTime value) {
                        return value == null ? null : value.format(ISO_LOCAL_DATE_TIME);
                }
        }
}
