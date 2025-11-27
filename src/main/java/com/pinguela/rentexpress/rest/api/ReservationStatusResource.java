package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationStatusDTO;
import com.pinguela.rentexpres.service.ReservationStatusService;
import com.pinguela.rentexpres.service.impl.ReservationStatusServiceImpl;
import com.pinguela.rentexpress.rest.api.util.LanguageResolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST resource for reservation status reference data.
 */
@Path("/reservation-status")
@Tag(name = "Reservation Statuses", description = "Operations for reservation status reference data")
public class ReservationStatusResource {

    private static final Logger logger = Logger.getLogger(ReservationStatusResource.class.getName());

    private final ReservationStatusService reservationStatusService;

    public ReservationStatusResource() {
        this.reservationStatusService = new ReservationStatusServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllReservationStatuses",
        summary = "Find all reservation statuses",
        description = "Retrieves every reservation status translated with the provided isoCode",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservation statuses retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationStatusDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No reservation statuses found"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving reservation statuses")
        }
    )
    public Response findAll(@QueryParam("isoCode") String isoCode, @HeaderParam("Accept-Language") String acceptLanguage) {
        String resolvedIsoCode = LanguageResolver.resolveIsoCode(isoCode, acceptLanguage);
        if (resolvedIsoCode == null) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode or Accept-Language header is required").build();
        }
        try {
            List<ReservationStatusDTO> statuses = reservationStatusService.findAll(resolvedIsoCode);
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
        operationId = "findReservationStatusById",
        summary = "Find reservation status by ID",
        description = "Retrieves a reservation status using its unique identifier and language code",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservation status retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationStatusDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reservation status not found"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation status identifier or isoCode supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the reservation status")
        }
    )
    public Response findById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode, @HeaderParam("Accept-Language") String acceptLanguage) {
        String resolvedIsoCode = LanguageResolver.resolveIsoCode(isoCode, acceptLanguage);
        if (id == null || resolvedIsoCode == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation status ID and isoCode or Accept-Language header are required").build();
        }
        try {
            ReservationStatusDTO status = reservationStatusService.findById(id, resolvedIsoCode);
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
