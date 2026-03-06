package com.pinguela.rentexpress.rest.api.dto;

import com.pinguela.rentexpres.model.UserDTO;

/**
 * Respuesta del endpoint de autenticación con Google.
 * Incluye el JWT de la aplicación y los datos del usuario para la sesión.
 */
public class GoogleAuthResponse {

    private String token;
    private UserDTO user;

    public GoogleAuthResponse() {
    }

    public GoogleAuthResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
