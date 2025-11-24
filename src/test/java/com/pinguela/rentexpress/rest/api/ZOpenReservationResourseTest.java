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

import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.ReservationService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenReservationResourseTest extends JerseyTest {

    @Mock
    private ReservationService reservationService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenReservationResourse resource = new ZOpenReservationResourse();
        injectReservationService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectReservationService(ZOpenReservationResourse resource) {
        try {
            Field field = ZOpenReservationResourse.class.getDeclaredField("reservationService");
            field.setAccessible(true);
            field.set(resource, reservationService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findByIdReturnsOk() {
        when(reservationService.findById(1)).thenReturn(new ReservationDTO());

        Response response = target("/reservations/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        ReservationDTO reservation = new ReservationDTO();
        reservation.setReservationId(2);
        when(reservationService.create(any(ReservationDTO.class))).thenReturn(true);
        when(reservationService.findById(2)).thenReturn(reservation);

        Response response = target("/reservations").request().post(Entity.json(reservation));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        ReservationDTO reservation = new ReservationDTO();
        when(reservationService.update(any(ReservationDTO.class))).thenReturn(true);
        when(reservationService.findById(3)).thenReturn(reservation);

        Response response = target("/reservations/3").request().put(Entity.json(reservation));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        when(reservationService.delete(4)).thenReturn(true);

        Response response = target("/reservations/4").request().delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByCriteriaReturnsOk() {
        @SuppressWarnings("unchecked")
        Results<ReservationDTO> results = mock(Results.class);
        when(results.getResults()).thenReturn(Collections.singletonList(new ReservationDTO()));
        when(reservationService.findByCriteria(any())).thenReturn(results);

        Response response = target("/reservations/search").queryParam("reservationId", 5).request().get();

        assertEquals(200, response.getStatus());
    }
}
