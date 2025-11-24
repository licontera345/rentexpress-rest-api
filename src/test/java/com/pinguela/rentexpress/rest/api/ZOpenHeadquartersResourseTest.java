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

import com.pinguela.rentexpres.model.HeadquartersDTO;
import com.pinguela.rentexpres.service.HeadquartersService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenHeadquartersResourseTest extends JerseyTest {

    @Mock
    private HeadquartersService headquartersService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenHeadquartersResourse resource = new ZOpenHeadquartersResourse();
        injectHeadquartersService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectHeadquartersService(ZOpenHeadquartersResourse resource) {
        try {
            Field field = ZOpenHeadquartersResourse.class.getDeclaredField("headquartersService");
            field.setAccessible(true);
            field.set(resource, headquartersService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllReturnsOk() {
        when(headquartersService.findAll()).thenReturn(Collections.singletonList(new HeadquartersDTO()));

        Response response = target("/headquarters").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByIdReturnsOk() {
        when(headquartersService.findById(1)).thenReturn(new HeadquartersDTO());

        Response response = target("/headquarters/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        HeadquartersDTO headquarters = new HeadquartersDTO();
        headquarters.setHeadquartersId(2);
        when(headquartersService.create(any(HeadquartersDTO.class))).thenReturn(true);
        when(headquartersService.findById(2)).thenReturn(headquarters);

        Response response = target("/headquarters").request().post(Entity.json(headquarters));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        HeadquartersDTO headquarters = new HeadquartersDTO();
        when(headquartersService.update(any(HeadquartersDTO.class))).thenReturn(true);
        when(headquartersService.findById(3)).thenReturn(headquarters);

        Response response = target("/headquarters/3").request().put(Entity.json(headquarters));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        when(headquartersService.delete(4)).thenReturn(true);

        Response response = target("/headquarters/4").request().delete();

        assertEquals(200, response.getStatus());
    }
}
