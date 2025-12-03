package com.pinguela.rentexpress.rest.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;
import com.pinguela.rentexpres.service.impl.FileServiceImpl;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
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

import com.pinguela.rentexpress.rest.api.security.Secured;

@Path("/file")
@Tag(name = "File Management", description = "APIs for managing vehicle images and user/employee avatars")	
public class FileResource {

    private static final Logger logger = Logger.getLogger(FileResource.class.getName());

    private final FileService fileService;

    public FileResource() {
        this.fileService = new FileServiceImpl();
    }

    @GET
    @Path("/vehicle/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listVehicleImages(@PathParam("vehicleId") Integer vehicleId) {
        if (vehicleId == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }
        try {
            List<String> images = fileService.listVehicleImages(vehicleId);
            return Response.ok(images).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/vehicle/{vehicleId}/{imageName}")
    @Produces({ "image/jpeg", "image/png", "image/gif", MediaType.APPLICATION_OCTET_STREAM })
    public Response getVehicleImage(@PathParam("vehicleId") Integer vehicleId, @PathParam("imageName") String imageName) {
        try {
            byte[] data = fileService.getVehicleImage(vehicleId, imageName);
            return Response.ok(data).type(resolveMediaType(imageName)).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = Status.INTERNAL_SERVER_ERROR;
            if ("Vehicle id and image name are required".equals(e.getMessage())) {
                status = Status.BAD_REQUEST;
            } else if ("Vehicle image not found".equals(e.getMessage())) {
                status = Status.NOT_FOUND;
            }
            return Response.status(status).entity(e.getMessage()).build();
        }
    }

    @POST
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Path("/vehicle/{vehicleId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadVehicleImage(@PathParam("vehicleId") Integer vehicleId,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (fileInputStream == null || fileDetail == null || fileDetail.getFileName() == null
                || fileDetail.getFileName().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("File is required").build();
        }

        try {
            byte[] data = toByteArray(fileInputStream);
            fileService.saveVehicleImage(vehicleId, fileDetail.getFileName(), data);
            return Response.status(Status.CREATED).entity("Image uploaded successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = "Vehicle id and image name are required".equals(e.getMessage())
                    || "Image data is required".equals(e.getMessage()) ? Status.BAD_REQUEST : Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error processing file").build();
        }
    }

    @DELETE
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Path("/vehicle/{vehicleId}/{imageName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVehicleImage(@PathParam("vehicleId") Integer vehicleId,
            @PathParam("imageName") String imageName) {
        try {
            fileService.deleteVehicleImage(vehicleId, imageName);
            return Response.ok("Image deleted").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = "Vehicle id and image name are required".equals(e.getMessage()) ? Status.BAD_REQUEST
                    : Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/user-avatar/{userId}")
    @Produces({ "image/jpeg", "image/png", MediaType.APPLICATION_OCTET_STREAM })
    public Response getUserAvatar(@PathParam("userId") Integer userId) {
        try {
            byte[] data = fileService.getUserAvatar(userId);
            if (data == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(data).type("image/jpeg").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = userId == null ? Status.BAD_REQUEST : Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(e.getMessage()).build();
        }
    }

    @POST
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Path("/user-avatar/{userId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadUserAvatar(@PathParam("userId") Integer userId,
            @FormDataParam("file") InputStream fileInputStream) {

        if (fileInputStream == null) {
            return Response.status(Status.BAD_REQUEST).entity("File is required").build();
        }

        try {
            byte[] data = toByteArray(fileInputStream);
            fileService.saveUserAvatar(userId, data);
            return Response.status(Status.CREATED).entity("Avatar uploaded successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = userId == null ? Status.BAD_REQUEST : Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error processing file").build();
        }
    }

    @GET
    @Path("/employee-avatar/{employeeId}")
    @Produces({ "image/jpeg", "image/png", MediaType.APPLICATION_OCTET_STREAM })
    public Response getEmployeeAvatar(@PathParam("employeeId") Integer employeeId) {
        try {
            byte[] data = fileService.getEmployeeAvatar(employeeId);
            if (data == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(data).type("image/jpeg").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = employeeId == null ? Status.BAD_REQUEST : Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(e.getMessage()).build();
        }
    }

    @POST
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Path("/employee-avatar/{employeeId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadEmployeeAvatar(@PathParam("employeeId") Integer employeeId,
            @FormDataParam("file") InputStream fileInputStream) {

        if (fileInputStream == null) {
            return Response.status(Status.BAD_REQUEST).entity("File is required").build();
        }

        try {
            byte[] data = toByteArray(fileInputStream);
            fileService.saveEmployeeAvatar(employeeId, data);
            return Response.status(Status.CREATED).entity("Avatar uploaded successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            Status status = employeeId == null ? Status.BAD_REQUEST : Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error processing file").build();
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private String resolveMediaType(String imageName) {
        if (imageName == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String lowerName = imageName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerName.endsWith(".gif")) {
            return "image/gif";
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
