package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.RentalService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenRentalResourseTest extends JerseyTest {

    @Mock
    private RentalService rentalService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenRentalResourse resource = new ZOpenRentalResourse();
        injectRentalService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectRentalService(ZOpenRentalResourse resource) {
        try {
            Field field = ZOpenRentalResourse.class.getDeclaredField("rentalService");
            field.setAccessible(true);
            field.set(resource, rentalService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findByIdReturnsOk() {
        when(rentalService.findById(1)).thenReturn(new RentalDTO());

        Response response = target("/rentals/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        RentalDTO rental = new RentalDTO();
        rental.setRentalId(2);
        when(rentalService.create(any(RentalDTO.class))).thenReturn(true);
        when(rentalService.findById(2)).thenReturn(rental);

        Response response = target("/rentals").request().post(Entity.json(rental));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        RentalDTO rental = new RentalDTO();
        when(rentalService.update(any(RentalDTO.class))).thenReturn(true);
        when(rentalService.findById(3)).thenReturn(rental);

        Response response = target("/rentals/3").request().put(Entity.json(rental));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        when(rentalService.delete(4)).thenReturn(true);

        Response response = target("/rentals/4").request().delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByCriteriaReturnsOk() {
        @SuppressWarnings("unchecked")
        Results<RentalDTO> results = mock(Results.class);
        when(results.getResults()).thenReturn(Collections.singletonList(new RentalDTO()));
        when(rentalService.findByCriteria(any())).thenReturn(results);

        Response response = target("/rentals/search").queryParam("rentalId", 5).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void existsByReservationReturnsOk() {
        when(rentalService.existsByReservation(6)).thenReturn(true);

        Response response = target("/rentals/reservations/6/exists").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createFromReservationReturnsCreated() {
        ReservationDTO reservation = new ReservationDTO();
        reservation.setReservationId(7);

        Response response = target("/rentals/from-reservation").request().post(Entity.json(reservation));

        assertEquals(201, response.getStatus());
    }

    @Test
    void autoConvertReservationsReturnsOk() {
        when(rentalService.autoConvertReservations()).thenReturn(2);

        Response response = target("/rentals/auto-convert").request().post(null);

        assertEquals(200, response.getStatus());
    }
}
