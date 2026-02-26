package com.pinguela.rentexpress.rest.api.dto;

/**
 * DTO para la respuesta de generación de código de recogida.
 * Evita construir JSON manualmente en el recurso.
 */
public class PickupCodeResponseDTO {

    private String pickupCode;

    public PickupCodeResponseDTO() {}

    public PickupCodeResponseDTO(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }
}
