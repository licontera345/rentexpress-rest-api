package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.dto.FilterRangesConfigDTO;
import com.pinguela.rentexpress.rest.api.dto.ImageUploadConfigDTO;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.CloudinarySignatureDTO;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.CloudinaryService;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.CloudinaryServiceImpl;
import com.pinguela.rentexpres.service.impl.EmployeeServiceImpl;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;
import com.pinguela.rentexpress.rest.api.dto.UserCredentials;
import com.pinguela.rentexpress.rest.api.util.JwtUtil;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/open")
public class AccesoResource {

	private static final Logger logger = Logger.getLogger(AccesoResource.class.getName());

	private final UserService userService;
	private final EmployeeService employeeService;
	private final CloudinaryService cloudinaryService;

	public AccesoResource() {
		super();
		this.userService = new UserServiceImpl();
		this.employeeService = new EmployeeServiceImpl();
		this.cloudinaryService = new CloudinaryServiceImpl();
	}

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(UserCredentials cred) {

		if (cred == null || cred.getUsername() == null || cred.getPassword() == null) {
			return Response.status(Status.BAD_REQUEST).entity("Username and password are required").build();
		}

		try {

			UserDTO user = userService.authenticate(cred.getUsername(), cred.getPassword());

			if (user != null) {

				String token = JwtUtil.generateUserToken(user.getUserId());
				logger.info("User logged in: " + cred.getUsername());
				return Response.status(Status.OK).entity("{\"token\" : \"" + token + "\"}").build();
			}

			EmployeeDTO employee = employeeService.autenticar(cred.getUsername(), cred.getPassword());

			if (employee != null) {

				String token = JwtUtil.generateEmployeeToken(employee.getId());
				logger.info("Employee logged in: " + cred.getUsername());
				return Response.status(Status.OK).entity("{\"token\" : \"" + token + "\"}").build();
			}

			logger.warning("Authentication failed for: " + cred.getUsername());
			return Response.status(Response.Status.UNAUTHORIZED).build();

		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}
	
	private static final long IMAGE_MAX_SIZE_BYTES = 5L * 1024 * 1024;
	private static final List<String> IMAGE_ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp");

	@GET
	@Path("/config/image-upload")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImageUploadConfig() {
		return Response.ok(new ImageUploadConfigDTO(IMAGE_MAX_SIZE_BYTES, IMAGE_ALLOWED_MIME_TYPES)).build();
	}


	private static final int FILTER_YEAR_MIN = 1990;
	private static final int FILTER_KM_MAX = 200_000;
	private static final int FILTER_PRICE_MAX = 500;

	@GET
	@Path("/config/filter-ranges")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFilterRangesConfig() {
		int currentYear = java.time.Year.now().getValue();
		FilterRangesConfigDTO dto = new FilterRangesConfigDTO(
				new FilterRangesConfigDTO.RangeConfig(FILTER_YEAR_MIN, currentYear),
				new FilterRangesConfigDTO.RangeConfig(0, FILTER_KM_MAX),
				new FilterRangesConfigDTO.RangeConfig(0, FILTER_PRICE_MAX));
		return Response.ok(dto).build();
	}

	@GET
	@Path("/generate-signature")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSignature() {
		try {
			CloudinarySignatureDTO signature = cloudinaryService.generateSignature();
			return Response.ok(signature).build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}
}
	