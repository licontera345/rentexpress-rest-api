package com.pinguela.rentexpress.rest.api.reservationstatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.ReservationStatusDTO;
import com.pinguela.rentexpres.service.ReservationStatusService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ReservationStatusResourceTest extends JerseyTest {

    @Mock
    private ReservationStatusService reservationStatusService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        ReservationStatusResource resource = new ReservationStatusResource();
        injectMock(resource, "reservationStatusService", reservationStatusService);

        ResourceConfig rc = new ResourceConfig();
        rc.registerInstances(resource);
        rc.register(JavaTimeParamConverterProvider.class);

        return rc;
    }

    @Override
    protected org.glassfish.jersey.test.spi.TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void findAllReturnsOk() throws Exception {
        when(reservationStatusService.findAll("es"))
                .thenReturn(Collections.singletonList(new ReservationStatusDTO()));

        Response response = target("api/reservation-status")
                .queryParam("isoCode", "es")
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() throws Exception {
        when(reservationStatusService.findById(1, "en"))
                .thenReturn(new ReservationStatusDTO());

        Response response = target("api/reservation-status/1")
                .queryParam("isoCode", "en")
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    private void injectMock(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
