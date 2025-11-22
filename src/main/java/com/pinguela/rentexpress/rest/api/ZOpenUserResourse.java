package com.pinguela.rentexpress.rest.api;

import java.util.Map;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/users")
@Tag(name = "Users", description = "Operations for user management")
public class ZOpenUserResourse {

    private static final Logger logger = Logger.getLogger(ZOpenUserResourse.class.getName());

    private final UserService userService;

    public ZOpenUserResourse() {
        this.userService = new UserServiceImpl();
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findUserById",
        summary = "Find user by ID",
        description = "Retrieves a user using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid user identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the user")
        }
    )
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID is required").build();
        }
        try {
            UserDTO user = userService.findById(id);
            if (user == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(user).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "createUser",
        summary = "Create user",
        description = "Creates a new user in the system",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid user data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the user")
        }
    )
    public Response create(UserDTO user) {
        if (user == null) {
            return Response.status(Status.BAD_REQUEST).entity("User data is required").build();
        }
        try {
            boolean created = userService.create(user);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("User could not be created").build();
            }
            UserDTO createdUser = user.getUserId() != null ? userService.findById(user.getUserId()) : user;
            return Response.status(Status.CREATED).entity(createdUser).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "updateUser",
        summary = "Update user",
        description = "Updates an existing user using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid user data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the user")
        }
    )
    public Response update(UserDTO user) {
        if (user == null || user.getUserId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID and data are required").build();
        }
        try {
            boolean updated = userService.update(user);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("User not found or not updated").build();
            }
            UserDTO updatedUser = userService.findById(user.getUserId());
            return Response.ok(updatedUser).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "deleteUser",
        summary = "Delete user",
        description = "Deletes a user using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid user identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the user")
        }
    )
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID is required").build();
        }
        try {
            boolean deleted = userService.delete(id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("User not found").build();
            }
            return Response.ok().entity("User deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "searchUsers",
        summary = "Search users by criteria",
        description = "Retrieves users that match the provided search criteria",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Users retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))
            ),
            @ApiResponse(responseCode = "204", description = "No users found"),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while searching users")
        }
    )
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
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "authenticateUser",
        summary = "Authenticate user",
        description = "Authenticates a user using login credentials",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Login and password are required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while authenticating the user")
        }
    )
    public Response authenticate(Map<String, String> credentials) {
        if (credentials == null || !credentials.containsKey("login") || !credentials.containsKey("password")) {
            return Response.status(Status.BAD_REQUEST).entity("Login and password are required").build();
        }
        try {
            UserDTO user = userService.authenticate(credentials.get("login"), credentials.get("password"));
            if (user == null) {
                return Response.status(Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
            return Response.ok(user).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/activate")
    @Operation(
        operationId = "activateUser",
        summary = "Activate user",
        description = "Activates a user using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User activated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid user identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while activating the user")
        }
    )
    public Response activate(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID is required").build();
        }
        try {
            boolean activated = userService.activate(id);
            if (!activated) {
                return Response.status(Status.NOT_FOUND).entity("User not found").build();
            }
            return Response.ok().entity("User activated successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
