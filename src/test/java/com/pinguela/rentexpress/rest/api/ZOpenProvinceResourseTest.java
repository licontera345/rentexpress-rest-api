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

import com.pinguela.rentexpres.model.ProvinceDTO;
import com.pinguela.rentexpres.service.ProvinceService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenProvinceResourseTest extends JerseyTest {

    @Mock
    private ProvinceService provinceService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenProvinceResourse resource = new ZOpenProvinceResourse();
        injectProvinceService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectProvinceService(ZOpenProvinceResourse resource) {
        try {
            Field field = ZOpenProvinceResourse.class.getDeclaredField("provinceService");
            field.setAccessible(true);
            field.set(resource, provinceService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllReturnsOk() {
        when(provinceService.findAll()).thenReturn(Collections.singletonList(new ProvinceDTO()));

        Response response = target("/provinces").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() {
        when(provinceService.findById(1)).thenReturn(new ProvinceDTO());

        Response response = target("/provinces/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        ProvinceDTO province = new ProvinceDTO();
        province.setId(2);
        when(provinceService.create(any(ProvinceDTO.class))).thenReturn(true);
        when(provinceService.findById(2)).thenReturn(province);

        Response response = target("/provinces").request().post(Entity.json(province));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        ProvinceDTO province = new ProvinceDTO();
        when(provinceService.update(any(ProvinceDTO.class))).thenReturn(true);
        when(provinceService.findById(3)).thenReturn(province);

        Response response = target("/provinces/3").request().put(Entity.json(province));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        when(provinceService.delete(any(ProvinceDTO.class))).thenReturn(true);

        Response response = target("/provinces/4").request().delete();

        assertEquals(200, response.getStatus());
    }
}
