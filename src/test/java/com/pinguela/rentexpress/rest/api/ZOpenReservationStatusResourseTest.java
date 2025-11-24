package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.ReservationStatusDTO;
import com.pinguela.rentexpres.service.ReservationStatusService;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenReservationStatusResourseTest extends JerseyTest {

    @Mock
    private ReservationStatusService reservationStatusService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenReservationStatusResourse resource = new ZOpenReservationStatusResourse();
        injectReservationStatusService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectReservationStatusService(ZOpenReservationStatusResourse resource) {
        try {
            Field field = ZOpenReservationStatusResourse.class.getDeclaredField("reservationStatusService");
            field.setAccessible(true);
            field.set(resource, reservationStatusService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllReturnsOk() {
        when(reservationStatusService.findAll("es")).thenReturn(Collections.singletonList(new ReservationStatusDTO()));

        Response response = target("/reservation-statuses").queryParam("isoCode", "es").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() {
        when(reservationStatusService.findById(1, "en")).thenReturn(new ReservationStatusDTO());

        Response response = target("/reservation-statuses/1").queryParam("isoCode", "en").request().get();

        assertEquals(200, response.getStatus());
    }
}
