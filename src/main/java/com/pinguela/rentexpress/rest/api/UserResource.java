package com.pinguela.rentexpress.rest.api;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Base64;
import java.io.IOException;
import java.io.InputStream;

import com.google.gson.reflect.TypeToken;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.model.PasswordResetTokenDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpress.rest.api.util.RedisCache;
import com.pinguela.rentexpres.util.MailContent;
import com.pinguela.rentexpres.service.MailService;
import com.pinguela.rentexpress.rest.api.security.RelationshipCheck;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.JwtUtil;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpress.rest.api.dto.LoginResponseDTO;
import com.pinguela.rentexpress.rest.api.dto.Confirm2FARequestDTO;
import com.pinguela.rentexpress.rest.api.dto.Disable2FARequestDTO;
import com.pinguela.rentexpress.rest.api.dto.TwoFactorSetupDTO;

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
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.inject.Inject;

@Path("/users")
@Tag(name = "Users", description = "Operations for user management")
public class UserResource {

    private static final Logger logger = Logger.getLogger(UserResource.class.getName());

    /** URL base del frontend para enlaces en emails (recuperar contraseña). Origen: FRONTEND_URL, rentexpress.frontend.url, config.properties frontend.url. Obligatorio en producción. */
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
        try {
            url = com.pinguela.rentexpres.config.ConfigManager.getValue("frontend.url");
            if (url != null && !url.trim().isEmpty()) return url.trim();
        } catch (Throwable t) {
            logger.fine("ConfigManager frontend.url: " + t.getMessage());
        }
        throw new IllegalStateException("Frontend base URL must be configured (FRONTEND_URL, rentexpress.frontend.url or frontend.url in config.properties) for password reset emails.");
    }

    private final UserService userService;
    private final MailService mailService;

    @Inject
    public UserResource(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
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
            @ApiResponse(responseCode = "304", description = "Not Modified (cache válido)"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid user identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the user")
        }
    )
    public Response findById(@PathParam("id") Integer id, @jakarta.ws.rs.core.Context Request request) throws RentexpresException {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "User ID is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        UserDTO user = userService.findById(id);
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String etagValue = computeETag(user);
        EntityTag etag = new EntityTag(etagValue);
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.build();
        }
        return Response.ok(user).tag(etag).build();
    }

    /**
     * Computes a weak ETag from user identifier and updated timestamp for cache revalidation (304 Not Modified).
     */
    private static String computeETag(UserDTO user) {
        StringBuilder sb = new StringBuilder();
        sb.append(user.getUserId() != null ? user.getUserId() : "");
        if (user.getUpdatedAt() != null) {
            sb.append(":").append(user.getUpdatedAt().toString());
        } else {
            sb.append(":").append(System.currentTimeMillis());
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return "W/\"" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            return "W/\"" + Integer.toHexString(sb.toString().hashCode()) + "\"";
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
    public Response create(UserDTO user) throws RentexpresException {
        if (user == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "User data is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        boolean created = userService.create(user);
        if (!created) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "User could not be created"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        UserDTO createdUser = user.getUserId() != null ? userService.findById(user.getUserId()) : user;
        RedisCache.deleteByPrefix("users:");
        return Response.status(Status.CREATED).entity(createdUser).build();
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
    public Response update(@PathParam("id") Integer id, UserDTO user) throws RentexpresException {
        if (id == null || user == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "User ID and data are required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        user.setUserId(id);
        boolean updated = userService.update(user);
        if (!updated) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "User not found or not updated");
        }
        UserDTO updatedUser = userService.findById(user.getUserId());
        RedisCache.deleteByPrefix("users:");
        return Response.ok(updatedUser).build();
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
    public Response delete(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "User ID is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        boolean deleted = userService.delete(id);
        if (!deleted) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "User not found");
        }
        RedisCache.deleteByPrefix("users:");
        return ErrorResponseHelper.ok("OK", "User deleted successfully");
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
    ) throws RentexpresException {
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
    public Response forgotPassword(Map<String, String> body) throws RentexpresException {
        String email = body != null ? body.get("email") : null;
        if (email == null || (email = email.trim()).isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Email is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        UserDTO user = userService.findByEmail(email);
        if (user != null) {
            String token = JwtUtil.generatePasswordResetToken(user.getUserId());
            String tokenHash = JwtUtil.sha256Hex(token);
            userService.savePasswordResetToken(user.getUserId(), tokenHash);
            String baseUrl = getFrontendBaseUrl();
            String resetLink = baseUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
            String subject = MailContent.passwordResetSubject();
            String textBody = MailContent.passwordResetBody(resetLink);
            String htmlBody = MailContent.passwordResetBodyHtml(resetLink);
            boolean sent = mailService.send(user.getEmail(), subject, textBody, htmlBody);
            if (!sent) {
                logger.warning("Failed to send password reset email to " + user.getEmail());
            }
        }
        return Response.ok().build();
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
            @ApiResponse(responseCode = "400", description = "Token and new password are required, password too short, or token already used"),
            @ApiResponse(responseCode = "404", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
        }
    )
    public Response resetPassword(Map<String, String> body) throws RentexpresException {
        String token = body != null ? body.get("token") : null;
        String newPassword = body != null ? body.get("newPassword") : null;
        if (token == null || token.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Token is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "New password is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        Integer userId = JwtUtil.validatePasswordResetToken(token);
        if (userId == null) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Invalid or expired token");
        }
        String tokenHash = JwtUtil.sha256Hex(token);
        PasswordResetTokenDTO tokenInfo = userService.getPasswordResetTokenByHash(tokenHash);
        if (tokenInfo == null) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Invalid or expired token");
        }
        if (Boolean.TRUE.equals(tokenInfo.getUsed())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Token already used"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        if (!userId.equals(tokenInfo.getUserId())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Invalid token"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        userService.updatePassword(userId, newPassword);
        userService.markPasswordResetTokenUsed(tokenHash);
        return Response.ok().build();
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
    public Response authenticate(Map<String, String> credentials) throws RentexpresException {
        if (credentials == null || !credentials.containsKey("login") || !credentials.containsKey("password")) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Login and password are required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        UserDTO user = userService.authenticate(credentials.get("login"), credentials.get("password"));
        if (user == null) {
            return ErrorResponseHelper.unauthorized("UNAUTHORIZED", "Invalid credentials");
        }
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String tempToken = JwtUtil.generateTemp2FAToken(user.getUserId());
            return Response.ok(LoginResponseDTO.withRequiresTwoFactor(tempToken)).build();
        }
        String token = JwtUtil.generateUserToken(user.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return Response.ok(response).build();
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
    public Response activate(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "User ID is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        boolean activated = userService.activate(id);
        if (!activated) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "User not found");
        }
        RedisCache.deleteByPrefix("users:");
        return ErrorResponseHelper.ok("OK", "User activated successfully");
    }

    @GET
    @Path("{id}/2fa/setup")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT" })
    @RelationshipCheck(pathParamName = "id", relatedEntity = "CLIENT_MATCH")
    @Operation(summary = "Start 2FA setup", description = "Returns a new TOTP secret for the user to configure in an authenticator app. Call confirm with the first code to enable.")
    public Response setup2FA(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorResponseDTO("BAD_REQUEST", "User ID required")).type(MediaType.APPLICATION_JSON).build();
        }
        try {
            String secret = userService.setupTwoFactor(id);
            return Response.ok(new TwoFactorSetupDTO(secret)).build();
        } catch (RentexpresException e) {
            if (e.getCode() == com.pinguela.rentexpres.exception.ErrorCode.NOT_FOUND) {
                return Response.status(Status.NOT_FOUND).entity(new ErrorResponseDTO("NOT_FOUND", "User not found")).type(MediaType.APPLICATION_JSON).build();
            }
            if (e.getCode() == com.pinguela.rentexpres.exception.ErrorCode.BAD_REQUEST) {
                return Response.status(Status.BAD_REQUEST).entity(
                    new ErrorResponseDTO("BAD_REQUEST", "Invalid request", e.getFieldErrors())).type(MediaType.APPLICATION_JSON).build();
            }
            throw e;
        }
    }

    @POST
    @Path("{id}/2fa/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT" })
    @RelationshipCheck(pathParamName = "id", relatedEntity = "CLIENT_MATCH")
    @Operation(summary = "Confirm 2FA", description = "Verifies the TOTP code and enables 2FA for the user.")
    public Response confirm2FA(@PathParam("id") Integer id, Confirm2FARequestDTO body) throws RentexpresException {
        if (id == null || body == null || body.getSecret() == null || body.getCode() == null) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorResponseDTO("BAD_REQUEST", "secret and code are required")).type(MediaType.APPLICATION_JSON).build();
        }
        boolean ok = userService.confirmTwoFactor(id, body.getSecret(), body.getCode().trim());
        if (!ok) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorResponseDTO("BAD_REQUEST", "Invalid code")).type(MediaType.APPLICATION_JSON).build();
        }
        return ErrorResponseHelper.ok("OK", "2FA enabled");
    }

    @POST
    @Path("{id}/2fa/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT" })
    @RelationshipCheck(pathParamName = "id", relatedEntity = "CLIENT_MATCH")
    @Operation(summary = "Disable 2FA", description = "Disables 2FA after verifying the current password.")
    public Response disable2FA(@PathParam("id") Integer id, Disable2FARequestDTO body) throws RentexpresException {
        if (id == null || body == null || body.getPassword() == null) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorResponseDTO("BAD_REQUEST", "password is required")).type(MediaType.APPLICATION_JSON).build();
        }
        try {
            boolean ok = userService.disableTwoFactor(id, body.getPassword());
            if (!ok) {
                return Response.status(Status.UNAUTHORIZED).entity(new ErrorResponseDTO("UNAUTHORIZED", "Invalid password")).type(MediaType.APPLICATION_JSON).build();
            }
            return ErrorResponseHelper.ok("OK", "2FA disabled");
        } catch (RentexpresException e) {
            if (e.getCode() == com.pinguela.rentexpres.exception.ErrorCode.NOT_FOUND) {
                return Response.status(Status.NOT_FOUND).entity(new ErrorResponseDTO("NOT_FOUND", "User not found")).type(MediaType.APPLICATION_JSON).build();
            }
            throw e;
        }
    }
}
