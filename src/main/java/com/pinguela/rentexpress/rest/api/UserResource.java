package com.pinguela.rentexpress.rest.api;

import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.auth.filter.Secured;
import com.pinguela.rentexpress.rest.api.auth.util.JwtUtil;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

@Path("/user")
@Secured
@RolesAllowed({"EMPLOYEE", "USER"})
@Tag(name = "Users", description = "Operations for user management")
public class UserResource {

    private static final Logger logger = Logger.getLogger(UserResource.class.getName());

    @Context
    private SecurityContext securityContext;

    @Context
    private HttpHeaders headers;

    private final UserService userService;

    public UserResource() {
        this.userService = new UserServiceImpl();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "findUserById", summary = "Find user by ID", description = "Retrieves a user using its unique identifier")
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.NO_CONTENT).build();
        }
        if (!isUserAuthorized(id)) {
            return Response.status(Status.FORBIDDEN).build();
        }
        try {
            UserDTO user = userService.findById(id);
            if (user == null) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(user).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateUser", summary = "Update user", description = "Updates an existing user using its unique identifier")
    public Response update(@PathParam("id") Integer id, UserDTO user) {
        if (id == null || user == null) {
            return Response.status(Status.NO_CONTENT).build();
        }
        user.setUserId(id);
        if (!isUserAuthorized(id)) {
            return Response.status(Status.FORBIDDEN).build();
        }
        try {
            boolean updated = userService.update(user);
            if (!updated) {
                return Response.status(Status.NO_CONTENT).build();
            }
            UserDTO updatedUser = userService.findById(user.getUserId());
            if (updatedUser == null) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(updatedUser).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"EMPLOYEE"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteUser", summary = "Delete user", description = "Deletes a user using its unique identifier")
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.NO_CONTENT).build();
        }
        try {
            boolean deleted = userService.delete(id);
            if (!deleted) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok().entity("User deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/search")
    @RolesAllowed({"EMPLOYEE"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "searchUsers", summary = "Search users by criteria", description = "Retrieves users that match the provided search criteria")
    public Response findByCriteria(
        @QueryParam("userId") Integer userId,
        @QueryParam("roleId") Integer roleId,
        @QueryParam("addressId") Integer addressId,
        @QueryParam("username") String username,
        @QueryParam("firstName") String firstName,
        @QueryParam("lastName1") String lastName1,
        @QueryParam("lastName2") String lastName2,
        @QueryParam("email") String email,
        @QueryParam("phone") String phone,
        @QueryParam("birthDateFrom") java.time.LocalDate birthDateFrom,
        @QueryParam("birthDateTo") java.time.LocalDate birthDateTo,
        @QueryParam("activeStatus") Boolean activeStatus,
        @QueryParam("pageNumber") Integer pageNumber,
        @QueryParam("pageSize") Integer pageSize,
        @QueryParam("createdAtFrom") java.time.LocalDateTime createdAtFrom,
        @QueryParam("createdAtTo") java.time.LocalDateTime createdAtTo,
        @QueryParam("updatedAtFrom") java.time.LocalDateTime updatedAtFrom,
        @QueryParam("updatedAtTo") java.time.LocalDateTime updatedAtTo
    ) {
        UserCriteria criteria = new UserCriteria();
        criteria.setUserId(userId);
        criteria.setRoleId(roleId);
        criteria.setAddressId(addressId);
        criteria.setUsername(username);
        criteria.setFirstName(firstName);
        criteria.setLastName1(lastName1);
        criteria.setLastName2(lastName2);
        criteria.setEmail(email);
        criteria.setPhone(phone);
        criteria.setBirthDateFrom(birthDateFrom);
        criteria.setBirthDateTo(birthDateTo);
        criteria.setActiveStatus(activeStatus);
        criteria.setPageNumber(pageNumber);
        criteria.setPageSize(pageSize);
        criteria.setCreatedAtFrom(createdAtFrom);
        criteria.setCreatedAtTo(createdAtTo);
        criteria.setUpdatedAtFrom(updatedAtFrom);
        criteria.setUpdatedAtTo(updatedAtTo);
        try {
            Results<UserDTO> results = userService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(results).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/activate")
    @RolesAllowed({"EMPLOYEE"})
    @Operation(operationId = "activateUser", summary = "Activate user", description = "Activates a user using its unique identifier")
    public Response activate(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.NO_CONTENT).build();
        }
        try {
            boolean activated = userService.activate(id);
            if (!activated) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok().entity("User activated successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private boolean isUserAuthorized(Integer id) {
        if (id == null) {
            return false;
        }
        if (hasRole("EMPLOYEE")) {
            return true;
        }
        if (hasRole("USER")) {
            Integer currentUserId = getCurrentUserId();
            return currentUserId != null && currentUserId.equals(id);
        }
        return false;
    }

    private boolean hasRole(String role) {
        if (securityContext == null) {
            return false;
        }
        return securityContext.isUserInRole(role);
    }

    private Integer getCurrentUserId() {
        if (headers == null) {
            return null;
        }
        String authorizationHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.length() == 0) {
            return null;
        }
        try {
            Claims claims = JwtUtil.parseClaims(token);
            Object userIdClaim = claims.get("userId");
            if (userIdClaim instanceof Number) {
                return Integer.valueOf(((Number) userIdClaim).intValue());
            }
            if (userIdClaim != null) {
                try {
                    return Integer.valueOf(userIdClaim.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        } catch (JwtException e) {
            logger.warning(e.getMessage());
            return null;
        }
        return null;
    }
}
