package com.pinguela.rentexpress.rest.api.util;

import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Helper para construir respuestas de error con ErrorResponseDTO (contrato unificado).
 * Evita devolver entity(string) en los Resources.
 */
public final class ErrorResponseHelper {

    private ErrorResponseHelper() {}

    public static Response badRequest(String code, String message) {
        return Response.status(Status.BAD_REQUEST)
                .entity(new ErrorResponseDTO(code != null ? code : "BAD_REQUEST", message))
                .type(MediaType.APPLICATION_JSON).build();
    }

    public static Response notFound(String code, String message) {
        return Response.status(Status.NOT_FOUND)
                .entity(new ErrorResponseDTO(code != null ? code : "NOT_FOUND", message))
                .type(MediaType.APPLICATION_JSON).build();
    }

    public static Response unauthorized(String code, String message) {
        return Response.status(Status.UNAUTHORIZED)
                .entity(new ErrorResponseDTO(code != null ? code : "UNAUTHORIZED", message))
                .type(MediaType.APPLICATION_JSON).build();
    }

    /** Respuesta 200 OK con mensaje en el contrato ErrorResponseDTO (p. ej. "Deleted successfully"). */
    public static Response ok(String code, String message) {
        return Response.status(Status.OK)
                .entity(new ErrorResponseDTO(code != null ? code : "OK", message))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
