package com.pinguela.rentexpress.rest.api.security;

import java.io.IOException;

import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;
import com.pinguela.rentexpres.config.ConfigManager;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro de rate limiting por cliente (IP o usuario autenticado).
 * Limita el número de peticiones por ventana de tiempo para evitar abuso.
 * <p>
 * <b>Almacenamiento:</b> Por defecto usa un almacén en memoria (por JVM). En un despliegue
 * con varias instancias detrás de un balanceador, el límite es por instancia, no global.
 * Para un límite compartido entre instancias, implementar un {@link RateLimitStore} con Redis
 * (o similar) y registrarlo en el {@link com.pinguela.rentexpress.rest.api.inject.RestApiBinder}.
 * </p>
 * Configuración:
 * - rate.limit.enabled (true/false)
 * - rate.limit.requests.per.minute (límite por defecto, p. ej. 120)
 * - rate.limit.requests.per.minute.admin (opcional, p. ej. 300)
 * - rate.limit.requests.per.minute.employee (opcional, p. ej. 200)
 * - rate.limit.requests.per.minute.client (opcional, p. ej. 120)
 * Si el usuario está autenticado, se usa el límite de su rol; si no hay rol o no está autenticado, el límite por defecto.
 */
@Provider
@Priority(Priorities.AUTHORIZATION + 100)
public class RateLimitFilter implements ContainerRequestFilter {

    private static final int DEFAULT_REQUESTS_PER_MINUTE = 120;
    private static final long WINDOW_MS = 60_000L;

    private final RateLimitStore store;
    private final boolean enabled;
    private final int defaultMaxRequestsPerMinute;
    private final int adminMaxRequestsPerMinute;
    private final int employeeMaxRequestsPerMinute;
    private final int clientMaxRequestsPerMinute;

    /**
     * Constructor para inyección (Binder). Usa el store inyectado.
     */
    public RateLimitFilter(RateLimitStore store) {
        this.store = store;
        this.enabled = parseEnabled(getConfig("rate.limit.enabled", "true"));
        this.defaultMaxRequestsPerMinute = parsePositiveInt(
                getConfig("rate.limit.requests.per.minute", String.valueOf(DEFAULT_REQUESTS_PER_MINUTE)),
                DEFAULT_REQUESTS_PER_MINUTE);
        this.adminMaxRequestsPerMinute = parsePositiveInt(
                getConfig("rate.limit.requests.per.minute.admin", null),
                defaultMaxRequestsPerMinute);
        this.employeeMaxRequestsPerMinute = parsePositiveInt(
                getConfig("rate.limit.requests.per.minute.employee", null),
                defaultMaxRequestsPerMinute);
        this.clientMaxRequestsPerMinute = parsePositiveInt(
                getConfig("rate.limit.requests.per.minute.client", null),
                defaultMaxRequestsPerMinute);
    }

    private static boolean parseEnabled(String enabledStr) {
        return enabledStr != null && "true".equalsIgnoreCase(enabledStr.trim());
    }

    private static String getConfig(String key, String defaultValue) {
        try {
            String v = ConfigManager.getValue(key);
            return (v != null && !v.trim().isEmpty()) ? v.trim() : defaultValue;
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    private static int parsePositiveInt(String v, int defaultValue) {
        if (v == null || v.trim().isEmpty()) return defaultValue;
        try {
            int n = Integer.parseInt(v.trim());
            return n > 0 ? n : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int getMaxRequestsForContext(ContainerRequestContext requestContext) {
        if (requestContext.getSecurityContext() == null) {
            return defaultMaxRequestsPerMinute;
        }
        if (requestContext.getSecurityContext().isUserInRole("ADMIN")) {
            return adminMaxRequestsPerMinute;
        }
        if (requestContext.getSecurityContext().isUserInRole("EMPLOYEE")) {
            return employeeMaxRequestsPerMinute;
        }
        if (requestContext.getSecurityContext().isUserInRole("CLIENT")) {
            return clientMaxRequestsPerMinute;
        }
        return defaultMaxRequestsPerMinute;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!enabled) return;

        String clientKey = resolveClientKey(requestContext);
        if (clientKey == null || clientKey.isEmpty()) return;

        int maxRequestsPerMinute = getMaxRequestsForContext(requestContext);

        long now = System.currentTimeMillis();
        long windowStartMs = (now / WINDOW_MS) * WINDOW_MS;

        int current = store.incrementAndGet(clientKey, windowStartMs);
        if (current > maxRequestsPerMinute) {
            requestContext.abortWith(Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity(new ErrorResponseDTO("TOO_MANY_REQUESTS", "Rate limit exceeded. Try again later."))
                    .type(MediaType.APPLICATION_JSON).build());
        }
    }

    private String resolveClientKey(ContainerRequestContext requestContext) {
        String principal = null;
        if (requestContext.getSecurityContext() != null && requestContext.getSecurityContext().getUserPrincipal() != null) {
            principal = requestContext.getSecurityContext().getUserPrincipal().getName();
        }
        if (principal != null && !principal.isEmpty()) {
            return "user:" + principal;
        }
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        if (requestContext.getHeaderString("X-Real-IP") != null) {
            return "ip:" + requestContext.getHeaderString("X-Real-IP").trim();
        }
        return "ip:anonymous";
    }
}
