package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.AddressDTO;
import com.pinguela.rentexpres.service.AddressService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenAddressResourseTest extends JerseyTest {

    @Mock
    private AddressService addressService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenAddressResourse resource = new ZOpenAddressResourse();
        injectAddressService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectAddressService(ZOpenAddressResourse resource) {
        try {
            Field field = ZOpenAddressResourse.class.getDeclaredField("addressService");
            field.setAccessible(true);
            field.set(resource, addressService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findByIdReturnsOk() {
        AddressDTO address = new AddressDTO();
        when(addressService.findById(1)).thenReturn(address);

        Response response = target("/addresses/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        AddressDTO address = new AddressDTO();
        address.setId(1);
        when(addressService.create(any(AddressDTO.class))).thenReturn(true);
        when(addressService.findById(1)).thenReturn(address);

        Response response = target("/addresses").request().post(Entity.json(address));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        AddressDTO address = new AddressDTO();
        when(addressService.update(any(AddressDTO.class))).thenReturn(true);
        when(addressService.findById(1)).thenReturn(address);

        Response response = target("/addresses/1").request().put(Entity.json(address));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        AddressDTO address = new AddressDTO();
        when(addressService.delete(any(AddressDTO.class))).thenReturn(true);

        Response response = target("/addresses/1").request().delete();

        assertEquals(200, response.getStatus());
    }
}
