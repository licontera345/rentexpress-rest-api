package com.pinguela.rentexpress.rest.api.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.ErrorCode;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapea RentexpresException (middleware) a Response HTTP estructurada.
 * NOT_FOUND → 404, BAD_REQUEST → 400, CONFLICT → 409, INTERNAL → 500.
 * Registrado como @Provider: los Resources no deben capturar RentexpresException;
 * se devuelve ErrorResponseDTO sin exponer stack traces al cliente.
 * Método estático para compatibilidad con código que aún capture y llame toResponse(e).
 */
@Provider
public class RentexpresExceptionMapper implements ExceptionMapper<RentexpresException> {

    private static final Logger logger = Logger.getLogger(RentexpresExceptionMapper.class.getName());
    private static final String INTERNAL_MESSAGE = "An unexpected error occurred";

    /** Para uso desde Resources que aún capturan la excepción. Preferir dejar propagar y usar @Provider. */
    public static Response buildResponse(RentexpresException e) {
        if (e == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO(ErrorCode.INTERNAL.name(), INTERNAL_MESSAGE))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        logger.warning(() -> "RentexpresException: " + (e.getCode() != null ? e.getCode() : "no code") + " - " + e.getMessage());
        ErrorCode code = e.getCode();
        Status status = code != null ? toStatus(code) : Status.INTERNAL_SERVER_ERROR;
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = status.getReasonPhrase();
        }
        if (status == Status.INTERNAL_SERVER_ERROR) {
            message = INTERNAL_MESSAGE;
            logger.log(Level.SEVERE, "RentexpresException (INTERNAL)", e);
        }
        String codeStr = code != null ? code.name() : ErrorCode.INTERNAL.name();
        java.util.Map<String, String> fieldErrors = e.getFieldErrors();
        ErrorResponseDTO dto = (fieldErrors != null && !fieldErrors.isEmpty())
                ? new ErrorResponseDTO(codeStr, message, fieldErrors)
                : new ErrorResponseDTO(codeStr, message);
        return Response.status(status)
                .entity(dto)
                .type(MediaType.APPLICATION_JSON).build();
    }

    @Override
    public Response toResponse(RentexpresException e) {
        return buildResponse(e);
    }

    private static Status toStatus(ErrorCode code) {
        if (code == null) return Status.INTERNAL_SERVER_ERROR;
        switch (code) {
            case NOT_FOUND: return Status.NOT_FOUND;
            case BAD_REQUEST: return Status.BAD_REQUEST;
            case CONFLICT: return Status.CONFLICT;
            case INTERNAL:
            default: return Status.INTERNAL_SERVER_ERROR;
        }
    }
}
