package com.pinguela.rentexpress.rest.api.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JavaTimeParamConverterProvider implements ParamConverterProvider {

	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (LocalDate.class.equals(rawType)) {
			return (ParamConverter<T>) new LocalDateConverter();
		}
		if (LocalDateTime.class.equals(rawType)) {
			return (ParamConverter<T>) new LocalDateTimeConverter();
		}
		return null;
	}

	private static class LocalDateConverter implements ParamConverter<LocalDate> {
		@Override
		public LocalDate fromString(String value) {
			return value == null || value.isEmpty() ? null : LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
		}

		@Override
		public String toString(LocalDate value) {
			return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE);
		}
	}

	private static class LocalDateTimeConverter implements ParamConverter<LocalDateTime> {
		@Override
		public LocalDateTime fromString(String value) {
			return value == null || value.isEmpty() ? null
					: LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		}

		@Override
		public String toString(LocalDateTime value) {
			return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		}
	}
}
