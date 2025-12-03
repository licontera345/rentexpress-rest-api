package com.pinguela.rentexpress.rest.api.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.dto.UserAuth;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.RoleDTO;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.RoleService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.impl.EmployeeServiceImpl;
import com.pinguela.rentexpres.service.impl.RoleServiceImpl;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtFilter.class.getName());

    private final UserService userService;
    private final EmployeeService employeeService;
    private final RoleService roleService;

    public JwtFilter() {
        super();
        this.userService = new UserServiceImpl();
        this.employeeService = new EmployeeServiceImpl();
        this.roleService = new RoleServiceImpl();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(requestContext, "Token faltante o inválido");
            return;
        }
        String token = authHeader.substring("Bearer".length()).trim();
        try {
            String subject = JwtUtil.validateToken(token);
            UserAuth userAuth = authenticateSubject(subject);
            if (userAuth == null) {
                abort(requestContext, "Token inválido o expirado");
                return;
            }
            boolean isHttps = true;
            requestContext.setSecurityContext(new AppSecurityContext(userAuth, isHttps));
        } catch (Exception e) {
            abort(requestContext, "Token inválido o expirado");
        }
    }

    private UserAuth authenticateSubject(String subject) throws RentexpresException {
        if (subject == null) {
            return null;
        }
        if (subject.startsWith("EMPLOYEE:")) {
            Integer employeeId = extractIdentifier(subject, "EMPLOYEE:");
            if (employeeId == null) {
                return null;
            }
            EmployeeDTO employee = employeeService.findById(employeeId);
            if (employee == null) {
                return null;
            }
            Set<String> roles = resolveRoles(employee.getRoleId());
            return new UserAuth(employee.getId().toString(), roles);
        }
        Integer userId = subject.startsWith("USER:") ? extractIdentifier(subject, "USER:") : extractIdentifier(subject, "");
        if (userId == null) {
            return null;
        }
        UserDTO user = userService.findById(userId);
        if (user == null) {
            return null;
        }
        Set<String> roles = resolveRoles(user.getRoleId());
        return new UserAuth(user.getUserId().toString(), roles);
    }

    private Integer extractIdentifier(String subject, String prefix) {
        try {
            String rawId = prefix.isEmpty() ? subject : subject.substring(prefix.length());
            return Integer.valueOf(rawId);
        } catch (NumberFormatException e) {
            logger.fine("Invalid token subject format: " + subject);
            return null;
        }
    }

    private Set<String> resolveRoles(Integer roleId) {
        Set<String> roles = new HashSet<>();
        if (roleId == null) {
            return roles;
        }
        roles.add(String.valueOf(roleId));
        try {
            RoleDTO role = roleService.findById(roleId);
            if (role != null && role.getRoleName() != null) {
                roles.add(role.getRoleName());
            }
        } catch (RentexpresException e) {
            logger.fine("Could not resolve role name for roleId=" + roleId);
        }
        return roles;
    }

    private void abort(ContainerRequestContext requestContext, String msg) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"msg\" :\"" + msg + "\" }")
                .build());
    }
}
