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

import com.pinguela.rentexpres.model.HeadquartersDTO;
import com.pinguela.rentexpres.service.HeadquartersService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ZOpenHeadquartersResourseTest extends JerseyTest {

    @Mock
    private HeadquartersService headquartersService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);
        ZOpenHeadquartersResourse resource = new ZOpenHeadquartersResourse();
        injectMock(resource, "headquartersService", headquartersService);
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
        when(headquartersService.findAll()).thenReturn(Collections.singletonList(new HeadquartersDTO()));

        Response response = target("headquarters").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(headquartersService.findById(1)).thenReturn(new HeadquartersDTO());

        Response response = target("headquarters/1").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        HeadquartersDTO headquarters = new HeadquartersDTO();
        when(headquartersService.create(any(HeadquartersDTO.class))).thenReturn(true);
        when(headquartersService.findById(null)).thenReturn(null);

        Response response = target("headquarters").request().post(Entity.entity(headquarters, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        HeadquartersDTO headquarters = new HeadquartersDTO();
        when(headquartersService.update(any(HeadquartersDTO.class))).thenReturn(true);
        when(headquartersService.findById(1)).thenReturn(headquarters);

        Response response = target("headquarters/1").request().put(Entity.entity(headquarters, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(headquartersService.delete(1)).thenReturn(true);

        Response response = target("headquarters/1").request().delete();

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
