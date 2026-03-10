package com.pinguela.rentexpress.rest.api.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.ErrorCode;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Captura cualquier excepción no manejada por otros mappers.
 * Devuelve 500 con ErrorResponseDTO genérico; nunca expone stack traces al cliente.
 * El detalle se registra solo en el servidor para depuración.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = Logger.getLogger(GenericExceptionMapper.class.getName());
    private static final String INTERNAL_MESSAGE = "An unexpected error occurred";

    @Override
    public Response toResponse(Throwable throwable) {
        // Log exhaustivo en servidor; nunca en la respuesta al cliente
        logger.log(Level.SEVERE, "Unhandled exception: " + throwable.getMessage(), throwable);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO(ErrorCode.INTERNAL.name(), INTERNAL_MESSAGE))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
