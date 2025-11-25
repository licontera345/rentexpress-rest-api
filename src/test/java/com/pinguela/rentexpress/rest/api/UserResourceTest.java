package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class UserResourceTest extends JerseyTest {

    @Mock
    private UserService userService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        UserResource resource = new UserResource();
        injectMock(resource, "userService", userService);

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
    public void findByIdReturnsOk() throws Exception {
        when(userService.findById(1)).thenReturn(new UserDTO());

        Response response = target("api/user/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void createReturnsOk() throws Exception {
        UserDTO user = new UserDTO();
        when(userService.create(Mockito.any(UserDTO.class))).thenReturn(true);
        when(userService.findById(null)).thenReturn(null);

        Response response = target("api/user").request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        UserDTO user = new UserDTO();
        when(userService.update(Mockito.any(UserDTO.class))).thenReturn(true);
        when(userService.findById(1)).thenReturn(user);

        Response response = target("api/user/1").request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(userService.delete(1)).thenReturn(true);

        Response response = target("api/user/1").request().delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void findByCriteriaReturnsOk() throws Exception {
        Results<UserDTO> results = new Results<UserDTO>();
        results.setResults(Collections.singletonList(new UserDTO()));
        when(userService.findByCriteria(Mockito.any())).thenReturn(results);

        Response response = target("api/user/search")
                .queryParam("pageNumber", 1)
                .request()
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void authenticateReturnsOk() throws Exception {
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("login", "user");
        credentials.put("password", "pass");

        when(userService.authenticate("user", "pass"))
                .thenReturn(new UserDTO());

        Response response = target("api/user/authenticate").request()
                .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void activateReturnsOk() throws Exception {
        when(userService.activate(1)).thenReturn(true);

        Response response = target("api/user/1/activate")
                .request()
                .post(null);

        assertEquals(200, response.getStatus());
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
