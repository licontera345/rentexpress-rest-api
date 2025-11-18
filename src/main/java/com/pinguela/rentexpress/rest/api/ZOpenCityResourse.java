package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.CityDTO;
import com.pinguela.rentexpres.service.CityService;
import com.pinguela.rentexpres.service.impl.CityServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class ZOpenCityResourse {

    private static final Logger logger = Logger.getLogger(ZOpenCityResourse.class.getName());

    private final CityService cityService;

    public ZOpenCityResourse() {
        this.cityService = new CityServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find all cities")
    public Response findAll() {
        try {
            List<CityDTO> cities = cityService.findAll();
            if (cities == null || cities.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(cities).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find city by ID")
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("City ID is required").build();
        }
        try {
            CityDTO city = cityService.findById(id);
            if (city == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(city).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/province/{provinceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find cities by province")
    public Response findByProvince(@PathParam("provinceId") Integer provinceId) {
        if (provinceId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Province ID is required").build();
        }
        try {
            List<CityDTO> cities = cityService.findByProvinceId(provinceId);
            if (cities == null || cities.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(cities).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create city")
    public Response create(CityDTO city) {
        if (city == null) {
            return Response.status(Status.BAD_REQUEST).entity("City data is required").build();
        }
        try {
            boolean created = cityService.create(city);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("City could not be created").build();
            }
            CityDTO createdCity = city.getId() != null ? cityService.findById(city.getId()) : city;
            return Response.status(Status.CREATED).entity(createdCity).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update city")
    public Response update(CityDTO city) {
        if (city == null || city.getId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("City ID and data are required").build();
        }
        try {
            boolean updated = cityService.update(city);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("City not found or not updated").build();
            }
            CityDTO updatedCity = cityService.findById(city.getId());
            return Response.ok(updatedCity).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete city")
    public Response delete(CityDTO city) {
        if (city == null || city.getId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("City ID and data are required").build();
        }
        try {
            boolean deleted = cityService.delete(city);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("City not found").build();
            }
            return Response.ok().entity("City deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
