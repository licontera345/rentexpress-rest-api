package com.pinguela.rentexpress.rest.api;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;
import com.pinguela.rentexpres.service.impl.FileServiceImpl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@jakarta.ws.rs.Path("/file")
public class FileResource {

	private final FileService fileService;

	public FileResource() {
		this.fileService = new FileServiceImpl();
	}

	@POST
	@jakarta.ws.rs.Path("/user-avatar")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadUserAvatar(@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("userId") Integer userId) {

		if (userId == null || fileInputStream == null || fileDetail == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Datos incompletos\"}").build();
		}

		try {
			String fileName = fileDetail.getFileName();

			// CREATE TEMP FILE
			Path temp = Files.createTempFile("upload-", "-" + fileName);

			// COPY CORRECTO — Java 8 necesita array
			Files.copy(fileInputStream, temp, new StandardCopyOption[] { StandardCopyOption.REPLACE_EXISTING });

			File tempFile = temp.toFile();

			fileService.uploadImageByUserId(tempFile, userId);

			String url = fileService.getUserAvatarUrl(userId);

			return Response.ok("{\"avatarUrl\":\"" + url + "\"}").build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"No se pudo guardar\"}")
					.build();
		}
	}

	@POST
	@jakarta.ws.rs.Path("/test-user-image")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testUserImage(@FormDataParam("userId") Integer userId) throws RentexpresException {

		if (userId == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"userId requerido\"}").build();
		}

		File f = fileService.getImageByUserId(userId);

		if (f == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"No se encontró imagen\"}").build();
		}

		return Response.ok("{\"file\":\"" + f.getAbsolutePath() + "\"}").build();
	}
}
