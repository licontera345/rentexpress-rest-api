package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.ReservationService;
import com.pinguela.rentexpres.service.impl.ReservationServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
    @Operation(summary = "Find all reservations")
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
    @Operation(summary = "Find reservation by ID")
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
    @Operation(summary = "Create reservation")
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
    @Operation(summary = "Update reservation")
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
    @Operation(summary = "Delete reservation")
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

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search reservations by criteria")
    public Response findByCriteria(ReservationCriteria criteria) {
        if (criteria == null) {
            return Response.status(Status.BAD_REQUEST).entity("Search criteria is required").build();
        }
        try {
            Results<ReservationDTO> results = reservationService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(results).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
