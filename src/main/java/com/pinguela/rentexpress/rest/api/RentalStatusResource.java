package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalStatusDTO;
import com.pinguela.rentexpres.service.RentalStatusService;
import com.pinguela.rentexpres.service.impl.RentalStatusServiceImpl;
import com.pinguela.rentexpress.rest.api.auth.util.LanguageResolver;

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
import com.pinguela.rentexpress.rest.api.auth.filter.Secured;

@Path("/rental-status")
@Secured
@RolesAllowed({"EMPLOYEE", "USER"})
@Tag(name = "Rental Statuses", description = "Protected operations for rental status reference data (requires authentication)")
public class RentalStatusResource {

    private static final Logger logger = Logger.getLogger(RentalStatusResource.class.getName());

    private final RentalStatusService rentalStatusService;

    public RentalStatusResource() {
        this.rentalStatusService = new RentalStatusServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllRentalStatuses",
        summary = "Find all rental statuses",
        description = "Retrieves every rental status translated with the provided isoCode (requires authentication)",
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
    public Response findAll(@QueryParam("isoCode") String isoCode, @HeaderParam("Accept-Language") String acceptLanguage) {
        String resolvedIsoCode = LanguageResolver.resolveIsoCode(isoCode, acceptLanguage);
        if (resolvedIsoCode == null) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode or Accept-Language header is required").build();
        }
        try {
            List<RentalStatusDTO> statuses = rentalStatusService.findAll(resolvedIsoCode);
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
        operationId = "findRentalStatusById",
        summary = "Find rental status by ID",
        description = "Retrieves a rental status using its unique identifier and language code (requires authentication)",
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
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode, @HeaderParam("Accept-Language") String acceptLanguage) {
        String resolvedIsoCode = LanguageResolver.resolveIsoCode(isoCode, acceptLanguage);
        if (id == null || resolvedIsoCode == null) {
            return Response.status(Status.BAD_REQUEST).entity("Rental status ID and isoCode or Accept-Language header are required").build();
        }
        try {
            RentalStatusDTO status = rentalStatusService.findById(id, resolvedIsoCode);
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
