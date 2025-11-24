package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.service.VehicleService;

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
        return new ResourceConfig().register(resource);
    }

    @AfterEach
    public void tearDownMocks() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void findByIdReturnsOk() {
        when(vehicleService.findById(1)).thenReturn(new VehicleDTO());

        Response response = target("vehicles/1").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createReturnsCreated() {
        VehicleDTO vehicle = new VehicleDTO();
        when(vehicleService.create(vehicle)).thenReturn(true);
        when(vehicleService.findById(null)).thenReturn(null);

        Response response = target("vehicles").request().post(Entity.entity(vehicle, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateReturnsOk() {
        VehicleDTO vehicle = new VehicleDTO();
        when(vehicleService.update(vehicle)).thenReturn(true);
        when(vehicleService.findById(1)).thenReturn(vehicle);

        Response response = target("vehicles/1").request().put(Entity.entity(vehicle, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteReturnsOk() {
        when(vehicleService.delete(1)).thenReturn(true);

        Response response = target("vehicles/1").request().delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void findByCriteriaReturnsOk() {
        Results<VehicleDTO> results = new Results<>();
        results.setResults(Collections.singletonList(new VehicleDTO()));
        when(vehicleService.findByCriteria(Mockito.any())).thenReturn(results);

        Response response = target("vehicles/search").queryParam("pageNumber", 1).request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
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
