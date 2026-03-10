package com.pinguela.rentexpress.rest.api.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.adapter.GroqApiException;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapea GroqApiException a 503 Service Unavailable con ErrorResponseDTO.
 * No expone el mensaje interno al cliente; solo mensaje genérico para degradación elegante.
 */
@Provider
public class GroqApiExceptionMapper implements ExceptionMapper<GroqApiException> {

    private static final Logger logger = Logger.getLogger(GroqApiExceptionMapper.class.getName());
    private static final String CLIENT_MESSAGE = "Recommendation service temporarily unavailable";

    @Override
    public Response toResponse(GroqApiException e) {
        logger.log(Level.WARNING, "GroqApiException (503): " + e.getMessage(), e);
        return Response.status(Status.SERVICE_UNAVAILABLE)
                .entity(new ErrorResponseDTO("SERVICE_UNAVAILABLE", CLIENT_MESSAGE))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
