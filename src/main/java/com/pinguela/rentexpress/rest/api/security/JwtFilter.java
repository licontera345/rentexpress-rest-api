package com.pinguela.rentexpress.rest.api.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.dto.UserAuth;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RoleDTO;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.RoleService;
import com.pinguela.rentexpres.service.UserService;
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
    private final RoleService roleService;

    public JwtFilter() {
        super();
        this.userService = new UserServiceImpl();
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
            String userId = JwtUtil.validateToken(token);
            UserDTO user = userService.findById(Integer.valueOf(userId));
            if (user == null) {
                abort(requestContext, "Token inválido o expirado");
                return;
            }

            Set<String> roles = new HashSet<>();
            if (user.getRoleId() != null) {
                roles.add(String.valueOf(user.getRoleId()));
                try {
                    RoleDTO role = roleService.findById(user.getRoleId());
                    if (role != null && role.getRoleName() != null) {
                        roles.add(role.getRoleName());
                    }
                } catch (RentexpresException e) {
                    logger.fine(() -> "Could not resolve role name for roleId=" + user.getRoleId());
                }
            }

            UserAuth userAuth = new UserAuth(user.getUserId().toString(), roles);
            boolean isHttps = true;
            requestContext.setSecurityContext(new AppSecurityContext(userAuth, isHttps));
        } catch (Exception e) {
            abort(requestContext, "Token inválido o expirado");
        }
    }

    private void abort(ContainerRequestContext requestContext, String msg) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"msg\" :\"" + msg + "\" }")
                .build());
    }
}
