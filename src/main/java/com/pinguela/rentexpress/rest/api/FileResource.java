package com.pinguela.rentexpress.rest.api;

import java.io.File;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;
import com.pinguela.rentexpres.service.impl.FileServiceImpl;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/file")
public class FileResource {

	private final FileService fileService;

	public FileResource() {
		this.fileService = new FileServiceImpl();
	}

	@POST
	@Path("/test-user-image")
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
