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

import com.pinguela.rentexpres.model.VehicleCategoryDTO;
import com.pinguela.rentexpres.service.VehicleCategoryService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenVehicleCategoryResourseTest extends JerseyTest {

    @Mock
    private VehicleCategoryService vehicleCategoryService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        ZOpenVehicleCategoryResourse resource = new ZOpenVehicleCategoryResourse();
        injectMock(resource, "vehicleCategoryService", vehicleCategoryService);
 
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
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void findAllReturnsOk() throws Exception {
        when(vehicleCategoryService.findAll("en"))
                .thenReturn(Collections.singletonList(new VehicleCategoryDTO()));

        Response response = target("vehicle-categories")
                .queryParam("isoCode", "en")
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(vehicleCategoryService.findById(1, "en"))
                .thenReturn(new VehicleCategoryDTO());

        Response response = target("vehicle-categories/1")
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
