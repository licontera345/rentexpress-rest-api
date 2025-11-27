package com.pinguela.rentexpress.rest.api.auth.security;

import java.security.Principal;

import jakarta.ws.rs.core.SecurityContext;

/**
 * Security context backed by {@link UserAuth} information.
 */
public class AppSecurityContext implements SecurityContext {

    private final UserAuth user;
    private final boolean secure;

    public AppSecurityContext(UserAuth user, boolean secure) {
        this.user = user;
        this.secure = secure;
    }

    public Principal getUserPrincipal() {
        return user;
    }

    public boolean isUserInRole(String role) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().contains(role);
    }

    public boolean isSecure() {
        return secure;
    }

    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
