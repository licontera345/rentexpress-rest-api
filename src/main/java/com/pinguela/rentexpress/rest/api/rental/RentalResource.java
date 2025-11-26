package com.pinguela.rentexpress.rest.api.rental;

import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.RentalService;
import com.pinguela.rentexpres.service.impl.RentalServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Private rental resource.
 */
@Path("/rental")
@Tag(name = "Rentals", description = "Operations for rental management")
public class RentalResource {

    private static final Logger logger = Logger.getLogger(RentalResource.class.getName());

    private final RentalService rentalService;

    public RentalResource() {
        this.rentalService = new RentalServiceImpl();
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
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "400", description = "Invalid rental identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the rental")
        }
    )
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Rental ID is required").build();
        }
        try {
            RentalDTO rental = rentalService.findById(id);
            if (rental == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(rental).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
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
    public Response create(RentalDTO rental) {
        if (rental == null) {
            return Response.status(Status.BAD_REQUEST).entity("Rental data is required").build();
        }
        try {
            boolean created = rentalService.create(rental);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Rental could not be created").build();
            }
            RentalDTO createdRental = rental.getRentalId() != null
                    ? rentalService.findById(rental.getRentalId())
                    : rental;
            return Response.status(Status.CREATED).entity(createdRental).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response update(@PathParam("id") Integer id, RentalDTO rental) {
        if (id == null || rental == null) {
            return Response.status(Status.BAD_REQUEST).entity("Rental ID and data are required").build();
        }
        rental.setRentalId(id);
        try {
            boolean updated = rentalService.update(rental);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Rental not found or not updated").build();
            }
            RentalDTO updatedRental = rentalService.findById(rental.getRentalId());
            return Response.ok(updatedRental).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Rental ID is required").build();
        }
        try {
            boolean deleted = rentalService.delete(id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Rental not found").build();
            }
            return Response.ok().entity("Rental deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "searchRentals",
        summary = "Search rentals by criteria",
        description = "Retrieves rentals that match the provided search criteria",
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
        @QueryParam("rentalId") Integer rentalId,
        @QueryParam("rentalStatusId") Integer rentalStatusId,
        @QueryParam("reservationId") Integer reservationId,
        @QueryParam("userId") Integer userId,
        @QueryParam("employeeId") Integer employeeId,
        @QueryParam("vehicleId") Integer vehicleId,
        @QueryParam("pickupHeadquartersId") Integer pickupHeadquartersId,
        @QueryParam("returnHeadquartersId") Integer returnHeadquartersId,
        @QueryParam("startDateEffectiveFrom") java.time.LocalDateTime startDateEffectiveFrom,
        @QueryParam("startDateEffectiveTo") java.time.LocalDateTime startDateEffectiveTo,
        @QueryParam("endDateEffectiveFrom") java.time.LocalDateTime endDateEffectiveFrom,
        @QueryParam("endDateEffectiveTo") java.time.LocalDateTime endDateEffectiveTo,
        @QueryParam("createdAtFrom") java.time.LocalDateTime createdAtFrom,
        @QueryParam("createdAtTo") java.time.LocalDateTime createdAtTo,
        @QueryParam("updatedAtFrom") java.time.LocalDateTime updatedAtFrom,
        @QueryParam("updatedAtTo") java.time.LocalDateTime updatedAtTo,
        @QueryParam("initialKmMin") Integer initialKmMin,
        @QueryParam("initialKmMax") Integer initialKmMax,
        @QueryParam("finalKmMin") Integer finalKmMin,
        @QueryParam("finalKmMax") Integer finalKmMax,
        @QueryParam("totalCostMin") java.math.BigDecimal totalCostMin,
        @QueryParam("totalCostMax") java.math.BigDecimal totalCostMax,
        @QueryParam("startDateEffective") java.time.LocalDateTime startDateEffective,
        @QueryParam("endDateEffective") java.time.LocalDateTime endDateEffective,
        @QueryParam("initialKm") Integer initialKm,
        @QueryParam("finalKm") Integer finalKm,
        @QueryParam("totalCost") java.math.BigDecimal totalCost,
        @QueryParam("userFirstName") String userFirstName,
        @QueryParam("userLastName1") String userLastName1,
        @QueryParam("phone") String phone,
        @QueryParam("licensePlate") String licensePlate,
        @QueryParam("brand") String brand,
        @QueryParam("model") String model,
        @QueryParam("pageNumber") Integer pageNumber,
        @QueryParam("pageSize") Integer pageSize
    ) {
        RentalCriteria criteria = new RentalCriteria();
        criteria.setRentalId(rentalId);
        criteria.setRentalStatusId(rentalStatusId);
        criteria.setReservationId(reservationId);
        criteria.setUserId(userId);
        criteria.setEmployeeId(employeeId);
        criteria.setVehicleId(vehicleId);
        criteria.setPickupHeadquartersId(pickupHeadquartersId);
        criteria.setReturnHeadquartersId(returnHeadquartersId);
        criteria.setStartDateEffectiveFrom(startDateEffectiveFrom);
        criteria.setStartDateEffectiveTo(startDateEffectiveTo);
        criteria.setEndDateEffectiveFrom(endDateEffectiveFrom);
        criteria.setEndDateEffectiveTo(endDateEffectiveTo);
        criteria.setCreatedAtFrom(createdAtFrom);
        criteria.setCreatedAtTo(createdAtTo);
        criteria.setUpdatedAtFrom(updatedAtFrom);
        criteria.setUpdatedAtTo(updatedAtTo);
        criteria.setInitialKmMin(initialKmMin);
        criteria.setInitialKmMax(initialKmMax);
        criteria.setFinalKmMin(finalKmMin);
        criteria.setFinalKmMax(finalKmMax);
        criteria.setTotalCostMin(totalCostMin);
        criteria.setTotalCostMax(totalCostMax);
        criteria.setStartDateEffective(startDateEffective);
        criteria.setEndDateEffective(endDateEffective);
        criteria.setInitialKm(initialKm);
        criteria.setFinalKm(finalKm);
        criteria.setTotalCost(totalCost);
        criteria.setUserFirstName(userFirstName);
        criteria.setUserLastName1(userLastName1);
        criteria.setPhone(phone);
        criteria.setLicensePlate(licensePlate);
        criteria.setBrand(brand);
        criteria.setModel(model);
        criteria.setPageNumber(pageNumber);
        criteria.setPageSize(pageSize);
        try {
            Results<RentalDTO> results = rentalService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(results).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response existsByReservation(@PathParam("reservationId") Integer reservationId) {
        if (reservationId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation ID is required").build();
        }
        try {
            boolean exists = rentalService.existsByReservation(reservationId);
            return Response.ok(exists).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response createFromReservation(ReservationDTO reservation) {
        if (reservation == null || reservation.getReservationId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation data is required").build();
        }
        try {
            rentalService.createFromReservation(reservation);
            return Response.status(Status.CREATED).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response autoConvertReservations() {
        try {
            int converted = rentalService.autoConvertReservations();
            return Response.ok(converted).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
