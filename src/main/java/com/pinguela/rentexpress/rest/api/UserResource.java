package com.pinguela.rentexpress.rest.api;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStream;

import com.google.gson.reflect.TypeToken;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpress.rest.api.util.RedisCache;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;
import com.pinguela.rentexpres.util.MailContent;
import com.pinguela.rentexpres.service.impl.MailServiceImpl;
import com.pinguela.rentexpress.rest.api.security.RelationshipCheck;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/users")
@Tag(name = "Users", description = "Operations for user management")
public class UserResource {

    private static final Logger logger = Logger.getLogger(UserResource.class.getName());
    private static final int MIN_AGE_FOR_REGISTER = 18;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /** URL base del frontend para enlaces en emails (recuperar contraseña). Origen: FRONTEND_URL, rentexpress.frontend.url, config.properties frontend.url, o default localhost. */
    private static String getFrontendBaseUrl() {
        String url = System.getenv("FRONTEND_URL");
        if (url != null && !url.isEmpty()) return url.trim();
        url = System.getProperty("rentexpress.frontend.url");
        if (url != null && !url.isEmpty()) return url.trim();
        try (InputStream is = UserResource.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                url = p.getProperty("frontend.url");
                if (url != null && !url.trim().isEmpty()) return url.trim();
            }
        } catch (IOException e) {
            logger.fine("Could not load config.properties for frontend.url: " + e.getMessage());
        }
        return "http://localhost:5173";
    }

    private final UserService userService;

    public UserResource() {
        this.userService = new UserServiceImpl();
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
    @RelationshipCheck(pathParamName = "id", relatedEntity = "CLIENT_MATCH")
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
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @POST
    @Path("/open")
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
        if (user.getBirthDate() != null) {
            long age = ChronoUnit.YEARS.between(user.getBirthDate(), LocalDate.now());
            if (age < MIN_AGE_FOR_REGISTER) {
                return Response.status(Status.BAD_REQUEST).entity("Minimum age for registration is " + MIN_AGE_FOR_REGISTER).build();
            }
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            return Response.status(Status.BAD_REQUEST).entity("Password must be at least " + MIN_PASSWORD_LENGTH + " characters").build();
        }
        try {
            boolean created = userService.create(user);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("User could not be created").build();
            }
            UserDTO createdUser = user.getUserId() != null ? userService.findById(user.getUserId()) : user;
            RedisCache.deleteByPrefix("users:");
            return Response.status(Status.CREATED).entity(createdUser).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
    @RelationshipCheck(pathParamName = "id", relatedEntity = "CLIENT_MATCH")
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
    public Response update(@PathParam("id") Integer id, UserDTO user) {
        if (id == null || user == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID and data are required").build();
        }
        user.setUserId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            return Response.status(Status.BAD_REQUEST).entity("Password must be at least " + MIN_PASSWORD_LENGTH + " characters").build();
        }
        if (user.getBirthDate() != null) {
            long age = ChronoUnit.YEARS.between(user.getBirthDate(), LocalDate.now());
            if (age < MIN_AGE_FOR_REGISTER) {
                return Response.status(Status.BAD_REQUEST).entity("Minimum age is " + MIN_AGE_FOR_REGISTER).build();
            }
        }
        try {
            boolean updated = userService.update(user);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("User not found or not updated").build();
            }
            UserDTO updatedUser = userService.findById(user.getUserId());
            RedisCache.deleteByPrefix("users:");
            return Response.ok(updatedUser).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
    @RelationshipCheck(pathParamName = "id", relatedEntity = "CLIENT_MATCH")
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
            RedisCache.deleteByPrefix("users:");
            return Response.ok().entity("User deleted successfully").build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
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
        int defaultPageNumber = 1;
        int defaultPageSize = 10;

        criteria.setPageNumber(pageNumber != null && pageNumber > 0 ? pageNumber : defaultPageNumber);
        criteria.setPageSize(pageSize != null && pageSize > 0 ? pageSize : defaultPageSize);

        criteria.setCreatedAtFrom(createdAtFrom);
        criteria.setCreatedAtTo(createdAtTo);
        criteria.setUpdatedAtFrom(updatedAtFrom);
        criteria.setUpdatedAtTo(updatedAtTo);
        try {
            String cacheKey = "users:"
                    + RedisCache.keyPart(userId) + ":"
                    + RedisCache.keyPart(roleId) + ":"
                    + RedisCache.keyPart(addressId) + ":"
                    + RedisCache.keyPart(username) + ":"
                    + RedisCache.keyPart(firstName) + ":"
                    + RedisCache.keyPart(lastName1) + ":"
                    + RedisCache.keyPart(lastName2) + ":"
                    + RedisCache.keyPart(email) + ":"
                    + RedisCache.keyPart(phone) + ":"
                    + RedisCache.keyPart(birthDateFrom) + ":"
                    + RedisCache.keyPart(birthDateTo) + ":"
                    + RedisCache.keyPart(activeStatus) + ":"
                    + criteria.getPageNumber() + ":"
                    + criteria.getPageSize() + ":"
                    + RedisCache.keyPart(createdAtFrom) + ":"
                    + RedisCache.keyPart(createdAtTo) + ":"
                    + RedisCache.keyPart(updatedAtFrom) + ":"
                    + RedisCache.keyPart(updatedAtTo);
            Type type = new TypeToken<Results<UserDTO>>(){}.getType();
            Results<UserDTO> cached = RedisCache.getObject(cacheKey, type);
            if (cached != null) {
                return Response.ok(cached).build();
            }
            Results<UserDTO> results = userService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            RedisCache.setObject(cacheKey, results, RedisCache.DEFAULT_TTL_SECONDS);
            return Response.ok(results).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @POST
    @Path("/open/forgot-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "forgotPassword",
        summary = "Request password reset",
        description = "Sends a password reset link to the user's email if the account exists. Always returns 200 to avoid revealing whether the email is registered.",
        responses = {
            @ApiResponse(responseCode = "200", description = "If the email exists, a reset link was sent; otherwise nothing is done"),
            @ApiResponse(responseCode = "400", description = "Email is required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
        }
    )
    public Response forgotPassword(Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        if (email == null || (email = email.trim()).isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Email is required").build();
        }
        try {
            UserDTO user = userService.findByEmail(email);
            if (user != null) {
                String token = JwtUtil.generatePasswordResetToken(user.getUserId());
                String baseUrl = getFrontendBaseUrl();
                String resetLink = baseUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
                String subject = MailContent.passwordResetSubject();
                String textBody = MailContent.passwordResetBody(resetLink);
                String htmlBody = MailContent.passwordResetBodyHtml(resetLink);
                MailServiceImpl mailService = new MailServiceImpl();
                boolean sent = mailService.send(user.getEmail(), subject, textBody, htmlBody);
                if (!sent) {
                    logger.warning("Failed to send password reset email to " + user.getEmail());
                }
            }
            return Response.ok().build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @POST
    @Path("/open/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "resetPassword",
        summary = "Reset password with token",
        description = "Sets a new password using the token received by email",
        responses = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Token and new password are required, or password too short"),
            @ApiResponse(responseCode = "404", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
        }
    )
    public Response resetPassword(Map<String, String> body) {
        String token = body != null ? body.get("token") : null;
        String newPassword = body != null ? body.get("newPassword") : null;
        if (token == null || token.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Token is required").build();
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Password must be at least " + MIN_PASSWORD_LENGTH + " characters").build();
        }
        try {
            Integer userId = JwtUtil.validatePasswordResetToken(token);
            if (userId == null) {
                return Response.status(Status.NOT_FOUND).entity("Invalid or expired token").build();
            }
            boolean updated = userService.updatePassword(userId, newPassword);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("User not found").build();
            }
            return Response.ok().build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @POST
    @Path("/open/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "authenticateUser",
        summary = "Authenticate user",
        description = "Authenticates a user using login credentials and returns an access token and user data",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON)
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
            String token = JwtUtil.generateUserToken(user.getUserId());
            
            // Crear objeto de respuesta con token y usuario
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            
            return Response.ok(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }
    
    @POST
    @Path("/{id}/activate")
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
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
            RedisCache.deleteByPrefix("users:");
            return Response.ok().entity("User activated successfully").build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }
}
