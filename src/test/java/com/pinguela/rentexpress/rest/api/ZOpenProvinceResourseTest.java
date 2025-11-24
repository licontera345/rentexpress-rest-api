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
import org.mockito.MockitoAnnotations;
 
import com.pinguela.rentexpress.model.ProvinceDTO;
import com.pinguela.rentexpress.service.ProvinceService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ZOpenProvinceResourseTest extends JerseyTest {

    @Mock
    private ProvinceService provinceService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        ZOpenProvinceResourse resource = new ZOpenProvinceResourse();
        injectMock(resource, "provinceService", provinceService);

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
        when(provinceService.findAll()).thenReturn(Collections.singletonList(new ProvinceDTO()));

        Response response = target("provinces").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(provinceService.findById(1)).thenReturn(new ProvinceDTO());

        Response response = target("provinces/1").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        ProvinceDTO province = new ProvinceDTO();

        when(provinceService.create(any(ProvinceDTO.class))).thenReturn(true);

        Response response = target("provinces")
                .request()
                .post(Entity.entity(province, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        ProvinceDTO province = new ProvinceDTO();

        when(provinceService.update(any(ProvinceDTO.class))).thenReturn(true);
        when(provinceService.findById(1)).thenReturn(province);

        Response response = target("provinces/1")
                .request()
                .put(Entity.entity(province, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(provinceService.delete(1)).thenReturn(true);

        Response response = target("provinces/1").request().delete();

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
