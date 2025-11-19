package com.pinguela.rentexpress.rest.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.ReservationService;
import com.pinguela.rentexpres.service.impl.ReservationServiceImpl;
import com.pinguela.rentexpress.rest.api.param.QueryParamUtils;

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

@Path("/reservations")
@Tag(name = "Reservations", description = "Operations for reservation management")
public class ZOpenReservationResourse {

    private static final Logger logger = Logger.getLogger(ZOpenReservationResourse.class.getName());

    private final ReservationService reservationService;

    public ZOpenReservationResourse() {
        this.reservationService = new ReservationServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllReservations",
        summary = "Find all reservations",
        description = "Retrieves every reservation available in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservations retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No reservations found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving reservations")
        }
    )
    public Response findAll() {
        try {
            List<ReservationDTO> reservations = reservationService.findAll();
            if (reservations == null || reservations.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(reservations).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findReservationById",
        summary = "Find reservation by ID",
        description = "Retrieves a reservation using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservation retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the reservation")
        }
    )
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation ID is required").build();
        }
        try {
            ReservationDTO reservation = reservationService.findById(id);
            if (reservation == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(reservation).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "createReservation",
        summary = "Create reservation",
        description = "Creates a new reservation with the provided information",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Reservation created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the reservation")
        }
    )
    public Response create(ReservationDTO reservation) {
        if (reservation == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation data is required").build();
        }
        try {
            boolean created = reservationService.create(reservation);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Reservation could not be created").build();
            }
            ReservationDTO createdReservation = reservation.getReservationId() != null
                    ? reservationService.findById(reservation.getReservationId())
                    : reservation;
            return Response.status(Status.CREATED).entity(createdReservation).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "updateReservation",
        summary = "Update reservation",
        description = "Updates an existing reservation with the provided data",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservation updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Reservation ID or data is invalid"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the reservation")
        }
    )
    public Response update(ReservationDTO reservation) {
        if (reservation == null || reservation.getReservationId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation ID and data are required").build();
        }
        try {
            boolean updated = reservationService.update(reservation);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Reservation not found or not updated").build();
            }
            ReservationDTO updatedReservation = reservationService.findById(reservation.getReservationId());
            return Response.ok(updatedReservation).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "deleteReservation",
        summary = "Delete reservation",
        description = "Deletes a reservation using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservation deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the reservation")
        }
    )
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Reservation ID is required").build();
        }
        try {
            boolean deleted = reservationService.delete(id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Reservation not found").build();
            }
            return Response.ok().entity("Reservation deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "searchReservations",
        summary = "Search reservations by criteria",
        description = "Retrieves reservations that match the provided search criteria",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reservations matching the criteria were found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))
            ),
            @ApiResponse(responseCode = "204", description = "No reservations matched the criteria"),
            @ApiResponse(responseCode = "400", description = "Search criteria is required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while searching reservations")
        }
    )
    public Response findByCriteria(
            @QueryParam("reservationId") Integer reservationId,
            @QueryParam("vehicleId") Integer vehicleId,
            @QueryParam("userId") Integer userId,
            @QueryParam("employeeId") Integer employeeId,
            @QueryParam("reservationStatusId") Integer reservationStatusId,
            @QueryParam("pickupHeadquartersId") Integer pickupHeadquartersId,
            @QueryParam("returnHeadquartersId") Integer returnHeadquartersId,
            @QueryParam("startDateFrom") String startDateFrom,
            @QueryParam("startDateTo") String startDateTo,
            @QueryParam("endDateFrom") String endDateFrom,
            @QueryParam("endDateTo") String endDateTo,
            @QueryParam("createdAtFrom") String createdAtFrom,
            @QueryParam("createdAtTo") String createdAtTo,
            @QueryParam("updatedAtFrom") String updatedAtFrom,
            @QueryParam("updatedAtTo") String updatedAtTo,
            @QueryParam("pageNumber") Integer pageNumber,
            @QueryParam("pageSize") Integer pageSize) {
        try {
            ReservationCriteria criteria = buildReservationCriteria(
                    reservationId,
                    vehicleId,
                    userId,
                    employeeId,
                    reservationStatusId,
                    pickupHeadquartersId,
                    returnHeadquartersId,
                    startDateFrom,
                    startDateTo,
                    endDateFrom,
                    endDateTo,
                    createdAtFrom,
                    createdAtTo,
                    updatedAtFrom,
                    updatedAtTo,
                    pageNumber,
                    pageSize);
            Results<ReservationDTO> results = reservationService.findByCriteria(criteria);
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

    private ReservationCriteria buildReservationCriteria(
            Integer reservationId,
            Integer vehicleId,
            Integer userId,
            Integer employeeId,
            Integer reservationStatusId,
            Integer pickupHeadquartersId,
            Integer returnHeadquartersId,
            String startDateFrom,
            String startDateTo,
            String endDateFrom,
            String endDateTo,
            String createdAtFrom,
            String createdAtTo,
            String updatedAtFrom,
            String updatedAtTo,
            Integer pageNumber,
            Integer pageSize) {
        ReservationCriteria criteria = new ReservationCriteria();
        if (reservationId != null) {
            criteria.setReservationId(reservationId);
        }
        if (vehicleId != null) {
            criteria.setVehicleId(vehicleId);
        }
        if (userId != null) {
            criteria.setUserId(userId);
        }
        if (employeeId != null) {
            criteria.setEmployeeId(employeeId);
        }
        if (reservationStatusId != null) {
            criteria.setReservationStatusId(reservationStatusId);
        }
        if (pickupHeadquartersId != null) {
            criteria.setPickupHeadquartersId(pickupHeadquartersId);
        }
        if (returnHeadquartersId != null) {
            criteria.setReturnHeadquartersId(returnHeadquartersId);
        }
        LocalDateTime startFromValue = QueryParamUtils.parseDateTime(startDateFrom, "startDateFrom");
        LocalDateTime startToValue = QueryParamUtils.parseDateTime(startDateTo, "startDateTo");
        LocalDateTime endFromValue = QueryParamUtils.parseDateTime(endDateFrom, "endDateFrom");
        LocalDateTime endToValue = QueryParamUtils.parseDateTime(endDateTo, "endDateTo");
        LocalDateTime createdFromValue = QueryParamUtils.parseDateTime(createdAtFrom, "createdAtFrom");
        LocalDateTime createdToValue = QueryParamUtils.parseDateTime(createdAtTo, "createdAtTo");
        LocalDateTime updatedFromValue = QueryParamUtils.parseDateTime(updatedAtFrom, "updatedAtFrom");
        LocalDateTime updatedToValue = QueryParamUtils.parseDateTime(updatedAtTo, "updatedAtTo");
        if (startFromValue != null) {
            criteria.setStartDateFrom(startFromValue);
        }
        if (startToValue != null) {
            criteria.setStartDateTo(startToValue);
        }
        if (endFromValue != null) {
            criteria.setEndDateFrom(endFromValue);
        }
        if (endToValue != null) {
            criteria.setEndDateTo(endToValue);
        }
        if (createdFromValue != null) {
            criteria.setCreatedAtFrom(createdFromValue);
        }
        if (createdToValue != null) {
            criteria.setCreatedAtTo(createdToValue);
        }
        if (updatedFromValue != null) {
            criteria.setUpdatedAtFrom(updatedFromValue);
        }
        if (updatedToValue != null) {
            criteria.setUpdatedAtTo(updatedToValue);
        }
        if (pageNumber != null) {
            criteria.setPageNumber(pageNumber);
        }
        if (pageSize != null) {
            criteria.setPageSize(pageSize);
        }
        return criteria;
    }
}
