package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.EmployeeService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenEmployeeResourseTest extends JerseyTest {

    @Mock
    private EmployeeService employeeService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        ZOpenEmployeeResourse resource = new ZOpenEmployeeResourse();
        injectEmployeeService(resource);
        return new ResourceConfig().register(resource);
    }

    private void injectEmployeeService(ZOpenEmployeeResourse resource) {
        try {
            Field field = ZOpenEmployeeResourse.class.getDeclaredField("employeeService");
            field.setAccessible(true);
            field.set(resource, employeeService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findByIdReturnsOk() {
        when(employeeService.findById(1)).thenReturn(new EmployeeDTO());

        Response response = target("/employees/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void createReturnsCreated() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmployeeId(2);
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(true);
        when(employeeService.findById(2)).thenReturn(employee);

        Response response = target("/employees").request().post(Entity.json(employee));

        assertEquals(201, response.getStatus());
    }

    @Test
    void updateReturnsOk() {
        EmployeeDTO employee = new EmployeeDTO();
        when(employeeService.update(any(EmployeeDTO.class))).thenReturn(true);
        when(employeeService.findById(3)).thenReturn(employee);

        Response response = target("/employees/3").request().put(Entity.json(employee));

        assertEquals(200, response.getStatus());
    }

    @Test
    void deleteReturnsOk() {
        EmployeeDTO employee = new EmployeeDTO();
        when(employeeService.delete(any(EmployeeDTO.class))).thenReturn(true);

        Response response = target("/employees/4").request().method("DELETE", Entity.json(employee));

        assertEquals(200, response.getStatus());
    }

    @Test
    void findByCriteriaReturnsOk() {
        @SuppressWarnings("unchecked")
        Results<EmployeeDTO> results = mock(Results.class);
        when(results.getResults()).thenReturn(Collections.singletonList(new EmployeeDTO()));
        when(employeeService.findByCriteria(any())).thenReturn(results);

        Response response = target("/employees/search").queryParam("employeeId", 5).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void authenticateReturnsOk() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "user");
        credentials.put("password", "pass");
        when(employeeService.autenticar("user", "pass")).thenReturn(new EmployeeDTO());

        Response response = target("/employees/authenticate").request().post(Entity.json(credentials));

        assertEquals(200, response.getStatus());
    }

    @Test
    void activateReturnsOk() {
        when(employeeService.activate(6)).thenReturn(true);

        Response response = target("/employees/6/activate").request().post(Entity.text(""));

        assertEquals(200, response.getStatus());
    }
}
