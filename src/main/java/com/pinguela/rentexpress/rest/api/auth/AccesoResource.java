package com.pinguela.rentexpress.rest.api.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpres.model.CloudinarySignatureDTO;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.CloudinaryService;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpress.rest.api.dto.FilterRangesConfigDTO;
import com.pinguela.rentexpress.rest.api.dto.GoogleAuthResponse;
import com.pinguela.rentexpress.rest.api.dto.ImageUploadConfigDTO;
import com.pinguela.rentexpress.rest.api.dto.LoginResponseDTO;
import com.pinguela.rentexpress.rest.api.dto.Token;
import com.pinguela.rentexpress.rest.api.dto.UserCredentials;
import com.pinguela.rentexpress.rest.api.dto.Verify2FARequestDTO;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpress.rest.api.util.JwtUtil;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.inject.Inject;

@Path("/open")
public class AccesoResource {

	private static final Logger logger = Logger.getLogger(AccesoResource.class.getName());

	private final UserService userService;
	private final EmployeeService employeeService;
	private final CloudinaryService cloudinaryService;

	@Inject
	public AccesoResource(UserService userService, EmployeeService employeeService, CloudinaryService cloudinaryService) {
		this.userService = userService;
		this.employeeService = employeeService;
		this.cloudinaryService = cloudinaryService;
	}

	/** Google OAuth client ID: GOOGLE_CLIENT_ID, rentexpress.google.clientId, config.properties google.clientId. Obligatorio si se usa login con Google. */
	private static String getGoogleClientId() {
		String clientId = System.getenv("GOOGLE_CLIENT_ID");
		if (clientId != null && !clientId.isEmpty()) return clientId.trim();
		clientId = System.getProperty("rentexpress.google.clientId");
		if (clientId != null && !clientId.isEmpty()) return clientId.trim();
		try (InputStream is = AccesoResource.class.getResourceAsStream("/config.properties")) {
			if (is != null) {
				Properties p = new Properties();
				p.load(is);
				clientId = p.getProperty("google.clientId");
				if (clientId != null && !clientId.trim().isEmpty()) return clientId.trim();
			}
		} catch (IOException e) {
			logger.fine("Could not load config.properties for google.clientId: " + e.getMessage());
		}
		try {
			clientId = ConfigManager.getValue("google.clientId");
			if (clientId != null && !clientId.trim().isEmpty()) return clientId.trim();
		} catch (Throwable t) {
			logger.fine("ConfigManager google.clientId: " + t.getMessage());
		}
		throw new IllegalStateException("Google OAuth client ID must be configured (GOOGLE_CLIENT_ID, rentexpress.google.clientId or google.clientId in config.properties).");
	}

	
	@POST
	@Path("/auth/google")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response googleAuth(Token tokenRequestGoogle) {
		if (tokenRequestGoogle == null || tokenRequestGoogle.getToken() == null || tokenRequestGoogle.getToken().isEmpty()) {
			return ErrorResponseHelper.badRequest("BAD_REQUEST", "Token is required");
		}
		String googleClientId = getGoogleClientId();
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
				new NetHttpTransport(),
				GsonFactory.getDefaultInstance())
				.setAudience(Collections.singletonList(googleClientId))
				.build();

		try {
			GoogleIdToken idToken = verifier.verify(tokenRequestGoogle.getToken());
			if (idToken == null) {
				return Response.status(Status.UNAUTHORIZED).build();
			}
			Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String name = (String) payload.get("name");
			// Identificador único estable de Google (claim "sub" del JWT). Ej: "109876543210987654321"
			String googleId = payload.getSubject();
			if (email == null || email.isEmpty() || googleId == null || googleId.isEmpty()) {
				return ErrorResponseHelper.badRequest("BAD_REQUEST", "Invalid Google token payload");
			}

			// Usuario existente: por google_id (prioritario) o por email
			UserDTO user = userService.findByGoogleId(googleId);
			if (user == null) {
				user = userService.findByEmail(email);
			}

			if (user != null) {
				String myTokenValue = JwtUtil.generateUserToken(user.getUserId());
				logger.info("Google login OK: " + email);
				return Response.ok(new GoogleAuthResponse(myTokenValue, user)).build();
			}

			// Usuario no existe: indicar que debe completar registro (frontend redirige a /auth/register con googlePayload)
			logger.info("Google user not registered, needsRegistration: " + email);
			return Response.ok(GoogleAuthResponse.needsRegistration(email, name != null ? name : email, googleId)).build();

		} catch (Exception e) {
			logger.warning("Google auth error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	
	
	
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(UserCredentials cred) throws RentexpresException {

		if (cred == null || cred.getUsername() == null || cred.getPassword() == null) {
			return ErrorResponseHelper.badRequest("BAD_REQUEST", "Username and password are required");
		}

		UserDTO user = userService.authenticate(cred.getUsername(), cred.getPassword());

		if (user != null) {
			if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
				String tempToken = JwtUtil.generateTemp2FAToken(user.getUserId());
				return Response.ok(LoginResponseDTO.withRequiresTwoFactor(tempToken)).build();
			}
			String token = JwtUtil.generateUserToken(user.getUserId());
			logger.info("User logged in: " + cred.getUsername());
			return Response.ok(LoginResponseDTO.withToken(token)).build();
		}

		EmployeeDTO employee = employeeService.autenticar(cred.getUsername(), cred.getPassword());

		if (employee != null) {
			String token = JwtUtil.generateEmployeeToken(employee.getId());
			logger.info("Employee logged in: " + cred.getUsername());
			return Response.ok(LoginResponseDTO.withToken(token)).build();
		}

		logger.warning("Authentication failed for: " + cred.getUsername());
		return Response.status(Status.UNAUTHORIZED).build();
	}

	@POST
	@Path("/verify-2fa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response verify2FA(Verify2FARequestDTO body) throws RentexpresException {
		if (body == null || body.getTempToken() == null || body.getCode() == null) {
			return ErrorResponseHelper.badRequest("BAD_REQUEST", "tempToken and code are required");
		}
		Integer userId = JwtUtil.validateTemp2FAToken(body.getTempToken());
		if (userId == null) {
			return ErrorResponseHelper.unauthorized("UNAUTHORIZED", "Invalid or expired temporary token");
		}
		if (!userService.verifyTwoFactorCode(userId, body.getCode().trim())) {
			return ErrorResponseHelper.unauthorized("UNAUTHORIZED", "Invalid 2FA code");
		}
		UserDTO user = userService.findById(userId);
		String token = JwtUtil.generateUserToken(userId);
		Map<String, Object> response = new HashMap<>();
		response.put("token", token);
		response.put("user", user);
		return Response.ok(response).build();
	}
	
	private static final List<String> IMAGE_ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp");

	/** Tamaño máximo de imagen desde config (base.image.max.bytes) o 5 MB por defecto. */
	private static long getImageMaxSizeBytes() {
		String v = null;
		try {
			v = ConfigManager.getValue("base.image.max.bytes");
		} catch (Throwable t) {
			// Config no cargado
		}
		if (v != null && !v.trim().isEmpty()) {
			try {
				return Long.parseLong(v.trim());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return 5L * 1024 * 1024;
	}

	@GET
	@Path("/config/image-upload")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImageUploadConfig() {
		return Response.ok(new ImageUploadConfigDTO(getImageMaxSizeBytes(), IMAGE_ALLOWED_MIME_TYPES)).build();
	}


	private static int getConfigInt(String key, int defaultValue) {
		String v = null;
		try {
			v = ConfigManager.getValue(key);
		} catch (Throwable t) {
			// ignore
		}
		if (v != null && !v.trim().isEmpty()) {
			try {
				return Integer.parseInt(v.trim());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return defaultValue;
	}

	@GET
	@Path("/config/filter-ranges")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFilterRangesConfig() {
		int yearMin = getConfigInt("filter.year.min", 1990);
		int kmMax = getConfigInt("filter.km.max", 200_000);
		int priceMax = getConfigInt("filter.price.max", 500);
		int currentYear = java.time.Year.now().getValue();
		FilterRangesConfigDTO dto = new FilterRangesConfigDTO(
				new FilterRangesConfigDTO.RangeConfig(yearMin, currentYear),
				new FilterRangesConfigDTO.RangeConfig(0, kmMax),
				new FilterRangesConfigDTO.RangeConfig(0, priceMax));
		return Response.ok(dto).build();
	}

	@GET
	@Path("/generate-signature")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSignature() throws RentexpresException {
		CloudinarySignatureDTO signature = cloudinaryService.generateSignature();
		return Response.ok(signature).build();
	}
}
	