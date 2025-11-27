package com.pinguela.rentexpress.rest.api;

import com.pinguela.rentexpress.rest.api.auth.filter.Secured;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Path;

@Path("/vehicle-status")
@Secured
@RolesAllowed({"EMPLOYEE", "USER"})
@Tag(name = "Vehicle Statuses", description = "Protected operations for vehicle status reference data")
public class VehicleStatusResource {

    /**
     * Protected resource stub for secured vehicle status operations.
     */
    public VehicleStatusResource() {
        // Intentionally left blank to preserve secured endpoint placeholder.
    }
}
