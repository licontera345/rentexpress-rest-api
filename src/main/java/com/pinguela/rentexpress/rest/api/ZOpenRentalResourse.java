package com.pinguela.rentexpress.rest.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.RentalService;
import com.pinguela.rentexpres.service.impl.RentalServiceImpl;
import com.pinguela.rentexpress.rest.api.param.QueryParamUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/rentals")
@Tag(name = "Rentals", description = "Operations for rental management")
public class ZOpenRentalResourse {

    private static final Logger logger = Logger.getLogger(ZOpenRentalResourse.class.getName());

    private final RentalService rentalService;

    public ZOpenRentalResourse() {
        this.rentalService = new RentalServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllRentals",
        summary = "Find all rentals",
        description = "Retrieves every rental available in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Rentals retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RentalDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No rentals found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving rentals")
        }
    )
    public Response findAll() {
        try {
            List<RentalDTO> rentals = rentalService.findAll();
            if (rentals == null || rentals.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(rentals).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response update(RentalDTO rental) {
        if (rental == null || rental.getRentalId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Rental ID and data are required").build();
        }
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
            @QueryParam("startDateEffectiveFrom") String startDateEffectiveFrom,
            @QueryParam("startDateEffectiveTo") String startDateEffectiveTo,
            @QueryParam("endDateEffectiveFrom") String endDateEffectiveFrom,
            @QueryParam("endDateEffectiveTo") String endDateEffectiveTo,
            @QueryParam("createdAtFrom") String createdAtFrom,
            @QueryParam("createdAtTo") String createdAtTo,
            @QueryParam("updatedAtFrom") String updatedAtFrom,
            @QueryParam("updatedAtTo") String updatedAtTo,
            @QueryParam("initialKmMin") Integer initialKmMin,
            @QueryParam("initialKmMax") Integer initialKmMax,
            @QueryParam("finalKmMin") Integer finalKmMin,
            @QueryParam("finalKmMax") Integer finalKmMax,
            @QueryParam("totalCostMin") Double totalCostMin,
            @QueryParam("totalCostMax") Double totalCostMax,
            @QueryParam("startDateEffective") String startDateEffective,
            @QueryParam("endDateEffective") String endDateEffective,
            @QueryParam("initialKm") Integer initialKm,
            @QueryParam("finalKm") Integer finalKm,
            @QueryParam("totalCost") Double totalCost,
            @QueryParam("userFirstName") String userFirstName,
            @QueryParam("userLastName1") String userLastName1,
            @QueryParam("phone") String phone,
            @QueryParam("licensePlate") String licensePlate,
            @QueryParam("brand") String brand,
            @QueryParam("model") String model,
            @QueryParam("pageNumber") Integer pageNumber,
            @QueryParam("pageSize") Integer pageSize) {
        try {
            RentalCriteria criteria = buildRentalCriteria(
                    rentalId,
                    rentalStatusId,
                    reservationId,
                    userId,
                    employeeId,
                    vehicleId,
                    pickupHeadquartersId,
                    returnHeadquartersId,
                    startDateEffectiveFrom,
                    startDateEffectiveTo,
                    endDateEffectiveFrom,
                    endDateEffectiveTo,
                    createdAtFrom,
                    createdAtTo,
                    updatedAtFrom,
                    updatedAtTo,
                    initialKmMin,
                    initialKmMax,
                    finalKmMin,
                    finalKmMax,
                    totalCostMin,
                    totalCostMax,
                    startDateEffective,
                    endDateEffective,
                    initialKm,
                    finalKm,
                    totalCost,
                    userFirstName,
                    userLastName1,
                    phone,
                    licensePlate,
                    brand,
                    model,
                    pageNumber,
                    pageSize);
            Results<RentalDTO> results = rentalService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(results).build();
        } catch (IllegalArgumentException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private RentalCriteria buildRentalCriteria(
            Integer rentalId,
            Integer rentalStatusId,
            Integer reservationId,
            Integer userId,
            Integer employeeId,
            Integer vehicleId,
            Integer pickupHeadquartersId,
            Integer returnHeadquartersId,
            String startDateEffectiveFrom,
            String startDateEffectiveTo,
            String endDateEffectiveFrom,
            String endDateEffectiveTo,
            String createdAtFrom,
            String createdAtTo,
            String updatedAtFrom,
            String updatedAtTo,
            Integer initialKmMin,
            Integer initialKmMax,
            Integer finalKmMin,
            Integer finalKmMax,
            Double totalCostMin,
            Double totalCostMax,
            String startDateEffective,
            String endDateEffective,
            Integer initialKm,
            Integer finalKm,
            Double totalCost,
            String userFirstName,
            String userLastName1,
            String phone,
            String licensePlate,
            String brand,
            String model,
            Integer pageNumber,
            Integer pageSize) {
        RentalCriteria criteria = new RentalCriteria();
        if (rentalId != null) {
            criteria.setRentalId(rentalId);
        }
        if (rentalStatusId != null) {
            criteria.setRentalStatusId(rentalStatusId);
        }
        if (reservationId != null) {
            criteria.setReservationId(reservationId);
        }
        if (userId != null) {
            criteria.setUserId(userId);
        }
        if (employeeId != null) {
            criteria.setEmployeeId(employeeId);
        }
        if (vehicleId != null) {
            criteria.setVehicleId(vehicleId);
        }
        if (pickupHeadquartersId != null) {
            criteria.setPickupHeadquartersId(pickupHeadquartersId);
        }
        if (returnHeadquartersId != null) {
            criteria.setReturnHeadquartersId(returnHeadquartersId);
        }
        LocalDateTime startFrom = QueryParamUtils.parseDateTime(startDateEffectiveFrom, "startDateEffectiveFrom");
        LocalDateTime startTo = QueryParamUtils.parseDateTime(startDateEffectiveTo, "startDateEffectiveTo");
        LocalDateTime endFrom = QueryParamUtils.parseDateTime(endDateEffectiveFrom, "endDateEffectiveFrom");
        LocalDateTime endTo = QueryParamUtils.parseDateTime(endDateEffectiveTo, "endDateEffectiveTo");
        LocalDateTime createdFrom = QueryParamUtils.parseDateTime(createdAtFrom, "createdAtFrom");
        LocalDateTime createdTo = QueryParamUtils.parseDateTime(createdAtTo, "createdAtTo");
        LocalDateTime updatedFrom = QueryParamUtils.parseDateTime(updatedAtFrom, "updatedAtFrom");
        LocalDateTime updatedTo = QueryParamUtils.parseDateTime(updatedAtTo, "updatedAtTo");
        LocalDateTime startExact = QueryParamUtils.parseDateTime(startDateEffective, "startDateEffective");
        LocalDateTime endExact = QueryParamUtils.parseDateTime(endDateEffective, "endDateEffective");
        if (startFrom != null) {
            criteria.setStartDateEffectiveFrom(startFrom);
        }
        if (startTo != null) {
            criteria.setStartDateEffectiveTo(startTo);
        }
        if (endFrom != null) {
            criteria.setEndDateEffectiveFrom(endFrom);
        }
        if (endTo != null) {
            criteria.setEndDateEffectiveTo(endTo);
        }
        if (createdFrom != null) {
            criteria.setCreatedAtFrom(createdFrom);
        }
        if (createdTo != null) {
            criteria.setCreatedAtTo(createdTo);
        }
        if (updatedFrom != null) {
            criteria.setUpdatedAtFrom(updatedFrom);
        }
        if (updatedTo != null) {
            criteria.setUpdatedAtTo(updatedTo);
        }
        if (startExact != null) {
            criteria.setStartDateEffective(startExact);
        }
        if (endExact != null) {
            criteria.setEndDateEffective(endExact);
        }
        if (initialKmMin != null) {
            criteria.setInitialKmMin(initialKmMin);
        }
        if (initialKmMax != null) {
            criteria.setInitialKmMax(initialKmMax);
        }
        if (finalKmMin != null) {
            criteria.setFinalKmMin(finalKmMin);
        }
        if (finalKmMax != null) {
            criteria.setFinalKmMax(finalKmMax);
        }
        if (totalCostMin != null) {
            criteria.setTotalCostMin(totalCostMin);
        }
        if (totalCostMax != null) {
            criteria.setTotalCostMax(totalCostMax);
        }
        if (initialKm != null) {
            criteria.setInitialKm(initialKm);
        }
        if (finalKm != null) {
            criteria.setFinalKm(finalKm);
        }
        if (totalCost != null) {
            criteria.setTotalCost(totalCost);
        }
        if (userFirstName != null) {
            criteria.setUserFirstName(userFirstName);
        }
        if (userLastName1 != null) {
            criteria.setUserLastName1(userLastName1);
        }
        if (phone != null) {
            criteria.setPhone(phone);
        }
        if (licensePlate != null) {
            criteria.setLicensePlate(licensePlate);
        }
        if (brand != null) {
            criteria.setBrand(brand);
        }
        if (model != null) {
            criteria.setModel(model);
        }
        if (pageNumber != null) {
            criteria.setPageNumber(pageNumber);
        }
        if (pageSize != null) {
            criteria.setPageSize(pageSize);
        }
        return criteria;
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
