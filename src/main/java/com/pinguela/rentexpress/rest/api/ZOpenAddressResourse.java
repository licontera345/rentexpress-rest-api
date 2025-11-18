package com.pinguela.rentexpress.rest.api;

import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.AddressDTO;
import com.pinguela.rentexpres.service.AddressService;
import com.pinguela.rentexpres.service.impl.AddressServiceImpl;

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

@Path("/addresses")
@Tag(name = "Addresses", description = "Operations for address management")
public class ZOpenAddressResourse {

    private static final Logger logger = Logger.getLogger(ZOpenAddressResourse.class.getName());

    private final AddressService addressService;

    public ZOpenAddressResourse() {
        this.addressService = new AddressServiceImpl();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find address by ID")
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Address ID is required").build();
        }
        try {
            AddressDTO address = addressService.findById(id);
            if (address == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(address).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create address")
    public Response create(AddressDTO address) {
        if (address == null) {
            return Response.status(Status.BAD_REQUEST).entity("Address data is required").build();
        }
        try {
            boolean created = addressService.create(address);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Address could not be created").build();
            }
            AddressDTO createdAddress = address.getId() != null ? addressService.findById(address.getId()) : address;
            return Response.status(Status.CREATED).entity(createdAddress).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update address")
    public Response update(AddressDTO address) {
        if (address == null || address.getId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Address ID and data are required").build();
        }
        try {
            boolean updated = addressService.update(address);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Address not found or not updated").build();
            }
            AddressDTO updatedAddress = addressService.findById(address.getId());
            return Response.ok(updatedAddress).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete address")
    public Response delete(AddressDTO address) {
        if (address == null || address.getId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Address ID and data are required").build();
        }
        try {
            boolean deleted = addressService.delete(address);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Address not found").build();
            }
            return Response.ok().entity("Address deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
