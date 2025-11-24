package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.service.VehicleService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ZOpenVehicleResourseTest extends JerseyTest {

    @Mock
    private VehicleService vehicleService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        ZOpenVehicleResourse resource = new ZOpenVehicleResourse();
        injectMock(resource, "vehicleService", vehicleService);

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
    public void findByIdReturnsOk() throws Exception {
        when(vehicleService.findById(1)).thenReturn(new VehicleDTO());

        Response response = target("vehicles/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        VehicleDTO vehicle = new VehicleDTO();

        when(vehicleService.create(any(VehicleDTO.class))).thenReturn(true);
        when(vehicleService.findById(null)).thenReturn(null);

        Response response = target("vehicles")
                .request()
                .post(Entity.entity(vehicle, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        VehicleDTO vehicle = new VehicleDTO();

        when(vehicleService.update(any(VehicleDTO.class))).thenReturn(true);
        when(vehicleService.findById(1)).thenReturn(vehicle);

        Response response = target("vehicles/1")
                .request()
                .put(Entity.entity(vehicle, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(vehicleService.delete(1)).thenReturn(true);

        Response response = target("vehicles/1").request().delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void findByCriteriaReturnsOk() throws Exception {
        Results<VehicleDTO> results = new Results<>();
        results.setResults(Collections.singletonList(new VehicleDTO()));

        when(vehicleService.findByCriteria(Mockito.any())).thenReturn(results);

        Response response = target("vehicles/search")
                .queryParam("pageNumber", 1)
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
