package com.pinguela.rentexpress.rest.api.dto;

import com.pinguela.rentexpres.model.UserDTO;

/**
 * Respuesta del endpoint de autenticación con Google.
 * Si el usuario ya existe: token + user.
 * Si no existe: needsRegistration=true y googlePayload para redirigir a registro.
 */
public class GoogleAuthResponse {

    private String token;
    private UserDTO user;
    /** true cuando el usuario no está en la BD y debe completar registro con googlePayload */
    private boolean needsRegistration;
    private GoogleRegistrationPayload googlePayload;

    public GoogleAuthResponse() {
    }

    /** Respuesta de login OK: usuario existente */
    public GoogleAuthResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
        this.needsRegistration = false;
        this.googlePayload = null;
    }

    /** Respuesta “debe registrarse”: sin token, con datos de Google para el formulario */
    public static GoogleAuthResponse needsRegistration(String email, String name, String googleId) {
        GoogleAuthResponse r = new GoogleAuthResponse();
        r.setNeedsRegistration(true);
        r.setGooglePayload(new GoogleRegistrationPayload(email, name, googleId));
        return r;
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

    public boolean isNeedsRegistration() {
        return needsRegistration;
    }

    public void setNeedsRegistration(boolean needsRegistration) {
        this.needsRegistration = needsRegistration;
    }

    public GoogleRegistrationPayload getGooglePayload() {
        return googlePayload;
    }

    public void setGooglePayload(GoogleRegistrationPayload googlePayload) {
        this.googlePayload = googlePayload;
    }
}

