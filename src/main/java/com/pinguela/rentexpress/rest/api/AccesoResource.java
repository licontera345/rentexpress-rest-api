package com.pinguela.rentexpress.rest.api;

import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.EmployeeServiceImpl;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;
import com.pinguela.rentexpress.rest.api.dto.UserCredentials;
import com.pinguela.rentexpress.rest.api.security.JwtUtil;

import jakarta.ws.rs.Consumes;
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

	public AccesoResource() {
		super();
		this.userService = new UserServiceImpl();
		this.employeeService = new EmployeeServiceImpl();
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
			
			logger.severe("Service error during login: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
}