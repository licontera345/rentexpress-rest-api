package com.pinguela.rentexpress.rest.api.rental;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.RentalService;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpress.rest.api.util.RedisCache;

import jakarta.inject.Inject;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

@Path("/rentals")
@Tag(name = "Rentals", description = "Operations for rental management")
@Secured
@RolesAllowed({ "ADMIN", "EMPLOYEE" })
public class RentalResource {

    private static final Logger logger = Logger.getLogger(RentalResource.class.getName());

    private final RentalService rentalService;

    @Inject
    public RentalResource(RentalService rentalService) {
        this.rentalService = rentalService;
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findRentalById",
        summary = "Find rental by ID",
        description = "Retrieves a rental using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rental retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalDTO.class))
            ),
            @ApiResponse(responseCode = "304", description = "Not Modified (cache válido)"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "400", description = "Invalid rental identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the rental")
        }
    )
    public Response findById(@PathParam("id") Integer id, @Context Request request) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental ID is required");
        }
        RentalDTO rental = rentalService.findById(id);
        if (rental == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String etagValue = computeETag(rental);
        EntityTag etag = new EntityTag(etagValue);
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.build();
        }
        return Response.ok(rental).tag(etag).build();
    }

    /**
     * Computes a weak ETag from rental identifier and updated timestamp for cache revalidation (304 Not Modified).
     */
    private static String computeETag(RentalDTO rental) {
        StringBuilder sb = new StringBuilder();
        sb.append(rental.getRentalId() != null ? rental.getRentalId() : "");
        if (rental.getUpdatedAt() != null) {
            sb.append(":").append(rental.getUpdatedAt().toString());
        } else {
            sb.append(":").append(System.currentTimeMillis());
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return "W/\"" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            return "W/\"" + Integer.toHexString(sb.toString().hashCode()) + "\"";
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "createRental",
        summary = "Create rental",
        description = "Creates a new rental record in the system",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Rental created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid rental data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the rental")
        }
    )
    public Response create(RentalDTO rental) throws RentexpresException {
        if (rental == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental data is required");
        }
        String validationError = validateRentalBusinessRules(rental);
        if (validationError != null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", validationError);
        }
        boolean created = rentalService.create(rental);
        if (!created) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental could not be created");
        }
        RentalDTO createdRental = rental.getRentalId() != null
                ? rentalService.findById(rental.getRentalId())
                : rental;
        RedisCache.deleteByPrefix("rentals:");
        return Response.status(Status.CREATED).entity(createdRental).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "updateRental",
        summary = "Update rental",
        description = "Updates an existing rental with the provided data",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rental updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid rental data supplied"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the rental")
        }
    )
    public Response update(@PathParam("id") Integer id, RentalDTO rental) throws RentexpresException {
        if (id == null || rental == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental ID and data are required");
        }
        rental.setRentalId(id);
        String validationError = validateRentalBusinessRules(rental);
        if (validationError != null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", validationError);
        }
        boolean updated = rentalService.update(rental);
        if (!updated) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Rental not found or not updated");
        }
        RentalDTO updatedRental = rentalService.findById(rental.getRentalId());
        RedisCache.deleteByPrefix("rentals:");
        return Response.ok(updatedRental).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "deleteRental",
        summary = "Delete rental",
        description = "Deletes a rental using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rental deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "400", description = "Invalid rental identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the rental")
        }
    )
    public Response delete(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental ID is required");
        }
        boolean deleted = rentalService.delete(id);
        if (!deleted) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Rental not found");
        }
        RedisCache.deleteByPrefix("rentals:");
        return ErrorResponseHelper.ok("OK", "Rental deleted successfully");
    }

    @GET
    @Path("/search")
    @RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "searchRentals",
        summary = "Search rentals by criteria",
        description = "Retrieves rentals that match the provided search criteria. Clients may only see their own rentals (userId is enforced from token).",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Search executed successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))
            ),
            @ApiResponse(responseCode = "204", description = "No rentals found for the provided criteria"),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while searching for rentals")
        }
    )
    public Response findByCriteria(
        @Context SecurityContext securityContext,
        @QueryParam("rentalId") Integer rentalId,
        @QueryParam("rentalStatusId") Integer rentalStatusId,
        @QueryParam("userId") Integer userId,
        @QueryParam("pickupHeadquartersId") Integer pickupHeadquartersId,
        @QueryParam("returnHeadquartersId") Integer returnHeadquartersId,
        @QueryParam("startDateEffectiveFrom") java.time.LocalDateTime startDateEffectiveFrom,
        @QueryParam("startDateEffectiveTo") java.time.LocalDateTime startDateEffectiveTo,
        @QueryParam("endDateEffectiveFrom") java.time.LocalDateTime endDateEffectiveFrom,
        @QueryParam("endDateEffectiveTo") java.time.LocalDateTime endDateEffectiveTo,
        @QueryParam("totalCostMin") java.math.BigDecimal totalCostMin,
        @QueryParam("totalCostMax") java.math.BigDecimal totalCostMax,
        @QueryParam("startDateEffective") java.time.LocalDateTime startDateEffective,
        @QueryParam("endDateEffective") java.time.LocalDateTime endDateEffective,
        @QueryParam("initialKm") Integer initialKm,
        @QueryParam("finalKm") Integer finalKm,
        @QueryParam("totalCost") java.math.BigDecimal totalCost,
        @QueryParam("pageNumber") Integer pageNumber,
        @QueryParam("pageSize") Integer pageSize
    ) throws RentexpresException {
        RentalCriteria criteria = new RentalCriteria();
        criteria.setRentalId(rentalId);
        criteria.setRentalStatusId(rentalStatusId);
        criteria.setPickupHeadquartersId(pickupHeadquartersId);
        criteria.setReturnHeadquartersId(returnHeadquartersId);
        criteria.setStartDateEffectiveFrom(startDateEffectiveFrom);
        criteria.setStartDateEffectiveTo(startDateEffectiveTo);
        criteria.setEndDateEffectiveFrom(endDateEffectiveFrom);
        criteria.setEndDateEffectiveTo(endDateEffectiveTo);
        criteria.setTotalCostMin(totalCostMin);
        criteria.setTotalCostMax(totalCostMax);
        criteria.setStartDateEffective(startDateEffective);
        criteria.setEndDateEffective(endDateEffective);
        criteria.setInitialKm(initialKm);
        criteria.setFinalKm(finalKm);
        criteria.setTotalCost(totalCost);
        criteria.setPageNumber(pageNumber);
        criteria.setPageSize(pageSize);
        int defaultPageNumber = 1;
        int defaultPageSize = 10;

        criteria.setPageNumber(pageNumber != null && pageNumber > 0 ? pageNumber : defaultPageNumber);
        criteria.setPageSize(pageSize != null && pageSize > 0 ? pageSize : defaultPageSize);

        // Contexto para el middleware (A2): el middleware aplica la regla "CLIENT solo ve sus alquileres".
        String cacheContext = "_";
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            try {
                int principalId = Integer.parseInt(securityContext.getUserPrincipal().getName());
                if (securityContext.isUserInRole("CLIENT") || securityContext.isUserInRole("Cliente") || securityContext.isUserInRole("user")) {
                    criteria.setRequestingUserId(principalId);
                    criteria.setRequestingUserRole("CLIENT");
                    criteria.setUserId(null);
                    cacheContext = "c" + principalId;
                } else {
                    criteria.setUserId(userId);
                    cacheContext = "a" + RedisCache.keyPart(userId);
                }
            } catch (NumberFormatException e) {
                return ErrorResponseHelper.badRequest("BAD_REQUEST", "Invalid user context");
            }
        } else {
            criteria.setUserId(userId);
        }

        String cacheKey = "rentals:"
                    + cacheContext + ":"
                    + RedisCache.keyPart(rentalId) + ":"
                    + RedisCache.keyPart(rentalStatusId) + ":"
                    + RedisCache.keyPart(pickupHeadquartersId) + ":"
                    + RedisCache.keyPart(returnHeadquartersId) + ":"
                    + RedisCache.keyPart(startDateEffectiveFrom) + ":"
                    + RedisCache.keyPart(startDateEffectiveTo) + ":"
                    + RedisCache.keyPart(endDateEffectiveFrom) + ":"
                    + RedisCache.keyPart(endDateEffectiveTo) + ":"
                    + RedisCache.keyPart(totalCostMin) + ":"
                    + RedisCache.keyPart(totalCostMax) + ":"
                    + RedisCache.keyPart(startDateEffective) + ":"
                    + RedisCache.keyPart(endDateEffective) + ":"
                    + RedisCache.keyPart(initialKm) + ":"
                    + RedisCache.keyPart(finalKm) + ":"
                    + RedisCache.keyPart(totalCost) + ":"
                    + criteria.getPageNumber() + ":"
                    + criteria.getPageSize();
            Type type = new TypeToken<Results<RentalDTO>>(){}.getType();
            Results<RentalDTO> cached = RedisCache.getObject(cacheKey, type);
            if (cached != null) {
                return Response.ok(cached).build();
            }
            Results<RentalDTO> results = rentalService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            RedisCache.setObject(cacheKey, results, RedisCache.DEFAULT_TTL_SECONDS);
            return Response.ok(results).build();
    }

    @GET
    @Path("/reservations/{reservationId}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "existsRentalByReservation",
        summary = "Check rental existence by reservation",
        description = "Determines whether a rental exists for the specified reservation",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Existence check completed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid reservation identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while checking rental existence")
        }
    )
    public Response existsByReservation(@PathParam("reservationId") Integer reservationId) throws RentexpresException {
        if (reservationId == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Reservation ID is required");
        }
        boolean exists = rentalService.existsByReservation(reservationId);
        return Response.ok(exists).build();
    }

    @POST
    @Path("/from-reservation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "createRentalFromReservation",
        summary = "Create rental from reservation",
        description = "Creates a rental entity using data from an existing reservation",
        responses = {
            @ApiResponse(responseCode = "201", description = "Rental created successfully from reservation"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the rental")
        }
    )
    public Response createFromReservation(ReservationDTO reservation) throws RentexpresException {
        if (reservation == null || reservation.getReservationId() == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Reservation data is required");
        }
        rentalService.createFromReservation(reservation);
        RedisCache.deleteByPrefix("rentals:");
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/{id}/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "completeRental",
        summary = "Complete rental (vehicle return)",
        description = "Marks a rental as completed when the vehicle is returned. Calculates late fees if applicable (59-min grace period, then 1.5x daily rate per extra day).",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rental completed successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid rental ID"),
            @ApiResponse(responseCode = "500", description = "Error completing rental")
        }
    )
    public Response completeRental(@PathParam("id") Integer id, RentalDTO rentalData) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Rental ID is required");
        }
        Integer finalKm = rentalData != null ? rentalData.getFinalKm() : null;
        RentalDTO completed = rentalService.completeRental(id, finalKm);
        if (completed == null) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Rental not found after completion");
        }
        RedisCache.deleteByPrefix("rentals:");
        return Response.ok(completed).build();
    }

    @POST
    @Path("/auto-convert")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "autoConvertReservations",
        summary = "Auto convert reservations into rentals",
        description = "Converts eligible reservations into rentals automatically",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservations converted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Integer.class))
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error while converting reservations")
        }
    )
    public Response autoConvertReservations() throws RentexpresException {
        int converted = rentalService.autoConvertReservations();
        RedisCache.deleteByPrefix("rentals:");
        return Response.ok(converted).build();
    }

    /** Validaciones de negocio para rental (F3); la API rechaza con 400 si no se cumplen. */
    private static String validateRentalBusinessRules(RentalDTO rental) {
        if (rental.getTotalCost() != null && rental.getTotalCost().compareTo(BigDecimal.ZERO) < 0) {
            return "Total cost cannot be negative";
        }
        if (rental.getInitialKm() != null && rental.getFinalKm() != null && rental.getFinalKm() < rental.getInitialKm()) {
            return "Final km cannot be less than initial km";
        }
        if (rental.getStartDateEffective() != null && rental.getEndDateEffective() != null
                && !rental.getEndDateEffective().isAfter(rental.getStartDateEffective())) {
            return "End date must be after start date";
        }
        return null;
    }
}
