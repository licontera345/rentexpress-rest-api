package com.pinguela.rentexpress.rest.api.auth.filter;

import java.io.IOException;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.auth.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGIN_PATH = "login";

    public AuthFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();

        // ░░░░░░░░░░░░░░░░░░░░░░░
        // 1) Rutas EXENTAS de token
        // ░░░░░░░░░░░░░░░░░░░░░░░

        if (path != null &&
            (path.startsWith(LOGIN_PATH) ||
             path.startsWith("swagger-ui") ||
             path.startsWith("v3/api-docs") ||
             path.startsWith("openapi") ||
             path.startsWith("api/openapi.json") ||
             path.endsWith("swagger-ui/index.html") ||
             path.endsWith("swagger-ui.html"))) {

            return; // swagger y login quedan libres
        }

        // ░░░░░░░░░░░░░░░░░░░░░░░
        // 2) Validación del Header
        // ░░░░░░░░░░░░░░░░░░░░░░░

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            abort(requestContext, "Authorization header missing or invalid");
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (token == null || token.trim().isEmpty()) {
            abort(requestContext, "Authorization token is empty");
            return;
        }

        // ░░░░░░░░░░░░░░░░░░░░░░░
        // 3) Validación del JWT
        // ░░░░░░░░░░░░░░░░░░░░░░░

        try {
            boolean valid = JwtUtil.isTokenValid(token);

            if (!valid) {
                abort(requestContext, "Invalid or expired token");
                return;
            }

            // Guardamos subject por si algún recurso quiere usarlo
            requestContext.setProperty("jwtSubject", JwtUtil.getSubject(token));

        } catch (JwtException e) {
            logger.warning("JWT validation failed: " + e.getMessage());
            abort(requestContext, "Token validation error");
        }
    }

    private void abort(ContainerRequestContext requestContext, String message) {
        Response response =
                Response.status(Response.Status.UNAUTHORIZED).entity(message).build();
        requestContext.abortWith(response);
    }
}
