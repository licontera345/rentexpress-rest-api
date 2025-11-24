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

import com.pinguela.rentexpres.model.RentalStatusDTO;
import com.pinguela.rentexpres.service.RentalStatusService;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenRentalStatusResourseTest extends JerseyTest {

    @Mock
    private RentalStatusService rentalStatusService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenRentalStatusResourse resource = new ZOpenRentalStatusResourse();
        injectRentalStatusService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectRentalStatusService(ZOpenRentalStatusResourse resource) {
        try {
            Field field = ZOpenRentalStatusResourse.class.getDeclaredField("rentalStatusService");
            field.setAccessible(true);
            field.set(resource, rentalStatusService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllReturnsOk() {
        when(rentalStatusService.findAll("es")).thenReturn(Collections.singletonList(new RentalStatusDTO()));

        Response response = target("/rental-statuses").queryParam("isoCode", "es").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() {
        when(rentalStatusService.findById(1, "en")).thenReturn(new RentalStatusDTO());

        Response response = target("/rental-statuses/1").queryParam("isoCode", "en").request().get();

        assertEquals(200, response.getStatus());
    }
}
