package com.pinguela.rentexpress.rest.api.dto;

import java.util.List;

/**
 * Configuración de límites de subida de imagen (F9/F10).
 * El frontend puede consumir este endpoint en lugar de hardcodear reglas de negocio.
 */
public class ImageUploadConfigDTO {

    private long maxSizeBytes;
    private List<String> allowedMimeTypes;

    public ImageUploadConfigDTO() {}

    public ImageUploadConfigDTO(long maxSizeBytes, List<String> allowedMimeTypes) {
        this.maxSizeBytes = maxSizeBytes;
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }
}
