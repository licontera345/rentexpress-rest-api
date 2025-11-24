package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.CityDTO;
import com.pinguela.rentexpres.service.CityService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenCityResourseTest extends JerseyTest {

    @Mock
    private CityService cityService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenCityResourse resource = new ZOpenCityResourse();
        injectCityService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectCityService(ZOpenCityResourse resource) {
        try {
            Field field = ZOpenCityResourse.class.getDeclaredField("cityService");
            field.setAccessible(true);
            field.set(resource, cityService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllReturnsOk() {
        when(cityService.findAll()).thenReturn(Collections.singletonList(new CityDTO()));

        Response response = target("/cities").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() {
        when(cityService.findById(1)).thenReturn(new CityDTO());

        Response response = target("/cities/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByProvinceReturnsOk() {
        when(cityService.findByProvinceId(2)).thenReturn(Collections.singletonList(new CityDTO()));

        Response response = target("/cities/province/2").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        CityDTO city = new CityDTO();
        city.setId(3);
        when(cityService.create(any(CityDTO.class))).thenReturn(true);
        when(cityService.findById(3)).thenReturn(city);

        Response response = target("/cities").request().post(Entity.json(city));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        CityDTO city = new CityDTO();
        when(cityService.update(any(CityDTO.class))).thenReturn(true);
        when(cityService.findById(4)).thenReturn(city);

        Response response = target("/cities/4").request().put(Entity.json(city));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        when(cityService.delete(any(CityDTO.class))).thenReturn(true);

        Response response = target("/cities/5").request().delete();

        assertEquals(200, response.getStatus());
    }
}
