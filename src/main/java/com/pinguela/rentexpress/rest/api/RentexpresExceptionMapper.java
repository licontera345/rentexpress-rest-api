package com.pinguela.rentexpress.rest.api;

import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.ErrorCode;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Mapea RentexpresException (código del middleware) a Response HTTP (M1/M2).
 * NOT_FOUND → 404, BAD_REQUEST → 400, CONFLICT → 409, null/INTERNAL → 500.
 */
public final class RentexpresExceptionMapper {

    private static final Logger logger = Logger.getLogger(RentexpresExceptionMapper.class.getName());
    private static final String INTERNAL_MESSAGE = "An unexpected error occurred";

    private RentexpresExceptionMapper() {}

    /**
     * Construye la Response HTTP y el cuerpo de error a partir de la excepción.
     */
    public static Response toResponse(RentexpresException e) {
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
        }
        String codeStr = code != null ? code.name() : ErrorCode.INTERNAL.name();
        return Response.status(status)
                .entity(new ErrorResponseDTO(codeStr, message))
                .type(MediaType.APPLICATION_JSON).build();
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
