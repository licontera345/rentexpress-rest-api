package com.pinguela.rentexpress.rest.api.dto;

/**
 * Datos de Google para completar registro cuando el usuario no existe en la app.
 * El frontend redirige a registro con estos datos prefilled y guarda googleId al crear el usuario.
 * googleId corresponde al claim "sub" del JWT de Google (id único estable, ej: "109876543210987654321").
 */
public class GoogleRegistrationPayload {

    private String email;
    private String name;
    private String googleId;

    public GoogleRegistrationPayload() {
    }

    public GoogleRegistrationPayload(String email, String name, String googleId) {
        this.email = email;
        this.name = name;
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
}
