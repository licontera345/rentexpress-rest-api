package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalStatusDTO;
import com.pinguela.rentexpres.service.RentalStatusService;
import com.pinguela.rentexpres.service.impl.RentalStatusServiceImpl;

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

@Path("/rental-statuses")
@Tag(name = "Rental Statuses", description = "Operations for rental status reference data")
public class ZOpenRentalStatusResourse {

    private static final Logger logger = Logger.getLogger(ZOpenRentalStatusResourse.class.getName());

    private final RentalStatusService rentalStatusService;

    public ZOpenRentalStatusResourse() {
        this.rentalStatusService = new RentalStatusServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find all rental statuses")
    public Response findAll(@QueryParam("isoCode") String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode is required").build();
        }
        try {
            List<RentalStatusDTO> statuses = rentalStatusService.findAll(isoCode);
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
    @Operation(summary = "Find rental status by ID")
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) {
        if (id == null || isoCode == null || isoCode.isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("Rental status ID and isoCode are required").build();
        }
        try {
            RentalStatusDTO status = rentalStatusService.findById(id, isoCode);
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
