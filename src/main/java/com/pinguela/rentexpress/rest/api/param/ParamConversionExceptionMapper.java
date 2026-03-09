package com.pinguela.rentexpress.rest.api.param;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.ErrorCode;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.WebApplicationException;

/**
 * Maneja errores de conversión de parámetros (PathParam, QueryParam).
 * Devuelve 400 con ErrorResponseDTO; para otras excepciones devuelve 500 sin exponer stack al cliente.
 */
@Provider
public class ParamConversionExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = Logger.getLogger(ParamConversionExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        String message = exception.getMessage();

        if (exception instanceof NumberFormatException ||
            (message != null && message.contains("NumberFormatException"))) {
            logger.warning("Invalid numeric parameter: " + message);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(ErrorCode.BAD_REQUEST.name(), "Invalid parameter format. Numeric value expected."))
                    .type(MediaType.APPLICATION_JSON).build();
        }

        if (exception instanceof IllegalArgumentException ||
            (message != null && (message.contains("convert") || message.contains("parse")))) {
            logger.warning("Parameter conversion error: " + message);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(ErrorCode.BAD_REQUEST.name(), "Invalid parameter value."))
                    .type(MediaType.APPLICATION_JSON).build();
        }

        logger.log(Level.SEVERE, "Unhandled exception in param mapper: " + exception.getClass().getSimpleName(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO(ErrorCode.INTERNAL.name(), "An unexpected error occurred"))
                .type(MediaType.APPLICATION_JSON).build();
    }
}