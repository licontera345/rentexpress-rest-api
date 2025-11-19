package com.pinguela.rentexpress.rest.api;

import java.util.List;
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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
    @Operation(summary = "Create rental")
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
    @Operation(summary = "Update rental")
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

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search rentals by criteria")
    public Response findByCriteria(RentalCriteria criteria) {
        if (criteria == null) {
            return Response.status(Status.BAD_REQUEST).entity("Search criteria is required").build();
        }
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
    @Operation(summary = "Check rental existence by reservation")
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
    @Operation(summary = "Create rental from reservation")
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
    @Operation(summary = "Auto convert reservations into rentals")
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
