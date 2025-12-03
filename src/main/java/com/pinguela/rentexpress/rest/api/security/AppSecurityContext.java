package com.pinguela.rentexpress.rest.api.security;

import java.security.Principal;

import com.pinguela.rentexpress.rest.api.dto.UserAuth;

import jakarta.ws.rs.core.SecurityContext;

public class AppSecurityContext implements SecurityContext {

    private final UserAuth user;
    private final boolean secure;

    public AppSecurityContext(UserAuth user, boolean secure) {
        this.user = user;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return user.getRoles().contains(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
