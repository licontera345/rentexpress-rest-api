package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.auth.filter.Secured;
import com.pinguela.rentexpress.rest.api.auth.util.LanguageResolver;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleStatusDTO;
import com.pinguela.rentexpres.service.VehicleStatusService;
import com.pinguela.rentexpres.service.impl.VehicleStatusServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/vehicle-status")
@Secured
@RolesAllowed({"EMPLOYEE", "USER"})
@Tag(name = "Vehicle Statuses", description = "Protected operations for vehicle status reference data")
public class VehicleStatusResource {

    private static final Logger logger = Logger.getLogger(VehicleStatusResource.class.getName());

    private final VehicleStatusService vehicleStatusService;

    public VehicleStatusResource() {
        this.vehicleStatusService = new VehicleStatusServiceImpl();
    }

    public VehicleStatusResource(VehicleStatusService vehicleStatusService) {
        this.vehicleStatusService = vehicleStatusService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllVehicleStatuses", 
        summary = "Find all vehicle statuses",
        description = "Retrieves every vehicle status translated with the provided isoCode",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle statuses retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = VehicleStatusDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No vehicle statuses found"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving vehicle statuses")
        }
    )
    public Response findAll(@QueryParam("isoCode") String isoCode, @HeaderParam("Accept-Language") String acceptLanguage) {
        String resolvedIsoCode = LanguageResolver.resolveIsoCode(isoCode, acceptLanguage);
        if (resolvedIsoCode == null) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode or Accept-Language header is required").build();
        }
        try {
            List<VehicleStatusDTO> statuses = vehicleStatusService.findAll(resolvedIsoCode);
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
    @Operation(
        operationId = "findVehicleStatusById",
        summary = "Find vehicle status by ID",
        description = "Retrieves a vehicle status using its unique identifier and language code",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle status retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = VehicleStatusDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Vehicle status not found"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle status identifier or isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the vehicle status")
        }
    )
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode, @HeaderParam("Accept-Language") String acceptLanguage) {
        String resolvedIsoCode = LanguageResolver.resolveIsoCode(isoCode, acceptLanguage);
        if (id == null || resolvedIsoCode == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle status ID and isoCode or Accept-Language header are required").build();
        }
        try {
            VehicleStatusDTO status = vehicleStatusService.findById(id, resolvedIsoCode);
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
