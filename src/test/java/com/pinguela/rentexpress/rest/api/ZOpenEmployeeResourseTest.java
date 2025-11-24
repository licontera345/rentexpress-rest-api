package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ZOpenEmployeeResourseTest extends JerseyTest {

    @Mock
    private EmployeeService employeeService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);
        ZOpenEmployeeResourse resource = new ZOpenEmployeeResourse();
        injectMock(resource, "employeeService", employeeService);
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
        when(employeeService.findById(1)).thenReturn(new EmployeeDTO());

        Response response = target("employees/1").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        EmployeeDTO employee = new EmployeeDTO();
        when(employeeService.create(Mockito.any(EmployeeDTO.class))).thenReturn(true);
        when(employeeService.findById(null)).thenReturn(null);

        Response response = target("employees").request().post(Entity.entity(employee, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        EmployeeDTO employee = new EmployeeDTO();
        when(employeeService.update(Mockito.any(EmployeeDTO.class))).thenReturn(true);
        when(employeeService.findById(1)).thenReturn(employee);

        Response response = target("employees/1").request().put(Entity.entity(employee, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(employeeService.delete(Mockito.any(EmployeeDTO.class), Mockito.eq(1))).thenReturn(true);

        Response response = target("employees/1").request().delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void findByCriteriaReturnsOk() throws Exception {
        Results<EmployeeDTO> results = new Results<>();
        results.setResults(Collections.singletonList(new EmployeeDTO()));
        when(employeeService.findByCriteria(Mockito.any())).thenReturn(results);

        Response response = target("employees/search").queryParam("pageNumber", 1).request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void authenticateReturnsOk() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "user");
        credentials.put("password", "pass");
        when(employeeService.autenticar("user", "pass")).thenReturn(new EmployeeDTO());

        Response response = target("employees/authenticate").request()
            .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void activateReturnsOk() throws Exception {
        when(employeeService.activate(1)).thenReturn(true);

        Response response = target("employees/1/activate").request().post(null);

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
