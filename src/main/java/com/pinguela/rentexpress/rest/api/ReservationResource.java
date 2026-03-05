package com.pinguela.rentexpress.rest.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.ReservationEstimateDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.ReservationService;
import com.google.gson.reflect.TypeToken;
import com.pinguela.rentexpress.rest.api.util.RedisCache;

import java.lang.reflect.Type;
import com.pinguela.rentexpres.service.impl.ReservationServiceImpl;
import com.pinguela.rentexpress.rest.api.dto.PickupCodeResponseDTO;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
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
public class ReservationResource {

	private static final Logger logger = Logger.getLogger(ReservationResource.class.getName());

	private final ReservationService reservationService;

	public ReservationResource() {
		this.reservationService = new ReservationServiceImpl();
	}

	@GET
	@Path("/{id}")
	@Secured
	@RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "findReservationById", summary = "Find reservation by ID", description = "Retrieves a reservation using its unique identifier", responses = {
			@ApiResponse(responseCode = "200", description = "Reservation retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))),
			@ApiResponse(responseCode = "404", description = "Reservation not found"),
			@ApiResponse(responseCode = "400", description = "Invalid reservation identifier supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the reservation") })
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
			return RentexpresExceptionMapper.toResponse(e);
		}
	}

	@GET
	@Path("/estimate")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getReservationEstimate", summary = "Get reservation estimate", description = "Returns estimated duration (days) and total for a reservation given daily price and date range. Business rule lives in backend.", responses = {
			@ApiResponse(responseCode = "200", description = "Estimate computed", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationEstimateDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid parameters") })
	public Response getEstimate(
			@QueryParam("dailyPrice") BigDecimal dailyPrice,
			@QueryParam("startDate") String startDateIso,
			@QueryParam("endDate") String endDateIso) {
		if (dailyPrice == null || startDateIso == null || startDateIso.trim().isEmpty() || endDateIso == null || endDateIso.trim().isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("dailyPrice, startDate and endDate are required (ISO-8601)").build();
		}
		try {
			LocalDateTime start = LocalDateTime.parse(startDateIso.trim());
			LocalDateTime end = LocalDateTime.parse(endDateIso.trim());
			ReservationEstimateDTO estimate = reservationService.calculateEstimate(dailyPrice, start, end);
			return Response.ok(estimate).build();
		} catch (Exception e) {
			logger.warning("getEstimate failed: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity("Invalid date format (use ISO-8601)").build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "createReservation", summary = "Create reservation", description = "Creates a new reservation with the provided information", responses = {
			@ApiResponse(responseCode = "201", description = "Reservation created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid reservation data supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while creating the reservation") })
	public Response create(ReservationDTO reservation) {
		if (reservation == null) {
			return Response.status(Status.BAD_REQUEST).entity("Reservation data is required").build();
		}
		if (reservation.getStartDate() != null && reservation.getEndDate() != null
				&& !reservation.getEndDate().isAfter(reservation.getStartDate())) {
			return Response.status(Status.BAD_REQUEST).entity("Invalid date range: end date must be after start date").build();
		}
		try {
			boolean created = reservationService.create(reservation);
			if (!created) {
				return Response.status(Status.BAD_REQUEST).entity("Reservation could not be created").build();
			}
			ReservationDTO createdReservation = reservation.getReservationId() != null
					? reservationService.findById(reservation.getReservationId())
					: reservation;
			RedisCache.deleteByPrefix("reservations:");
			return Response.status(Status.CREATED).entity(createdReservation).build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected error creating reservation: " + e.getMessage(), e);
			String detail = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.APPLICATION_JSON)
					.entity(new ErrorResponseDTO("INTERNAL", detail))
					.build();
		}
	}

	@PUT
	@Path("/{id}")
	@Secured
	@RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updateReservation", summary = "Update reservation", description = "Updates an existing reservation with the provided data", responses = {
			@ApiResponse(responseCode = "200", description = "Reservation updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))),
			@ApiResponse(responseCode = "400", description = "Reservation ID or data is invalid"),
			@ApiResponse(responseCode = "404", description = "Reservation not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while updating the reservation") })
	public Response update(@PathParam("id") Integer id, ReservationDTO reservation) {
		if (id == null || reservation == null) {
			return Response.status(Status.BAD_REQUEST).entity("Reservation ID and data are required").build();
		}
		reservation.setReservationId(id);
		if (reservation.getStartDate() != null && reservation.getEndDate() != null
				&& !reservation.getEndDate().isAfter(reservation.getStartDate())) {
			return Response.status(Status.BAD_REQUEST).entity("Invalid date range: end date must be after start date").build();
		}
		try {
			boolean updated = reservationService.update(reservation);
			if (!updated) {
				return Response.status(Status.NOT_FOUND).entity("Reservation not found or not updated").build();
			}
			ReservationDTO updatedReservation = reservationService.findById(reservation.getReservationId());
			RedisCache.deleteByPrefix("reservations:");
			return Response.ok(updatedReservation).build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}

	@DELETE
	@Path("/{id}")
	@Secured
	@RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "deleteReservation", summary = "Delete reservation", description = "Deletes a reservation using its unique identifier", responses = {
			@ApiResponse(responseCode = "200", description = "Reservation deleted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Reservation not found"),
			@ApiResponse(responseCode = "400", description = "Invalid reservation identifier supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while deleting the reservation") })
	public Response delete(@PathParam("id") Integer id) {
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).entity("Reservation ID is required").build();
		}
		try {
			boolean deleted = reservationService.delete(id);
			if (!deleted) {
				return Response.status(Status.NOT_FOUND).entity("Reservation not found").build();
			}
			RedisCache.deleteByPrefix("reservations:");
			return Response.ok().entity("Reservation deleted successfully").build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}

	@POST
	@Path("/{id}/generate-pickup-code")
	@Secured
	@RolesAllowed({ "ADMIN", "EMPLOYEE" })
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "generatePickupCode", summary = "Generate pickup code", description = "Generates a pickup verification code for a reservation and sends it to the customer via email", responses = {
			@ApiResponse(responseCode = "200", description = "Pickup code generated and sent successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid reservation ID"),
			@ApiResponse(responseCode = "500", description = "Error generating pickup code") })
	public Response generatePickupCode(@PathParam("id") Integer id) {
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).entity("Reservation ID is required").build();
		}
		try {
			String code = reservationService.generatePickupCode(id);
			RedisCache.deleteByPrefix("reservations:");
			return Response.ok(new PickupCodeResponseDTO(code)).build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}

	@GET
	@Path("/verify-code/{code}")
	@Secured
	@RolesAllowed({ "ADMIN", "EMPLOYEE" })
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "verifyPickupCode", summary = "Verify pickup code", description = "Verifies a pickup code and returns the associated reservation details", responses = {
			@ApiResponse(responseCode = "200", description = "Reservation found for the pickup code", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationDTO.class))),
			@ApiResponse(responseCode = "404", description = "No reservation found for the pickup code"),
			@ApiResponse(responseCode = "500", description = "Error verifying pickup code") })
	public Response verifyPickupCode(@PathParam("code") String code) {
		if (code == null || code.trim().isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("Pickup code is required").build();
		}
		if (code.trim().length() < 4) {
			return Response.status(Status.BAD_REQUEST).entity("Pickup code must be at least 4 characters").build();
		}
		try {
			ReservationDTO reservation = reservationService.findByPickupCode(code);
			if (reservation == null) {
				return Response.status(Status.NOT_FOUND).entity("No pending reservation found for this code").build();
			}
			return Response.ok(reservation).build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}

	@GET
	@Path("/search")
	@Secured
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
	@Operation(operationId = "searchReservations", summary = "Search reservations by criteria", description = "Retrieves reservations that match the provided search criteria", responses = {
			@ApiResponse(responseCode = "200", description = "Reservations matching the criteria were found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))),
			@ApiResponse(responseCode = "204", description = "No reservations matched the criteria"),
			@ApiResponse(responseCode = "400", description = "Search criteria is required"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while searching reservations") })
	public Response findByCriteria(@QueryParam("reservationId") Integer reservationId,
			@QueryParam("vehicleId") Integer vehicleId, @QueryParam("userId") Integer userId,
			@QueryParam("employeeId") Integer employeeId,
			@QueryParam("reservationStatusId") Integer reservationStatusId,
			@QueryParam("pickupHeadquartersId") Integer pickupHeadquartersId,
			@QueryParam("returnHeadquartersId") Integer returnHeadquartersId,
			@QueryParam("startDateFrom") java.time.LocalDateTime startDateFrom,
			@QueryParam("startDateTo") java.time.LocalDateTime startDateTo,
			@QueryParam("endDateFrom") java.time.LocalDateTime endDateFrom,
			@QueryParam("endDateTo") java.time.LocalDateTime endDateTo,
			@QueryParam("createdAtFrom") java.time.LocalDateTime createdAtFrom,
			@QueryParam("createdAtTo") java.time.LocalDateTime createdAtTo,
			@QueryParam("updatedAtFrom") java.time.LocalDateTime updatedAtFrom,
			@QueryParam("updatedAtTo") java.time.LocalDateTime updatedAtTo,
			@QueryParam("pageNumber") Integer pageNumber, @QueryParam("pageSize") Integer pageSize) {
		ReservationCriteria criteria = new ReservationCriteria();
		criteria.setReservationId(reservationId);
		criteria.setVehicleId(vehicleId);
		criteria.setUserId(userId);
		criteria.setEmployeeId(employeeId);
		criteria.setReservationStatusId(reservationStatusId);
		criteria.setPickupHeadquartersId(pickupHeadquartersId);
		criteria.setReturnHeadquartersId(returnHeadquartersId);
		criteria.setStartDateFrom(startDateFrom);
		criteria.setStartDateTo(startDateTo);
		criteria.setEndDateFrom(endDateFrom);	
		criteria.setEndDateTo(endDateTo);
		criteria.setCreatedAtFrom(createdAtFrom);
		criteria.setCreatedAtTo(createdAtTo);
		criteria.setUpdatedAtFrom(updatedAtFrom);
		criteria.setUpdatedAtTo(updatedAtTo);
		criteria.setPageNumber(pageNumber != null && pageNumber > 0 ? pageNumber : 1);
		criteria.setPageSize(pageSize != null && pageSize > 0 ? pageSize : 10);

		try {
			String cacheKey = "reservations:"
					+ RedisCache.keyPart(reservationId) + ":"
					+ RedisCache.keyPart(vehicleId) + ":"
					+ RedisCache.keyPart(userId) + ":"
					+ RedisCache.keyPart(employeeId) + ":"
					+ RedisCache.keyPart(reservationStatusId) + ":"
					+ RedisCache.keyPart(pickupHeadquartersId) + ":"
					+ RedisCache.keyPart(returnHeadquartersId) + ":"
					+ RedisCache.keyPart(startDateFrom) + ":"
					+ RedisCache.keyPart(startDateTo) + ":"
					+ RedisCache.keyPart(endDateFrom) + ":"
					+ RedisCache.keyPart(endDateTo) + ":"
					+ RedisCache.keyPart(createdAtFrom) + ":"
					+ RedisCache.keyPart(createdAtTo) + ":"
					+ RedisCache.keyPart(updatedAtFrom) + ":"
					+ RedisCache.keyPart(updatedAtTo) + ":"
					+ criteria.getPageNumber() + ":"
					+ criteria.getPageSize();
			Type type = new TypeToken<Results<ReservationDTO>>(){}.getType();
			Results<ReservationDTO> cached = RedisCache.getObject(cacheKey, type);
			if (cached != null) {
				logger.info("Cache HIT reservations");
				return Response.ok(cached).build();
			}
			logger.info("Cache MISS reservations");
			Results<ReservationDTO> results = reservationService.findByCriteria(criteria);
			if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
				return Response.status(Status.NO_CONTENT).build();
			}
			RedisCache.setObject(cacheKey, results, RedisCache.DEFAULT_TTL_SECONDS);
			return Response.ok(results).build();
		} catch (RentexpresException e) {
			return RentexpresExceptionMapper.toResponse(e);
		}
	}
}
