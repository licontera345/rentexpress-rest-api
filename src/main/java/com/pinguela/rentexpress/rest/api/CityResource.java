package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.base.BaseCrudResource;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.CityDTO;
import com.pinguela.rentexpres.service.CityService;

import jakarta.inject.Inject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/cities")
@Tag(name = "Cities", description = "Operations for city management")
public class CityResource extends BaseCrudResource<CityDTO, CityService> {

	private static final Logger logger = Logger.getLogger(CityResource.class.getName());

	private final CityService cityService;

	@Inject
	public CityResource(CityService cityService) {
		this.cityService = cityService;
	}

	@GET
	@Path("/open")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "findAllCities", summary = "Find all cities", description = "Retrieves every city available in the system", responses = {
			@ApiResponse(responseCode = "200", description = "Cities retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CityDTO[].class))),
			@ApiResponse(responseCode = "204", description = "No cities found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving cities") })
	public Response findAll() throws RentexpresException {
		List<CityDTO> cities = cityService.findAll();
		if (cities == null || cities.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.ok(cities).build();
	}

	@GET
	@Path("/{id}")
	@Secured
	@RolesAllowed({ "ADMIN", "EMPLOYEE" ,"CLIENT" })
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "findCityById", summary = "Find city by ID", description = "Retrieves a city using its unique identifier", responses = {
			@ApiResponse(responseCode = "200", description = "City retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CityDTO.class))),
			@ApiResponse(responseCode = "404", description = "City not found"),
			@ApiResponse(responseCode = "400", description = "Invalid city identifier supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the city") })
	public Response findById(@PathParam("id") Integer id) throws RentexpresException {
		return doFindById(id, cityService);
	}

	@GET
	@Path("/province/{provinceId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "findCitiesByProvince", summary = "Find cities by province", description = "Retrieves all cities that belong to the provided province", responses = {
			@ApiResponse(responseCode = "200", description = "Cities retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CityDTO[].class))),
			@ApiResponse(responseCode = "204", description = "No cities found for the provided province"),
			@ApiResponse(responseCode = "400", description = "Invalid province identifier supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while retrieving cities") })
	public Response findByProvince(@PathParam("provinceId") Integer provinceId) throws RentexpresException {
		if (provinceId == null) {
			return ErrorResponseHelper.badRequest("BAD_REQUEST", "Province ID is required");
		}
		List<CityDTO> cities = cityService.findByProvinceId(provinceId);
		if (cities == null || cities.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.ok(cities).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "createCity", summary = "Create city", description = "Creates a new city and returns the created entity", responses = {
			@ApiResponse(responseCode = "201", description = "City created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CityDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or incomplete city data supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while creating the city") })
	public Response create(CityDTO city) throws RentexpresException {
		return doCreate(city, cityService, CityDTO::getId, "cities:");
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updateCity", summary = "Update city", description = "Updates an existing city and returns the updated entity", responses = {
			@ApiResponse(responseCode = "200", description = "City updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CityDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid city data supplied"),
			@ApiResponse(responseCode = "404", description = "City not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while updating the city") })
	public Response update(@PathParam("id") Integer id, CityDTO city) throws RentexpresException {
		return doUpdate(id, city, cityService, CityDTO::setId, "cities:");
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "deleteCity", summary = "Delete city", description = "Deletes a city using its unique identifier", responses = {
			@ApiResponse(responseCode = "200", description = "City deleted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "City not found"),
			@ApiResponse(responseCode = "400", description = "Invalid city identifier supplied"),
			@ApiResponse(responseCode = "500", description = "Unexpected error while deleting the city") })
	public Response delete(@PathParam("id") Integer id) throws RentexpresException {
		return doDelete(id, cityService, "cities:");
	}
}
