package com.pinguela.rentexpress.rest.api.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.auth.dto.UserCredentials;
import com.pinguela.rentexpress.rest.api.auth.util.JwtUtil;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.EmployeeServiceImpl;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/login")
public class LoginResource {

    private static final Logger logger = Logger.getLogger(LoginResource.class.getName());
    private static final String PROFILE_USER = "USER";
    private static final String PROFILE_EMPLOYEE = "EMPLOYEE";

    private final UserService userService;
    private final EmployeeService employeeService;

    public LoginResource() {
        this.userService = new UserServiceImpl();
        this.employeeService = new EmployeeServiceImpl();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserCredentials credentials) {
        if (credentials == null) {
            return Response.status(Status.BAD_REQUEST).entity("Login and password are required").build();
        }

        String login = extractLogin(credentials);
        String password = credentials.getPassword();

        if (login == null || password == null) {
            return Response.status(Status.BAD_REQUEST).entity("Login and password are required").build();
        }

        try {
            UserDTO user = userService.authenticate(login, password);
            if (user != null) {
                Map<String, Object> response = buildUserResponse(user, login);
                return Response.ok(response).build();
            }

            EmployeeDTO employee = employeeService.autenticar(login, password);
            if (employee != null) {
                Map<String, Object> response = buildEmployeeResponse(employee, login);
                return Response.ok(response).build();
            }

            return Response.status(Status.UNAUTHORIZED).entity("Invalid credentials").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private Map<String, Object> buildUserResponse(UserDTO user, String login) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("profile", PROFILE_USER);
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        claims.put("roles", PROFILE_USER);

        String token = JwtUtil.generateToken(login, claims);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("token", token);
        response.put("expiresIn", Long.valueOf(JwtUtil.EXPIRATION_TIME));
        response.put("profile", PROFILE_USER);
        response.put("user", user);
        return response;
    }

    private Map<String, Object> buildEmployeeResponse(EmployeeDTO employee, String login) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("profile", PROFILE_EMPLOYEE);
        claims.put("employeeId", employee.getId());
        claims.put("roleId", employee.getRoleId());
        claims.put("roles", PROFILE_EMPLOYEE);

        String token = JwtUtil.generateToken(login, claims);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("token", token);
        response.put("expiresIn", Long.valueOf(JwtUtil.EXPIRATION_TIME));
        response.put("profile", PROFILE_EMPLOYEE);
        response.put("employee", employee);
        return response;
    }

    private String extractLogin(UserCredentials credentials) {
        if (credentials.getUsername() != null && credentials.getUsername().trim().length() > 0) {
            return credentials.getUsername();
        }
        return null;
    }
}
