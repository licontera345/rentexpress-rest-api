package com.pinguela.rentexpress.rest.api.address;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.base.BaseCrudResource;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ProvinceDTO;
import com.pinguela.rentexpres.service.ProvinceService;

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

@Path("/provinces")
@Tag(name = "Provinces", description = "Operations for province management")
public class ProvinceResource extends BaseCrudResource<ProvinceDTO, ProvinceService> {

    private static final Logger logger = Logger.getLogger(ProvinceResource.class.getName());

    private final ProvinceService provinceService;

    @Inject
    public ProvinceResource(ProvinceService provinceService) {
        this.provinceService = provinceService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllProvinces",
        summary = "Find all provinces",
        description = "Retrieves every province available in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Provinces retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProvinceDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No provinces found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving provinces")
        }
    )
    public Response findAll() {
        List<ProvinceDTO> provinces = provinceService.findAll();
        if (provinces == null || provinces.isEmpty()) {
            return Response.status(Status.NO_CONTENT).build();
        }
        return Response.ok(provinces).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findProvinceById",
        summary = "Find province by ID",
        description = "Retrieves a province using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Province retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProvinceDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Province not found"),
            @ApiResponse(responseCode = "400", description = "Invalid province identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the province")
        }
    )
    public Response findById(@PathParam("id") Integer id) throws RentexpresException {
        return doFindById(id, provinceService);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "createProvince",
        summary = "Create province",
        description = "Creates a new province in the system",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Province created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProvinceDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid province data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the province")
        }
    )
    public Response create(ProvinceDTO province) throws RentexpresException {
        return doCreate(province, provinceService, ProvinceDTO::getProvinceId, "provinces:");
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "updateProvince",
        summary = "Update province",
        description = "Updates an existing province in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Province updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProvinceDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid province data supplied"),
            @ApiResponse(responseCode = "404", description = "Province not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the province")
        }
    )
    public Response update(@PathParam("id") Integer id, ProvinceDTO province) throws RentexpresException {
        return doUpdate(id, province, provinceService, ProvinceDTO::setProvinceId, "provinces:");
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "deleteProvince",
        summary = "Delete province",
        description = "Deletes a province using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Province deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Province not found"),
            @ApiResponse(responseCode = "400", description = "Invalid province identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the province")
        }
    )
    public Response delete(@PathParam("id") Integer id) throws RentexpresException {
        return doDelete(id, provinceService, "provinces:");
    }
}
