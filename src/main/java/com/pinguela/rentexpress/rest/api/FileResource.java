// package com.pinguela.rentexpress.rest.api;

// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.util.List;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
// import org.glassfish.jersey.media.multipart.FormDataParam;

// import com.pinguela.rentexpres.exception.RentexpresException;
// import com.pinguela.rentexpres.service.FileService;
// import com.pinguela.rentexpres.service.impl.FileServiceImpl;

// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.ws.rs.Consumes;
// import jakarta.ws.rs.DELETE;
// import jakarta.ws.rs.GET;
// import jakarta.ws.rs.POST;
// import jakarta.ws.rs.Path;
// import jakarta.ws.rs.PathParam;
// import jakarta.ws.rs.Produces;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;
// import jakarta.ws.rs.core.Response.Status;

// @Path("open/file")
// @Tag(name = "File Management", description = "APIs for managing vehicle images and avatars")
// public class FileResource {

//     private static final Logger logger = LogManager.getLogger(FileResource.class);
//     private final FileService fileService;

//     public FileResource() {
//         this.fileService = new FileServiceImpl();
//     }

//     // ================= VEHÍCULOS =================

//     @GET
//     @Path("/vehicle/{vehicleId}")
//     @Produces(MediaType.APPLICATION_JSON)
//     public Response listVehicleImages(@PathParam("vehicleId") Integer vehicleId) {
//         if (vehicleId == null) {
//             return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
//         }

//         try {
//             List<String> images = fileService.listVehicleImages(vehicleId);
//             return Response.ok(images).build();
//         } catch (RentexpresException e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     @GET
//     @Path("/vehicle/{vehicleId}/{imageName}")
//     public Response getVehicleImage(@PathParam("vehicleId") Integer vehicleId,
//                                     @PathParam("imageName") String imageName) {

//         try {
//             String imageUrl = fileService.getVehicleImage(vehicleId, imageName);
            

//             if (imageUrl == null) {
//                 return Response.status(Status.NOT_FOUND).build();
//             }

//             return Response.status(Status.TEMPORARY_REDIRECT)
//                     .header("Location", imageUrl)
//                     .build();

//         } catch (RentexpresException e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     @POST
//     @Path("/vehicle/{vehicleId}")
//     @Consumes(MediaType.MULTIPART_FORM_DATA)
//     public Response uploadVehicleImage(@PathParam("vehicleId") Integer vehicleId,
//                                        @FormDataParam("file") InputStream fileInputStream,
//                                        @FormDataParam("file") FormDataContentDisposition fileDetail) {

//         try {
//             byte[] data = toByteArray(fileInputStream);
//             fileService.saveVehicleImage(vehicleId, fileDetail.getFileName(), data);
//             return Response.status(Status.CREATED).build();
//         } catch (Exception e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     @DELETE
//     @Path("/vehicle/{vehicleId}/{imageName}")
//     public Response deleteVehicleImage(@PathParam("vehicleId") Integer vehicleId,
//                                        @PathParam("imageName") String imageName) {

//         try {
//             fileService.deleteVehicleImage(vehicleId, imageName);
//             return Response.ok().build();
//         } catch (RentexpresException e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     // ================= USUARIOS =================

//     @GET
//     @Path("/user-avatar/{userId}")
//     public Response getUserAvatar(@PathParam("userId") Integer userId) {

//         try {
//             String avatarUrl = fileService.getUserAvatarUrl(userId);

//             if (avatarUrl == null) {
//                 return Response.status(Status.NOT_FOUND).build();
//             }

//             return Response.status(Status.TEMPORARY_REDIRECT)
//                     .header("Location", avatarUrl)
//                     .build();

//         } catch (RentexpresException e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     @POST
//     @Path("/user-avatar/{userId}")
//     @Consumes(MediaType.MULTIPART_FORM_DATA)
//     public Response uploadUserAvatar(@PathParam("userId") Integer userId,
//                                      @FormDataParam("file") InputStream fileInputStream) {

//         try {
//             byte[] data = toByteArray(fileInputStream);
//             fileService.saveUserAvatar(userId, data);
//             return Response.status(Status.CREATED).build();
//         } catch (Exception e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     // ================= EMPLEADOS =================

//     @GET
//     @Path("/employee-avatar/{employeeId}")
//     public Response getEmployeeAvatar(@PathParam("employeeId") Integer employeeId) {

//         try {
//             String avatarUrl = fileService.getEmployeeAvatar(employeeId );

//             if (avatarUrl == null) {
//                 return Response.status(Status.NOT_FOUND).build();
//             }

//             return Response.status(Status.TEMPORARY_REDIRECT)
//                     .header("Location", avatarUrl)
//                     .build();

//         } catch (RentexpresException e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     @POST
//     @Path("/employee-avatar/{employeeId}")
//     @Consumes(MediaType.MULTIPART_FORM_DATA)
//     public Response uploadEmployeeAvatar(@PathParam("employeeId") Integer employeeId,
//                                          @FormDataParam("file") InputStream fileInputStream) {

//         try {
//             byte[] data = toByteArray(fileInputStream);
//             fileService.saveEmployeeAvatar(employeeId, data);
//             return Response.status(Status.CREATED).build();
//         } catch (Exception e) {
//             return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
//         }
//     }

//     private byte[] toByteArray(InputStream inputStream) throws IOException {
//         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//         byte[] data = new byte[8192];
//         int bytesRead;

//         while ((bytesRead = inputStream.read(data)) != -1) {
//             buffer.write(data, 0, bytesRead);
//         }

//         return buffer.toByteArray();
//     }
// }