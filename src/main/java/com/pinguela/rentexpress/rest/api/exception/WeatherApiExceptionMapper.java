package com.pinguela.rentexpress.rest.api.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.adapter.WeatherApiException;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapea WeatherApiException a 503 Service Unavailable con ErrorResponseDTO.
 * No expone el mensaje interno al cliente; solo mensaje genérico para degradación elegante.
 */
@Provider
public class WeatherApiExceptionMapper implements ExceptionMapper<WeatherApiException> {

    private static final Logger logger = Logger.getLogger(WeatherApiExceptionMapper.class.getName());
    private static final String CLIENT_MESSAGE = "Weather service temporarily unavailable";

    @Override
    public Response toResponse(WeatherApiException e) {
        logger.log(Level.WARNING, "WeatherApiException (503): " + e.getMessage(), e);
        return Response.status(Status.SERVICE_UNAVAILABLE)
                .entity(new ErrorResponseDTO("SERVICE_UNAVAILABLE", CLIENT_MESSAGE))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
