package com.pinguela.rentexpress.rest.api.auth.security;

import java.security.Principal;
import java.util.Set;

/**
 * Principal implementation holding username and roles.
 */
public class UserAuth implements Principal {

    private final String username;
    private final Set<String> roles;

    public UserAuth(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getName() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
