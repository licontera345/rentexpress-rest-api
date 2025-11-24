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
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.RoleDTO;
import com.pinguela.rentexpres.service.RoleService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenRoleResourseTest extends JerseyTest {

    @Mock
    private RoleService roleService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);
        ZOpenRoleResourse resource = new ZOpenRoleResourse();
        injectMock(resource, "roleService", roleService);
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
        when(roleService.findAll()).thenReturn(Collections.singletonList(new RoleDTO()));

        Response response = target("roles").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(roleService.findById(1)).thenReturn(new RoleDTO());

        Response response = target("roles/1").request().get();

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
