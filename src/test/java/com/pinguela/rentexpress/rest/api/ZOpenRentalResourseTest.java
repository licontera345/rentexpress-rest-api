package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

import com.pinguela.rentexpress.model.RentalDTO;
import com.pinguela.rentexpress.model.ReservationDTO;
import com.pinguela.rentexpress.model.Results;
import com.pinguela.rentexpress.service.RentalService;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

public class ZOpenRentalResourseTest extends JerseyTest {

	@Mock
	private RentalService rentalService;

	private AutoCloseable mocks;

	@Override
	protected Application configure() {
		mocks = MockitoAnnotations.openMocks(this);

		ZOpenRentalResourse resource = new ZOpenRentalResourse();
		injectMock(resource, "rentalService", rentalService);

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

	// ---------------------------------------------------
	// TESTS
	// ---------------------------------------------------

	@Test
	void findByIdReturnsOk() throws Exception {
		when(rentalService.findById(1)).thenReturn(new RentalDTO());

		Response response = target("rentals/1").request().get();

		assertEquals(200, response.getStatus());
	}

	@Test
	void createReturnsCreated() throws Exception {
		RentalDTO rental = new RentalDTO();
		rental.setRentalId(2);

		when(rentalService.create(any(RentalDTO.class))).thenReturn(true);
		when(rentalService.findById(2)).thenReturn(rental);

		Response response = target("rentals").request().post(Entity.json(rental));

		assertEquals(201, response.getStatus());
	}

	@Test
	void updateReturnsOk() throws Exception {
		RentalDTO rental = new RentalDTO();

		when(rentalService.update(any(RentalDTO.class))).thenReturn(true);
		when(rentalService.findById(3)).thenReturn(rental);

		Response response = target("rentals/3").request().put(Entity.json(rental));

		assertEquals(200, response.getStatus());
	}

	@Test
	void deleteReturnsOk() throws Exception {
		when(rentalService.delete(4)).thenReturn(true);

		Response response = target("rentals/4").request().delete();

		assertEquals(200, response.getStatus());
	}

	@Test
	void findByCriteriaReturnsOk() throws Exception {
		@SuppressWarnings("unchecked")
		Results<RentalDTO> results = mock(Results.class);
		when(results.getResults()).thenReturn(Collections.singletonList(new RentalDTO()));
		when(rentalService.findByCriteria(any())).thenReturn(results);

		Response response = target("rentals/search").queryParam("rentalId", 5).request().get();

		assertEquals(200, response.getStatus());
	}

	@Test
	void existsByReservationReturnsOk() throws Exception {
		when(rentalService.existsByReservation(6)).thenReturn(true);

		Response response = target("rentals/reservations/6/exists").request().get();

		assertEquals(200, response.getStatus());
	}

	@Test
	void createFromReservationReturnsCreated() throws Exception {
		ReservationDTO res = new ReservationDTO();
		res.setReservationId(7);

		doNothing().when(rentalService).createFromReservation(any(ReservationDTO.class));
		Response response = target("rentals/from-reservation").request().post(Entity.json(res));

		assertEquals(201, response.getStatus());
	}

	@Test
	void autoConvertReservationsReturnsOk() throws Exception {
		when(rentalService.autoConvertReservations()).thenReturn(2);

		Response response = target("rentals/auto-convert").request().post(null);

		assertEquals(200, response.getStatus());
	}

	// ---------------------------------------------------
	// HELPERS
	// ---------------------------------------------------

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
