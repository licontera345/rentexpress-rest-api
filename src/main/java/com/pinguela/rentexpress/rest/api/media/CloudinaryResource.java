package com.pinguela.rentexpress.rest.api.media;

import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.RentexpresExceptionMapper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.CloudinarySignatureDTO;
import com.pinguela.rentexpres.service.CloudinaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.inject.Inject;

@Path("/open/cloudinary")
@Tag(name = "Cloudinary", description = "Operations for Cloudinary signatures")
public class CloudinaryResource {

    private static final Logger logger = Logger.getLogger(CloudinaryResource.class.getName());

    private final CloudinaryService cloudinaryService;

    @Inject
    public CloudinaryResource(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @GET
    @Path("/signature")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "generateCloudinarySignature",
        summary = "Generate Cloudinary signature",
        description = "Generates a Cloudinary signature using the configured credentials",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Signature generated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CloudinarySignatureDTO.class))
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error while generating signature")
        }
    )
    public Response generateSignature() {
        try {
            CloudinarySignatureDTO signature = cloudinaryService.generateSignature();
            return Response.ok(signature).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.buildResponse(e);
        }
    }
}