package com.pinguela.rentexpress.rest.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;
import com.pinguela.rentexpres.service.impl.FileServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/files")
public class FileResource {

    private static final Logger logger = Logger.getLogger(FileResource.class.getName());
    private static final String VEHICLE_IMAGES_FIELD = "images";
    private static final String DEFAULT_EXTENSION = ".tmp";

    private final FileService fileService;

    public FileResource() {
        this.fileService = new FileServiceImpl();
    }

    @GET
    @Path("/vehicles/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "getVehicleImages",
        summary = "Retrieve vehicle gallery",
        description = "Returns the public URLs of every stored vehicle image",
        responses = {
            @ApiResponse(responseCode = "200", description = "Images found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = VehicleImageResponse[].class))),
            @ApiResponse(responseCode = "204", description = "Vehicle has no images"),
            @ApiResponse(responseCode = "400", description = "Vehicle identifier is required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving vehicle images")
        }
    )
    public Response getVehicleImages(@PathParam("vehicleId") Integer vehicleId) {
        if (vehicleId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }
        try {
            List<File> images = fileService.getImagesByVehicleId(vehicleId);
            if (images == null || images.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            List<VehicleImageResponse> payload = buildVehicleImageResponse(vehicleId, images);
            return Response.ok(payload).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/vehicles/{vehicleId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "uploadVehicleImages",
        summary = "Upload images for vehicle",
        description = "Stores the provided files and returns the updated gallery",
        responses = {
            @ApiResponse(responseCode = "200", description = "Vehicle gallery updated",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = VehicleImageResponse[].class))),
            @ApiResponse(responseCode = "400", description = "Vehicle identifier and files are required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while uploading images")
        }
    )
    public Response uploadVehicleImages(@PathParam("vehicleId") Integer vehicleId, FormDataMultiPart multiPart) {
        if (vehicleId == null) {
            closeQuietly(multiPart);
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }
        if (multiPart == null) {
            return Response.status(Status.BAD_REQUEST).entity("Multipart request is required").build();
        }

        try (FormDataMultiPart formData = multiPart) {
            List<FormDataBodyPart> bodyParts = formData.getFields(VEHICLE_IMAGES_FIELD);
            if (bodyParts == null || bodyParts.isEmpty()) {
                return Response.status(Status.BAD_REQUEST).entity("No files provided").build();
            }

            List<File> tempFiles = new ArrayList<>();
            try {
                for (FormDataBodyPart part : bodyParts) {
                    if (part == null) {
                        continue;
                    }
                    tempFiles.add(writeToTempFile(part.getValueAs(InputStream.class),
                            getOriginalFileName(part.getFormDataContentDisposition())));
                }

                if (tempFiles.isEmpty()) {
                    return Response.status(Status.BAD_REQUEST).entity("No valid files provided").build();
                }

                fileService.uploadImagesByVehicleId(tempFiles, vehicleId);
                List<File> images = fileService.getImagesByVehicleId(vehicleId);
                List<VehicleImageResponse> payload = buildVehicleImageResponse(vehicleId, images);
                return Response.ok(payload).build();
            } catch (RentexpresException e) {
                logger.warning(e.getMessage());
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            } catch (IOException e) {
                logger.warning(e.getMessage());
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to store uploaded files").build();
            } finally {
                tempFiles.forEach(this::deleteQuietly);
            }
        }
    }

    @GET
    @Path("/users/{userId}/avatar")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(
        operationId = "getUserAvatar",
        summary = "Download user avatar",
        description = "Streams the avatar image assigned to the provided user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Avatar found"),
            @ApiResponse(responseCode = "400", description = "User identifier is required"),
            @ApiResponse(responseCode = "404", description = "Avatar not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving avatar")
        }
    )
    public Response getUserAvatar(@PathParam("userId") Integer userId) {
        if (userId == null) {
            return Response.status(Status.BAD_REQUEST).entity("User ID is required").build();
        }
        try {
            File avatar = fileService.getImageByUserId(userId);
            return buildFileResponse(avatar);
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to read avatar").build();
        }
    }

    @POST
    @Path("/users/{userId}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "uploadUserAvatar",
        summary = "Upload user avatar",
        description = "Stores the avatar provided for the user and returns the public URL",
        responses = {
            @ApiResponse(responseCode = "200", description = "Avatar updated",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "User identifier and file are required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while uploading avatar")
        }
    )
    public Response uploadUserAvatar(@PathParam("userId") Integer userId,
            @FormDataParam("image") InputStream uploadedInputStream,
            @FormDataParam("image") FormDataContentDisposition fileDisposition) {

        if (userId == null) {
            IOUtils.closeQuietly(uploadedInputStream);
            return Response.status(Status.BAD_REQUEST).entity("User ID is required").build();
        }
        if (uploadedInputStream == null) {
            return Response.status(Status.BAD_REQUEST).entity("Avatar file is required").build();
        }

        File tempFile = null;
        try {
            tempFile = writeToTempFile(uploadedInputStream, getOriginalFileName(fileDisposition));
            fileService.uploadImageByUserId(tempFile, userId);
            Map<String, String> payload = Collections.singletonMap("avatarUrl", fileService.getUserAvatarUrl(userId));
            return Response.ok(payload).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to store avatar").build();
        } finally {
            IOUtils.closeQuietly(uploadedInputStream);
            deleteQuietly(tempFile);
        }
    }

    @GET
    @Path("/employees/{employeeId}/avatar")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(
        operationId = "getEmployeeAvatar",
        summary = "Download employee avatar",
        responses = {
            @ApiResponse(responseCode = "200", description = "Avatar found"),
            @ApiResponse(responseCode = "400", description = "Employee identifier is required"),
            @ApiResponse(responseCode = "404", description = "Avatar not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving avatar")
        }
    )
    public Response getEmployeeAvatar(@PathParam("employeeId") Integer employeeId) {
        if (employeeId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID is required").build();
        }
        try {
            File avatar = fileService.getImageByEmployeeId(employeeId);
            return buildFileResponse(avatar);
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to read avatar").build();
        }
    }

    @POST
    @Path("/employees/{employeeId}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "uploadEmployeeAvatar",
        summary = "Upload employee avatar",
        responses = {
            @ApiResponse(responseCode = "200", description = "Avatar updated",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Employee identifier and file are required"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while uploading avatar")
        }
    )
    public Response uploadEmployeeAvatar(@PathParam("employeeId") Integer employeeId,
            @FormDataParam("image") InputStream uploadedInputStream,
            @FormDataParam("image") FormDataContentDisposition fileDisposition) {

        if (employeeId == null) {
            IOUtils.closeQuietly(uploadedInputStream);
            return Response.status(Status.BAD_REQUEST).entity("Employee ID is required").build();
        }
        if (uploadedInputStream == null) {
            return Response.status(Status.BAD_REQUEST).entity("Avatar file is required").build();
        }

        File tempFile = null;
        try {
            tempFile = writeToTempFile(uploadedInputStream, getOriginalFileName(fileDisposition));
            fileService.uploadImageByEmployeeId(tempFile, employeeId);
            Map<String, String> payload = Collections.singletonMap("avatarUrl", fileService.getEmployeeAvatarUrl(employeeId));
            return Response.ok(payload).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to store avatar").build();
        } finally {
            IOUtils.closeQuietly(uploadedInputStream);
            deleteQuietly(tempFile);
        }
    }

    private List<VehicleImageResponse> buildVehicleImageResponse(Integer vehicleId, List<File> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files.stream()
                .map(file -> new VehicleImageResponse(file.getName(), fileService.getImageUrl(vehicleId, file.getName())))
                .collect(Collectors.toList());
    }

    private File writeToTempFile(InputStream inputStream, String originalFileName) throws IOException {
        if (inputStream == null) {
            throw new IOException("Input stream is null");
        }
        String suffix = extractExtension(originalFileName);
        Path tempFile = Files.createTempFile("upload-", suffix);
        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
        return tempFile.toFile();
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.lastIndexOf('.') < 0) {
            return DEFAULT_EXTENSION;
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    private void deleteQuietly(File file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            logger.fine(() -> "Unable to delete temp file: " + file.getAbsolutePath());
        }
    }

    private Response buildFileResponse(File file) throws IOException {
        if (file == null || !file.exists()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return Response.ok(file, contentType)
                .header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"")
                .build();
    }

    private String getOriginalFileName(FormDataContentDisposition disposition) {
        return disposition == null ? null : disposition.getFileName();
    }

    private void closeQuietly(FormDataMultiPart multiPart) {
        if (multiPart != null) {
            try {
                multiPart.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    private static class VehicleImageResponse {
        private String fileName;
        private String url;

        public VehicleImageResponse() {
        }

        public VehicleImageResponse(String fileName, String url) {
            this.fileName = fileName;
            this.url = url;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
