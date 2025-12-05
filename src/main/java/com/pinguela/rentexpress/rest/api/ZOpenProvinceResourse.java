package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ProvinceDTO;
import com.pinguela.rentexpres.service.ProvinceService;
import com.pinguela.rentexpres.service.impl.ProvinceServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@Path("/zopen/provinces")
@Tag(name = "Provinces", description = "Operations for province management")
public class ZOpenProvinceResourse {

    private static final Logger logger = Logger.getLogger(ZOpenProvinceResourse.class.getName());

    private final ProvinceService provinceService;

    public ZOpenProvinceResourse() {
        this.provinceService = new ProvinceServiceImpl();
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
        try {
            List<ProvinceDTO> provinces = provinceService.findAll();
            if (provinces == null || provinces.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(provinces).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Province ID is required").build();
        }
        try {
            ProvinceDTO province = provinceService.findById(id);
            if (province == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(province).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response create(ProvinceDTO province) {
        if (province == null) {
            return Response.status(Status.BAD_REQUEST).entity("Province data is required").build();
        }
        try {
            boolean created = provinceService.create(province);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Province could not be created").build();
            }
            ProvinceDTO createdProvince = province.getProvinceId() != null
                    ? provinceService.findById(province.getProvinceId())
                    : province;
            return Response.status(Status.CREATED).entity(createdProvince).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response update(@PathParam("id") Integer id, ProvinceDTO province) {
        if (id == null || province == null) {
            return Response.status(Status.BAD_REQUEST).entity("Province ID and data are required").build();
        }
        province.setProvinceId(id);
        try {
            boolean updated = provinceService.update(province);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Province not found or not updated").build();
            }
            ProvinceDTO updatedProvince = provinceService.findById(province.getProvinceId());
            return Response.ok(updatedProvince).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Province ID is required").build();
        }
        try {
            boolean deleted = provinceService.delete(id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Province not found").build();
            }
            return Response.ok().entity("Province deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
