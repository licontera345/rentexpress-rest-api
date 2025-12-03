package com.pinguela.rentexpress.rest.api.dto;

import java.security.Principal;
import java.util.Set;

public class UserAuth implements Principal {

    private final String idUser;
    private final Set<String> roles;

    public UserAuth(String idUser, Set<String> roles) {
        this.idUser = idUser;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return idUser;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
