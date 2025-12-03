package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleCategoryDTO;
import com.pinguela.rentexpres.service.VehicleCategoryService;
import com.pinguela.rentexpres.service.impl.VehicleCategoryServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/open/vehicle-categories")
@Tag(name = "Vehicle Categories", description = "Operations for vehicle category reference data")
public class ZOpenVehicleCategoryResource {

    private static final Logger logger = Logger.getLogger(ZOpenVehicleCategoryResource.class.getName());

    private final VehicleCategoryService vehicleCategoryService;

    public ZOpenVehicleCategoryResource() {
        this.vehicleCategoryService = new VehicleCategoryServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllVehicleCategories",
        summary = "Find all vehicle categories",
        description = "Retrieves every vehicle category translated with the provided isoCode",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle categories retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = VehicleCategoryDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No vehicle categories found"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving vehicle categories")
        }
    )
    public Response findAll(@QueryParam("isoCode") String isoCode) {
        if (isoCode == null || isoCode.isEmpty()) {
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
    @Operation(
        operationId = "findVehicleCategoryById",
        summary = "Find vehicle category by ID",
        description = "Retrieves a vehicle category using its unique identifier and language code",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle category retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = VehicleCategoryDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Vehicle category not found"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle category identifier or isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the vehicle category")
        }
    )
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) {
        if (id == null || isoCode == null || isoCode.isEmpty()) {
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
