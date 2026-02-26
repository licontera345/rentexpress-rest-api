package com.pinguela.rentexpress.rest.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ImageDTO;
import com.pinguela.rentexpres.service.ImageService;
import com.pinguela.rentexpres.service.impl.ImageServiceImpl;
import com.pinguela.rentexpress.rest.api.dto.ImageCreateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/images")
@Tag(name = "Images", description = "Operations for managing vehicle, user, and employee images")
public class ImageResource {

    private static final Logger logger = Logger.getLogger(ImageResource.class.getName());

    private final ImageService imageService;

    public ImageResource() {
        this.imageService = new ImageServiceImpl();
    }

    @POST
    @Path("/vehicles/{vehicleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "addVehicleImage",
        summary = "Add vehicle image",
        description = "Creates an image and links it to a vehicle",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Image created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON)
            ),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle or image data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the image")
        }
    )
    public Response addVehicleImage(@PathParam("vehicleId") Integer vehicleId, ImageCreateRequest request) {
        if (vehicleId == null || request == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID and image data are required").build();
        }
        try {
            Long imageId = imageService.addVehicleImage(vehicleId, request.getPublicId(), request.getSecureUrl(),
                request.getPrimary());
            Map<String, Object> response = new HashMap<>();
            response.put("imageId", imageId);
            return Response.status(Status.CREATED).entity(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @POST
    @Path("/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "addUserImage",
        summary = "Add user image",
        description = "Creates an image and links it to a user",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Image created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON)
            ),
            @ApiResponse(responseCode = "400", description = "Invalid user or image data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the image")
        }
    )
    public Response addUserImage(@PathParam("userId") Integer userId, ImageCreateRequest request) {
        if (userId == null || request == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID and image data are required").build();
        }
        try {
            Long imageId = imageService.addUserImage(userId, request.getPublicId(), request.getSecureUrl(),
                request.getPrimary());
            Map<String, Object> response = new HashMap<>();
            response.put("imageId", imageId);
            return Response.status(Status.CREATED).entity(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @POST
    @Path("/employees/{employeeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "addEmployeeImage",
        summary = "Add employee image",
        description = "Creates an image and links it to an employee",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Image created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON)
            ),
            @ApiResponse(responseCode = "400", description = "Invalid employee or image data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the image")
        }
    )
    public Response addEmployeeImage(@PathParam("employeeId") Integer employeeId, ImageCreateRequest request) {
        if (employeeId == null || request == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID and image data are required").build();
        }
        try {
            Long imageId = imageService.addEmployeeImage(employeeId, request.getPublicId(), request.getSecureUrl(),
                request.getPrimary());
            Map<String, Object> response = new HashMap<>();
            response.put("imageId", imageId);
            return Response.status(Status.CREATED).entity(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Path("/vehicles/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "listVehicleImages",
        summary = "List vehicle images",
        description = "Returns all images linked to a vehicle",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle images retrieved successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class))
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving images")
        }
    )
    public Response listVehicleImages(@PathParam("vehicleId") Integer vehicleId) {
        if (vehicleId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }
        try {
            List<ImageDTO> images = imageService.listVehicleImages(vehicleId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "listUserImages",
        summary = "List user images",
        description = "Returns all images linked to a user",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User images retrieved successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class))
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid user identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving images")
        }
    )
    public Response listUserImages(@PathParam("userId") Integer userId) {
        if (userId == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID is required").build();
        }
        try {
            List<ImageDTO> images = imageService.listUserImages(userId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Path("/employees/{employeeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "listEmployeeImages",
        summary = "List employee images",
        description = "Returns all images linked to an employee",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Employee images retrieved successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class))
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid employee identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving images")
        }
    )
    public Response listEmployeeImages(@PathParam("employeeId") Integer employeeId) {
        if (employeeId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID is required").build();
        }
        try {
            List<ImageDTO> images = imageService.listEmployeeImages(employeeId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @DELETE
    @Path("/{imageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "deleteImage",
        summary = "Delete image",
        description = "Deletes an image by identifier",
        responses = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "400", description = "Invalid image identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the image")
        }
    )
    public Response deleteImage(@PathParam("imageId") Long imageId) {
        if (imageId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Image ID is required").build();
        }
        try {
            boolean deleted = imageService.deleteImage(imageId);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Image not found").build();
            }
            return Response.ok().build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }
}