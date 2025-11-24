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

import com.pinguela.rentexpress.model.VehicleStatusDTO;
import com.pinguela.rentexpress.service.VehicleStatusService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenVehicleStatusResourseTest extends JerseyTest {

    @Mock
    private VehicleStatusService vehicleStatusService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        ZOpenVehicleStatusResourse resource = new ZOpenVehicleStatusResourse();
        injectMock(resource, "vehicleStatusService", vehicleStatusService);

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
    public void tearDownMocks() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    public void findAllReturnsOk() throws Exception {
        when(vehicleStatusService.findAll("en"))
                .thenReturn(Collections.singletonList(new VehicleStatusDTO()));

        Response response = target("vehicle-statuses")
                .queryParam("isoCode", "en")
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(vehicleStatusService.findById(1, "en"))
                .thenReturn(new VehicleStatusDTO());

        Response response = target("vehicle-statuses/1")
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
 