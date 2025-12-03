package com.pinguela.rentexpress.rest.api.security;

import java.io.IOException;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.service.OwnershipService;
import com.pinguela.rentexpress.rest.api.service.impl.OwnershipServiceImpl;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@RelationshipCheck(pathParamName = "", relatedEntity = "")
@Priority(Priorities.AUTHORIZATION)
public class RelationshipCheckFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(RelationshipCheckFilter.class.getName());

    private final OwnershipService ownershipService;

    @Context
    private ResourceInfo resourceInfo;

    public RelationshipCheckFilter() {
        super();
        this.ownershipService = new OwnershipServiceImpl();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        RelationshipCheck annotation = resourceInfo.getResourceMethod().getAnnotation(RelationshipCheck.class);
        if (annotation == null) {
            annotation = resourceInfo.getResourceClass().getAnnotation(RelationshipCheck.class);
        }
        if (annotation == null) {
            return;
        }

        jakarta.ws.rs.core.SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            abortUnauthorized(requestContext);
            return;
        }

        if (securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("EMPLOYEE")) {
            return;
        }

        String pathParamName = annotation.pathParamName();
        String relatedEntity = annotation.relatedEntity();

        String resourceId = requestContext.getUriInfo().getPathParameters().getFirst(pathParamName);
        if (resourceId == null) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        String authenticatedOwnerId = requestContext.getSecurityContext().getUserPrincipal().getName();
        boolean isOwned = ownershipService.checkOwnership(relatedEntity, resourceId, authenticatedOwnerId);

        logger.fine(() -> "Ownership check for entity " + relatedEntity + " with resource " + resourceId
                + " and owner " + authenticatedOwnerId + " => " + isOwned);

        if (!isOwned) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied. Resource (" + resourceId + ") not owned by client (" + authenticatedOwnerId
                            + ").")
                    .build());
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Authentication required to perform ownership checks.")
                .build());
    }
}
