package com.pinguela.rentexpress.rest.api.headquarters;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.HeadquartersDTO;
import com.pinguela.rentexpres.service.HeadquartersService;

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

@Path("/headquarters")
@Tag(name = "Headquarters", description = "Operations for headquarters management")
public class HeadquartersResource {

    private static final Logger logger = Logger.getLogger(HeadquartersResource.class.getName());

    private final HeadquartersService headquartersService;

    @Inject
    public HeadquartersResource(HeadquartersService headquartersService) {
        this.headquartersService = headquartersService;
    }

    @GET
    @Path("/open")
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
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("INTERNAL", "An unexpected error occurred"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE","CLIENT" })
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Headquarters ID is required");
        }
        try {
            HeadquartersDTO headquarters = headquartersService.findById(id);
            if (headquarters == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(headquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("INTERNAL", "An unexpected error occurred"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Headquarters data is required");
        }
        try {
            boolean created = headquartersService.create(headquarters);
            if (!created) {
                return ErrorResponseHelper.badRequest("BAD_REQUEST", "Headquarters could not be created");
            }
            HeadquartersDTO createdHeadquarters = headquarters.getId() != null
                    ? headquartersService.findById(headquarters.getId())
                    : headquarters;
            return Response.status(Status.CREATED).entity(createdHeadquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("INTERNAL", "An unexpected error occurred"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Headquarters ID and data are required");
        }
        headquarters.setId(id);
        try {
            boolean updated = headquartersService.update(headquarters);
            if (!updated) {
                return ErrorResponseHelper.notFound("NOT_FOUND", "Headquarters not found or not updated");
            }
            HeadquartersDTO updatedHeadquarters = headquartersService.findById(headquarters.getId());
            return Response.ok(updatedHeadquarters).build();
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("INTERNAL", "An unexpected error occurred"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Headquarters ID is required");
        }
        try {
            boolean deleted = headquartersService.delete(id);
            if (!deleted) {
                return ErrorResponseHelper.notFound("NOT_FOUND", "Headquarters not found");
            }
            return ErrorResponseHelper.ok("OK", "Headquarters deleted successfully");
        } catch (DataException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("INTERNAL", "An unexpected error occurred"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }
}
