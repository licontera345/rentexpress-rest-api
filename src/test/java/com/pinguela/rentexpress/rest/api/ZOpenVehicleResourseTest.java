package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

import com.pinguela.rentexpress.model.Results;
import com.pinguela.rentexpress.model.VehicleDTO;
import com.pinguela.rentexpress.service.VehicleService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenVehicleResourseTest extends JerseyTest {

	@Mock
	private VehicleService vehicleService;

	private AutoCloseable mocks;

	@Override
	protected Application configure() {
		mocks = MockitoAnnotations.openMocks(this);

		ZOpenVehicleResourse resource = new ZOpenVehicleResourse();
		injectMock(resource, "vehicleService", vehicleService);

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
	void findByIdReturnsOk() throws Exception {
		when(vehicleService.findById(1)).thenReturn(new VehicleDTO());

		Response response = target("vehicles/1").request().get();

		assertEquals(200, response.getStatus());
	}

	@Test
	void createReturnsCreated() throws Exception {
		VehicleDTO vehicle = new VehicleDTO();
		vehicle.setVehicleId(2);

		when(vehicleService.create(any(VehicleDTO.class))).thenReturn(true);
		when(vehicleService.findById(2)).thenReturn(vehicle);

		Response response = target("vehicles").request().post(Entity.json(vehicle));

		assertEquals(201, response.getStatus());
	}

	@Test
	void updateReturnsOk() throws Exception {
		VehicleDTO vehicle = new VehicleDTO();

		when(vehicleService.update(any(VehicleDTO.class))).thenReturn(true);
		when(vehicleService.findById(3)).thenReturn(vehicle);

		Response response = target("vehicles/3").request().put(Entity.json(vehicle));

		assertEquals(200, response.getStatus());
	}

	@Test
	void deleteReturnsOk() throws Exception {
		when(vehicleService.delete(4)).thenReturn(true);

		Response response = target("vehicles/4").request().delete();

		assertEquals(200, response.getStatus());
	}

	@Test
	void findByCriteriaReturnsOk() throws Exception {
		@SuppressWarnings("unchecked")
		Results<VehicleDTO> results = mock(Results.class);

		when(results.getResults()).thenReturn(Collections.singletonList(new VehicleDTO()));
		when(vehicleService.findByCriteria(any())).thenReturn(results);

		Response response = target("vehicles/search").queryParam("brand", "test").request().get();

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
