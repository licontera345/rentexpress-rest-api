package com.pinguela.rentexpress.rest.api.param;

import java.util.logging.Logger;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.WebApplicationException;

/**
 * Maneja errores de conversión de parámetros automáticamente.
 * Convierte el 404 por defecto en un 400 Bad Request cuando se envía un parámetro inválido.
 * No requiere cambios en los recursos existentes.
 */
@Provider
public class ParamConversionExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = Logger.getLogger(ParamConversionExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        // Dejar que las excepciones JAX-RS (404, 405, etc.) se devuelvan con su respuesta original
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        
        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        
        // Capturar errores de conversión de parámetros
        if (exception instanceof NumberFormatException || 
            (message != null && message.contains("NumberFormatException"))) {
            
            logger.warning("Invalid numeric parameter: " + message);
            
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid parameter format. Numeric value expected.\"}")
                    .build();
        }
        
        // Capturar otros errores de conversión de PathParam/QueryParam
        if (exception instanceof IllegalArgumentException ||
            exceptionType.contains("ParamException") ||
            (message != null && (message.contains("convert") || message.contains("parse")))) {
            
            logger.warning("Parameter conversion error: " + message);
            
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid parameter value.\"}")
                    .build();
        }
        
        // Para cualquier otra excepción no manejada, dejar que el servidor la gestione
        logger.severe("Unhandled exception: " + exceptionType + " - " + message);
        throw new RuntimeException(exception);
    }
}