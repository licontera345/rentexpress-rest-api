package com.pinguela.rentexpress.rest.api.auth.filter;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.auth.security.AppSecurityContext;
import com.pinguela.rentexpress.rest.api.auth.security.UserAuth;
import com.pinguela.rentexpress.rest.api.auth.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtFilter.class.getName());
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtFilter() {
    }

    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            abort(requestContext, "Authorization header missing or invalid");
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.length() == 0) {
            abort(requestContext, "Authorization token is empty");
            return;
        }

        try {
            Claims claims = JwtUtil.parseClaims(token);
            boolean valid = JwtUtil.isTokenValid(token);
            if (!valid) {
                abort(requestContext, "Invalid or expired token");
                return;
            }

            String username = claims.getSubject();
            Set<String> roles = JwtUtil.extractRoles(claims);
            if (roles.isEmpty()) {
                roles.add("USER");
            }

            boolean isHttps = requestContext.getUriInfo().getRequestUri().getScheme().equalsIgnoreCase("https");
            UserAuth userAuth = new UserAuth(username, roles);
            requestContext.setSecurityContext(new AppSecurityContext(userAuth, isHttps));
        } catch (JwtException e) {
            logger.warning("JWT validation failed: " + e.getMessage());
            abort(requestContext, "Token validation error");
        }
    }

    private void abort(ContainerRequestContext requestContext, String message) {
        Response response = Response.status(Response.Status.UNAUTHORIZED).entity(message).build();
        requestContext.abortWith(response);
    }
}
