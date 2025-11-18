package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleStatusDTO;
import com.pinguela.rentexpres.service.VehicleStatusService;
import com.pinguela.rentexpres.service.impl.VehicleStatusServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/vehicle-statuses")
@Tag(name = "Vehicle Statuses", description = "Operations for vehicle status reference data")
public class ZOpenVehicleStatusResourse {

    private static final Logger logger = Logger.getLogger(ZOpenVehicleStatusResourse.class.getName());

    private final VehicleStatusService vehicleStatusService;

    public ZOpenVehicleStatusResourse() {
        this.vehicleStatusService = new VehicleStatusServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find all vehicle statuses")
    public Response findAll(@QueryParam("isoCode") String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode is required").build();
        }
        try {
            List<VehicleStatusDTO> statuses = vehicleStatusService.findAll(isoCode);
            if (statuses == null || statuses.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(statuses).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find vehicle status by ID")
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) {
        if (id == null || isoCode == null || isoCode.isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle status ID and isoCode are required").build();
        }
        try {
            VehicleStatusDTO status = vehicleStatusService.findById(id, isoCode);
            if (status == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(status).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
