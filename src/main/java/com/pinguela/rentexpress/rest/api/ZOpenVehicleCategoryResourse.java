package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleCategoryDTO;
import com.pinguela.rentexpres.service.VehicleCategoryService;
import com.pinguela.rentexpres.service.impl.VehicleCategoryServiceImpl;

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

@Path("/vehicle-categories")
@Tag(name = "Vehicle Categories", description = "Operations for vehicle category reference data")
public class ZOpenVehicleCategoryResourse {

    private static final Logger logger = Logger.getLogger(ZOpenVehicleCategoryResourse.class.getName());

    private final VehicleCategoryService vehicleCategoryService;

    public ZOpenVehicleCategoryResourse() {
        this.vehicleCategoryService = new VehicleCategoryServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find all vehicle categories")
    public Response findAll(@QueryParam("isoCode") String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode is required").build();
        }
        try {
            List<VehicleCategoryDTO> categories = vehicleCategoryService.findAll(isoCode);
            if (categories == null || categories.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(categories).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find vehicle category by ID")
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) {
        if (id == null || isoCode == null || isoCode.isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("Category ID and isoCode are required").build();
        }
        try {
            VehicleCategoryDTO category = vehicleCategoryService.findById(id, isoCode);
            if (category == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(category).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
