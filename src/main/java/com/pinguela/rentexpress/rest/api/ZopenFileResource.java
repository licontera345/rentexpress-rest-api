package com.pinguela.rentexpress.rest.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;
import com.pinguela.rentexpres.service.impl.FileServiceImpl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/file")
public class ZopenFileResource {

    private static final Logger logger = Logger.getLogger(ZopenFileResource.class.getName());

    private final FileService fileService;

    public ZopenFileResource() {
        this.fileService = new FileServiceImpl();
    }

    @POST
    @Path("/vehicle/{vehicleId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadVehicleImages(@PathParam("vehicleId") Integer vehicleId,
            @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        if (vehicleId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }

        if (fileParts == null || fileParts.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("At least one file is required").build();
        }

        List<File> tempFiles = new ArrayList<>();
        try {
            for (FormDataBodyPart part : fileParts) {
                if (part == null) {
                    continue;
                }

                FormDataContentDisposition fileDetail = part.getFormDataContentDisposition();
                String originalName = fileDetail != null ? fileDetail.getFileName() : null;

                try (InputStream inputStream = part.getValueAs(InputStream.class)) {
                    if (inputStream == null || originalName == null || originalName.isEmpty()) {
                        continue;
                    }
                    java.nio.file.Path temp = Files.createTempFile("upload-", "-" + originalName);
                    Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
                    tempFiles.add(temp.toFile());
                }
            }

            if (tempFiles.isEmpty()) {
                return Response.status(Status.BAD_REQUEST).entity("No valid files were provided").build();
            }

            fileService.uploadImagesByVehicleId(tempFiles, vehicleId);

            List<File> storedImages = fileService.getImagesByVehicleId(vehicleId);
            List<String> urls = storedImages == null ? Collections.emptyList()
                    : storedImages.stream()
                            .map(file -> fileService.getImageUrl(vehicleId, file.getName()))
                            .collect(Collectors.toList());

            return Response.ok(urls).build();
        } catch (RentexpresException | IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error uploading files").build();
        } finally {
            for (File f : tempFiles) {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    logger.fine("Unable to delete temporary file: " + f.getAbsolutePath());
                }
            }
        }
    }

    @GET
    @Path("/vehicle/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listVehicleImages(@PathParam("vehicleId") Integer vehicleId) {
        if (vehicleId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }

        try {
            List<File> images = fileService.getImagesByVehicleId(vehicleId);
            if (images == null || images.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }

            List<String> urls = images.stream()
                    .map(file -> fileService.getImageUrl(vehicleId, file.getName()))
                    .collect(Collectors.toList());

            return Response.ok(urls).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving images").build();
        }
    }

    @GET
    @Path("/vehicle/{vehicleId}/{imageName}")
    @Produces({ "image/jpeg", "image/png", "image/gif", MediaType.APPLICATION_OCTET_STREAM })
    public Response downloadVehicleImage(@PathParam("vehicleId") Integer vehicleId,
            @PathParam("imageName") String imageName) {

        if (vehicleId == null || imageName == null || imageName.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID and image name are required").build();
        }

        try {
            List<File> images = fileService.getImagesByVehicleId(vehicleId);
            if (images == null || images.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("No images found for vehicle").build();
            }

            for (File image : images) {
                if (imageName.equals(image.getName())) {
                    String contentType = Files.probeContentType(image.toPath());
                    return Response.ok(image, contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM)
                            .build();
                }
            }

            return Response.status(Status.NOT_FOUND).entity("Image not found").build();
        } catch (RentexpresException | IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error downloading image").build();
        }
    }
}
