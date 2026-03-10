package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalStatusDTO;
import com.pinguela.rentexpres.service.RentalStatusService;

import jakarta.inject.Inject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
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
@Secured
@RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
public class RentalStatusResource {

    private static final Logger logger = Logger.getLogger(RentalStatusResource.class.getName());

    private final RentalStatusService rentalStatusService;

    @Inject
    public RentalStatusResource(RentalStatusService rentalStatusService) {
        this.rentalStatusService = rentalStatusService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllRentalStatuses",
        summary = "Find all rental statuses",
        description = "Retrieves every rental status translated with the provided isoCode",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rental statuses retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalStatusDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No rental statuses found"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving rental statuses")
        }
    )
    public Response findAll(@QueryParam("isoCode") String isoCode) throws RentexpresException {
        if (isoCode == null || isoCode.isEmpty()) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "isoCode is required");
        }
        List<RentalStatusDTO> statuses = rentalStatusService.findAll(isoCode);
        if (statuses == null || statuses.isEmpty()) {
            return Response.status(Status.NO_CONTENT).build();
        }
        return Response.ok(statuses).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findRentalStatusById",
        summary = "Find rental status by ID",
        description = "Retrieves a rental status using its unique identifier and language code",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rental status retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalStatusDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Rental status not found"),
            @ApiResponse(responseCode = "400", description = "Invalid rental status identifier or isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the rental status")
        }
    )
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) throws RentexpresException {
        if (id == null || isoCode == null || isoCode.isEmpty()) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental status ID and isoCode are required");
        }
        RentalStatusDTO status = rentalStatusService.findById(id, isoCode);
        if (status == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(status).build();
    }
}
