package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.AddressDTO;
import com.pinguela.rentexpres.service.AddressService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ZOpenAddressResourseTest extends JerseyTest {

    @Mock
    private AddressService addressService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);
        ZOpenAddressResourse resource = new ZOpenAddressResourse();
        injectMock(resource, "addressService", addressService);
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
    public void findByIdReturnsOk() throws Exception {
        when(addressService.findById(1)).thenReturn(new AddressDTO());

        Response response = target("addresses/1").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        AddressDTO address = new AddressDTO();
        when(addressService.create(any(AddressDTO.class))).thenReturn(true);
        when(addressService.findById(null)).thenReturn(null);

        Response response = target("addresses").request().post(Entity.entity(address, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        AddressDTO address = new AddressDTO();
        when(addressService.update(any(AddressDTO.class))).thenReturn(true);
        when(addressService.findById(1)).thenReturn(address);

        Response response = target("addresses/1").request().put(Entity.entity(address, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(addressService.delete(any(AddressDTO.class))).thenReturn(true);

        Response response = target("addresses/1").request().delete();

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
