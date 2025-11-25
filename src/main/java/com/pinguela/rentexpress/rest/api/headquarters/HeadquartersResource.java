package com.pinguela.rentexpress.rest.api.headquarters;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.HeadquartersDTO;
import com.pinguela.rentexpres.service.HeadquartersService;
import com.pinguela.rentexpres.service.impl.HeadquartersServiceImpl;

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

@Path("/api/headquarters")
@Tag(name = "Headquarters", description = "Operations for headquarters management")
public class HeadquartersResource {

    private static final Logger logger = Logger.getLogger(HeadquartersResource.class.getName());

    private final HeadquartersService headquartersService;

    public HeadquartersResource() {
        this.headquartersService = new HeadquartersServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllHeadquarters",
        summary = "Find all headquarters",
        description = "Retrieves every headquarters available in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Headquarters retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HeadquartersDTO[].class))
            ),
            @ApiResponse(responseCode = "204", description = "No headquarters found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving headquarters")
        }
    )
    public Response findAll() {
        try {
            List<HeadquartersDTO> headquarters = headquartersService.findAll();
            if (headquarters == null || headquarters.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(headquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findHeadquartersById",
        summary = "Find headquarters by ID",
        description = "Retrieves a headquarters using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Headquarters retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HeadquartersDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Headquarters not found"),
            @ApiResponse(responseCode = "400", description = "Invalid headquarters identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the headquarters")
        }
    )
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Headquarters ID is required").build();
        }
        try {
            HeadquartersDTO headquarters = headquartersService.findById(id);
            if (headquarters == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(headquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "createHeadquarters",
        summary = "Create headquarters",
        description = "Creates a new headquarters entry",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Headquarters created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HeadquartersDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid headquarters data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the headquarters")
        }
    )
    public Response create(HeadquartersDTO headquarters) {
        if (headquarters == null) {
            return Response.status(Status.BAD_REQUEST).entity("Headquarters data is required").build();
        }
        try {
            boolean created = headquartersService.create(headquarters);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Headquarters could not be created").build();
            }
            HeadquartersDTO createdHeadquarters = headquarters.getId() != null
                    ? headquartersService.findById(headquarters.getId())
                    : headquarters;
            return Response.status(Status.CREATED).entity(createdHeadquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "updateHeadquarters",
        summary = "Update headquarters",
        description = "Updates an existing headquarters entry",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Headquarters updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HeadquartersDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid headquarters data supplied"),
            @ApiResponse(responseCode = "404", description = "Headquarters not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the headquarters")
        }
    )
    public Response update(@PathParam("id") Integer id, HeadquartersDTO headquarters) {
        if (id == null || headquarters == null) {
            return Response.status(Status.BAD_REQUEST).entity("Headquarters ID and data are required").build();
        }
        headquarters.setId(id);
        try {
            boolean updated = headquartersService.update(headquarters);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Headquarters not found or not updated").build();
            }
            HeadquartersDTO updatedHeadquarters = headquartersService.findById(headquarters.getId());
            return Response.ok(updatedHeadquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "deleteHeadquarters",
        summary = "Delete headquarters",
        description = "Deletes a headquarters using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Headquarters deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Headquarters not found"),
            @ApiResponse(responseCode = "400", description = "Invalid headquarters identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the headquarters")
        }
    )
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Headquarters ID is required").build();
        }
        try {
            boolean deleted = headquartersService.delete(id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Headquarters not found").build();
            }
            return Response.ok().entity("Headquarters deleted successfully").build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
