package com.pinguela.rentexpress.rest.api;

import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.base.BaseCrudResource;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.AddressDTO;
import com.pinguela.rentexpres.service.AddressService;

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

@Path("/addresses")
@Tag(name = "Addresses", description = "Operations for address management")
public class AddressResource extends BaseCrudResource<AddressDTO, AddressService> {

    private static final Logger logger = Logger.getLogger(AddressResource.class.getName());

    private final AddressService addressService;

    @Inject
    public AddressResource(AddressService addressService) {
        this.addressService = addressService;
    }

    @GET
    @Path("/open/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAddressById",
        summary = "Find address by ID",
        description = "Retrieves an address using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Address retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AddressDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "400", description = "Invalid address identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the address")
        }
    )
    public Response findById(@PathParam("id") Integer id) throws RentexpresException {
        return doFindById(id, addressService);
    }

    @POST
    @Path("/open")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "createAddress",
        summary = "Create address",
        description = "Creates a new address and returns the created entity",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Address created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AddressDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or incomplete address data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the address")
        }
    )
    public Response create(AddressDTO address) throws RentexpresException {
        return doCreate(address, addressService, AddressDTO::getId, "addresses:");
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE","CLIENT" })
    @Operation(
        operationId = "updateAddress",
        summary = "Update address",
        description = "Updates an existing address and returns the updated entity",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Address updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AddressDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid address data supplied"),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the address")
        }
    )
    public Response update(@PathParam("id") Integer id, AddressDTO address) throws RentexpresException {
        return doUpdate(id, address, addressService, AddressDTO::setId, "addresses:");
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE","CLIENT" })
    @Operation(
        operationId = "deleteAddress",
        summary = "Delete address",
        description = "Deletes an address using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Address deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "400", description = "Invalid address identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the address")
        }
    )
    public Response delete(@PathParam("id") Integer id) throws RentexpresException {
        return doDelete(id, addressService, "addresses:");
    }
}
