package com.pinguela.rentexpress.rest.api.statistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpres.model.DashboardStatsDTO;
import com.pinguela.rentexpres.model.HeadquartersStatsDTO;
import com.pinguela.rentexpres.model.ReservationStatsDTO;
import com.pinguela.rentexpres.model.RevenueByPeriodDTO;
import com.pinguela.rentexpres.model.VehicleFleetStatsDTO;
import com.pinguela.rentexpres.service.StatisticsService;

import jakarta.inject.Inject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/statistics")
@Tag(name = "Statistics", description = "Dashboard and reporting statistics")
@Secured
@RolesAllowed({ "ADMIN", "EMPLOYEE" })
public class StatisticsResource {

	private static final Logger logger = Logger.getLogger(StatisticsResource.class.getName());

	private final StatisticsService statisticsService;

	@Inject
	public StatisticsResource(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	@GET
	@Path("/dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "getDashboardStats",
		summary = "Get dashboard statistics",
		description = "Retrieves a summary of key business metrics: revenue, rentals, reservations, fleet status and clients. "
				+ "Optionally filtered by date range (from/to). If omitted, returns global totals.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Dashboard statistics retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DashboardStatsDTO.class))
			),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving dashboard statistics")
		}
	)
	public Response getDashboardStats(
			@QueryParam("from") LocalDateTime from,
			@QueryParam("to") LocalDateTime to) {
		DashboardStatsDTO stats = statisticsService.getDashboardStats(from, to);
		return Response.ok(stats).build();
	}

	@GET
	@Path("/revenue")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "getTotalRevenue",
		summary = "Get total revenue",
		description = "Returns total revenue from completed rentals. "
				+ "Optionally filtered by date range (from/to). If omitted, returns all-time revenue.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Total revenue retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BigDecimal.class))
			),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving revenue")
		}
	)
	public Response getTotalRevenue(
			@QueryParam("from") LocalDateTime from,
			@QueryParam("to") LocalDateTime to) {
		BigDecimal revenue = statisticsService.getTotalRevenue(from, to);
		return Response.ok(revenue).build();
	}

	@GET
	@Path("/revenue/monthly")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "getRevenueByMonth",
		summary = "Get monthly revenue for a given year",
		description = "Returns revenue and rental count broken down by month for the specified year. Useful for trend charts.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Monthly revenue retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RevenueByPeriodDTO[].class))
			),
			@ApiResponse(responseCode = "204", description = "No revenue data found for the specified year"),
			@ApiResponse(responseCode = "400", description = "Year parameter is required"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving monthly revenue")
		}
	)
	public Response getRevenueByMonth(@QueryParam("year") Integer year) {
		if (year == null) {
			return ErrorResponseHelper.badRequest("BAD_REQUEST", "year is required");
		}
		List<RevenueByPeriodDTO> list = statisticsService.getRevenueByMonth(year);
		if (list == null || list.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}
		// Etiqueta de periodo es presentación; la API la construye desde year/month (M3).
		for (RevenueByPeriodDTO dto : list) {
			if (dto.getPeriodLabel() == null && dto.getYear() != null && dto.getMonth() != null) {
				dto.setPeriodLabel(String.format("%d-%02d", dto.getYear(), dto.getMonth()));
			}
		}
		return Response.ok(list).build();
	}

	@GET
	@Path("/reservations")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "getReservationStats",
		summary = "Get reservation statistics by status",
		description = "Returns the count of reservations grouped by status (Pending, Confirmed, Canceled).",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Reservation statistics retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReservationStatsDTO[].class))
			),
			@ApiResponse(responseCode = "204", description = "No reservation data found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving reservation statistics")
		}
	)
	public Response getReservationStats() {
		List<ReservationStatsDTO> list = statisticsService.getReservationStats();
		if (list == null || list.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.ok(list).build();
	}

	@GET
	@Path("/fleet")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "getVehicleFleetStats",
		summary = "Get vehicle fleet statistics by status",
		description = "Returns the count of vehicles grouped by status (Available, Maintenance, Rented).",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Fleet statistics retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = VehicleFleetStatsDTO[].class))
			),
			@ApiResponse(responseCode = "204", description = "No fleet data found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving fleet statistics")
		}
	)
	public Response getVehicleFleetStats() {
		List<VehicleFleetStatsDTO> list = statisticsService.getVehicleFleetStats();
		if (list == null || list.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.ok(list).build();
	}

	@GET
	@Path("/headquarters")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "getHeadquartersStats",
		summary = "Get statistics by headquarters",
		description = "Returns statistics per headquarters: reservation count, rental count, revenue and assigned vehicles.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Headquarters statistics retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HeadquartersStatsDTO[].class))
			),
			@ApiResponse(responseCode = "204", description = "No headquarters data found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving headquarters statistics")
		}
	)
	public Response getHeadquartersStats() {
		List<HeadquartersStatsDTO> list = statisticsService.getHeadquartersStats();
		if (list == null || list.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.ok(list).build();
	}
}
