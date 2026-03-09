package com.pinguela.rentexpress.rest.api.media;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.pinguela.rentexpress.rest.api.RentexpresExceptionMapper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ImageDTO;
import com.pinguela.rentexpres.model.ImageOwnerDTO;
import com.pinguela.rentexpres.service.ImageService;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;
import com.pinguela.rentexpress.rest.api.dto.ImageCreateRequest;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.service.OwnershipService;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

import jakarta.inject.Inject;

@Path("/images")
@Tag(name = "Images", description = "Operations for managing vehicle, user, and employee images")
public class ImageResource {

    private static final Logger logger = Logger.getLogger(ImageResource.class.getName());
    private static final Pattern CLOUDINARY_UPLOAD = Pattern.compile("(/image/upload)(/v\\d+/|/)([^/]+)$");

    private final ImageService imageService;
    private final OwnershipService ownershipService;

    @Inject
    public ImageResource(ImageService imageService, OwnershipService ownershipService) {
        this.imageService = imageService;
        this.ownershipService = ownershipService;
    }

    /**
     * Descarga segura: valida permisos y redirige a la URL de la imagen (Cloudinary).
     * Opcional: width y height para redimensionado ligero vía Cloudinary.
     */
    @GET
    @Path("/{imageId}/download")
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE", "CLIENT" })
    @Operation(
        operationId = "downloadImage",
        summary = "Secure image download",
        description = "Returns redirect to image URL after validating permissions. Optional width/height for resizing.",
        responses = {
            @ApiResponse(responseCode = "302", description = "Redirect to image URL"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Image not found")
        }
    )
    public Response downloadImage(
            @PathParam("imageId") Long imageId,
            @QueryParam("width") Integer width,
            @QueryParam("height") Integer height,
            @jakarta.ws.rs.core.Context SecurityContext securityContext) {
        if (imageId == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Image ID is required"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        try {
            ImageDTO image = imageService.findById(imageId);
            if (image == null || image.getSecureUrl() == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity(new ErrorResponseDTO("NOT_FOUND", "Image not found"))
                        .type(MediaType.APPLICATION_JSON).build();
            }
            ImageOwnerDTO owner = imageService.getImageOwner(imageId);
            if (owner == null || owner.getEntityType() == null || owner.getEntityId() == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity(new ErrorResponseDTO("NOT_FOUND", "Image not found"))
                        .type(MediaType.APPLICATION_JSON).build();
            }
            String principalId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
            boolean adminOrEmployee = securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("EMPLOYEE");
            boolean allowed = false;
            if ("USER".equals(owner.getEntityType())) {
                allowed = adminOrEmployee || (principalId != null && ownershipService.checkOwnership("CLIENT_MATCH", String.valueOf(owner.getEntityId()), principalId));
            } else if ("VEHICLE".equals(owner.getEntityType()) || "EMPLOYEE".equals(owner.getEntityType())) {
                allowed = adminOrEmployee || (principalId != null && "EMPLOYEE".equals(owner.getEntityType()) && principalId.equals(String.valueOf(owner.getEntityId())));
            }
            if (!allowed) {
                return Response.status(Status.FORBIDDEN)
                        .entity(new ErrorResponseDTO("FORBIDDEN", "Access denied to this image"))
                        .type(MediaType.APPLICATION_JSON).build();
            }
            String url = image.getSecureUrl();
            if ((width != null && width > 0) || (height != null && height > 0)) {
                url = buildCloudinaryTransformedUrl(url, width != null ? width : 0, height != null ? height : 0);
            }
            return Response.temporaryRedirect(URI.create(url)).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
        }
    }

    private static String buildCloudinaryTransformedUrl(String secureUrl, int width, int height) {
        if (secureUrl == null) return secureUrl;
        int w = Math.min(Math.max(width, 0), 2000);
        int h = Math.min(Math.max(height, 0), 2000);
        if (w == 0 && h == 0) return secureUrl;
        String transform = "w_" + (w > 0 ? w : "auto") + ",h_" + (h > 0 ? h : "auto") + ",c_fill";
        java.util.regex.Matcher m = CLOUDINARY_UPLOAD.matcher(secureUrl);
        if (m.find()) {
            return m.replaceFirst("$1/" + transform + "$2$3");
        }
        return secureUrl;
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Vehicle ID and image data are required");
        }
        try {
            Long imageId = imageService.addVehicleImage(vehicleId, request.getPublicId(), request.getSecureUrl(),
                request.getPrimary());
            Map<String, Object> response = new HashMap<>();
            response.put("imageId", imageId);
            return Response.status(Status.CREATED).entity(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "User ID and image data are required");
        }
        try {
            Long imageId = imageService.addUserImage(userId, request.getPublicId(), request.getSecureUrl(),
                request.getPrimary());
            Map<String, Object> response = new HashMap<>();
            response.put("imageId", imageId);
            return Response.status(Status.CREATED).entity(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee ID and image data are required");
        }
        try {
            Long imageId = imageService.addEmployeeImage(employeeId, request.getPublicId(), request.getSecureUrl(),
                request.getPrimary());
            Map<String, Object> response = new HashMap<>();
            response.put("imageId", imageId);
            return Response.status(Status.CREATED).entity(response).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Vehicle ID is required");
        }
        try {
            List<ImageDTO> images = imageService.listVehicleImages(vehicleId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "User ID is required");
        }
        try {
            List<ImageDTO> images = imageService.listUserImages(userId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee ID is required");
        }
        try {
            List<ImageDTO> images = imageService.listEmployeeImages(employeeId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
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
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Image ID is required");
        }
        try {
            boolean deleted = imageService.deleteImage(imageId);
            if (!deleted) {
                return ErrorResponseHelper.notFound("NOT_FOUND", "Image not found");
            }
            return Response.ok().build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
        }
    }
}