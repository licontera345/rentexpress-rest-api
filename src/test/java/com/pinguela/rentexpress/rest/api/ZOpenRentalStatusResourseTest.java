package com.pinguela.rentexpress.rest.api;

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
 
import com.pinguela.rentexpres.model.RentalStatusDTO;
import com.pinguela.rentexpres.service.RentalStatusService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenRentalStatusResourseTest extends JerseyTest {

    @Mock
    private RentalStatusService rentalStatusService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        ZOpenRentalStatusResourse resource = new ZOpenRentalStatusResourse();
        injectMock(resource, "rentalStatusService", rentalStatusService);

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
    void tearDownMocks() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void findAllReturnsOk() throws Exception {
        when(rentalStatusService.findAll("es"))
                .thenReturn(Collections.singletonList(new RentalStatusDTO()));

        Response response = target("rental-statuses")
                .queryParam("isoCode", "es")
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() throws Exception {
        when(rentalStatusService.findById(1, "en"))
                .thenReturn(new RentalStatusDTO());

        Response response = target("rental-statuses/1")
                .queryParam("isoCode", "en")
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    // ---------------------
    // Helper
    // ---------------------
    private void injectMock(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
