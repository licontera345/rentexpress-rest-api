package com.pinguela.rentexpress.rest.api.security;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro que valida las anotaciones @RolesAllowed en los endpoints. Se ejecuta
 * despues de JwtFilter (que maneja autenticación).
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class RolesAllowedFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		Method method = resourceInfo.getResourceMethod();
		// Buscar @RolesAllowed primero en el método, luego en la clase
		RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
		if (rolesAllowed == null) {
			rolesAllowed = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
		}

		// Si no hay @RolesAllowed permitir acceso
		if (rolesAllowed == null) {
			return;
		}

		// Validar que el usuario esté autenticado
		var securityContext = requestContext.getSecurityContext();
		if (securityContext == null || securityContext.getUserPrincipal() == null) {
			abortUnauthorized(requestContext, "Authentication required");
			return;
		}

		// Obtener los roles permitidos
		Set<String> allowedRoles = new HashSet<>(Arrays.asList(rolesAllowed.value()));

		// Verificar si el usuario tiene alguno de los roles permitidos
		boolean hasRole = allowedRoles.stream().anyMatch(role -> securityContext.isUserInRole(role));

		// Si no tiene el rol requerido, denegar acceso
		if (!hasRole) {
			abortForbidden(requestContext, "User does not have required role. Required: " + allowedRoles);
		}
	}

	private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
		requestContext.abortWith(
				Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"" + message + "\"}").build());
	}

	private void abortForbidden(ContainerRequestContext requestContext, String message) {
		requestContext.abortWith(
				Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"" + message + "\"}").build());
	}
}