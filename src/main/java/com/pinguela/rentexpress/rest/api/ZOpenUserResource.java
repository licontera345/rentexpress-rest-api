package com.pinguela.rentexpress.rest.api;

import java.util.Map;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/open/user")
@Tag(name = "Users", description = "Operations for user management")
public class ZOpenUserResource {

    private static final Logger logger = Logger.getLogger(ZOpenUserResource.class.getName());

    private final UserService userService;

    public ZOpenUserResource() {
        this.userService = new UserServiceImpl();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createUser", summary = "Create user", description = "Creates a new user in the system")
    public Response create(UserDTO user) {
        if (user == null) {
            return Response.status(Status.NO_CONTENT).build();
        }
        try {
            boolean created = userService.create(user);
            if (!created) {
                return Response.status(Status.NO_CONTENT).build();
            }
            UserDTO createdUser = user.getUserId() != null ? userService.findById(user.getUserId()) : user;
            return Response.ok(createdUser).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "authenticateUser", summary = "Authenticate user", description = "Authenticates a user using login credentials")
    public Response authenticate(Map<String, String> credentials) {
        if (credentials == null || !credentials.containsKey("login") || !credentials.containsKey("password")) {
            return Response.status(Status.NO_CONTENT).build();
        }
        try {
            UserDTO user = userService.authenticate(credentials.get("login"), credentials.get("password"));
            if (user == null) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(user).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
