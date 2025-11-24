package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.CityDTO;
import com.pinguela.rentexpres.service.CityService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ZOpenCityResourseTest extends JerseyTest {

    @Mock
    private CityService cityService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);
        ZOpenCityResourse resource = new ZOpenCityResourse();
        injectMock(resource, "cityService", cityService);
        return new ResourceConfig()
                .register(resource)
                .register(JavaTimeParamConverterProvider.class);
    }

    @AfterEach
    public void tearDownMocks() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void findAllReturnsOk() throws Exception {
        when(cityService.findAll()).thenReturn(Collections.singletonList(new CityDTO()));

        Response response = target("cities").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(cityService.findById(1)).thenReturn(new CityDTO());

        Response response = target("cities/1").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        CityDTO city = new CityDTO();
        when(cityService.create(any(CityDTO.class))).thenReturn(true);
        when(cityService.findById(null)).thenReturn(null);

        Response response = target("cities").request().post(Entity.entity(city, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        CityDTO city = new CityDTO();
        when(cityService.update(any(CityDTO.class))).thenReturn(true);
        when(cityService.findById(1)).thenReturn(city);

        Response response = target("cities/1").request().put(Entity.entity(city, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(cityService.delete(any(CityDTO.class))).thenReturn(true);

        Response response = target("cities/1").request().delete();

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
