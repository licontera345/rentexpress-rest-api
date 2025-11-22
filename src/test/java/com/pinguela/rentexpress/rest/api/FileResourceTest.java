package com.pinguela.rentexpress.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.service.FileService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class FileResourceTest extends JerseyTest {

    @Mock
    private FileService fileService;

    @Override
    protected Application configure() {
        MockitoAnnotations.openMocks(this);
        FileResource resource = new FileResource();
        injectFileService(resource);
        return new ResourceConfig()
                .register(resource)
                .register(MultiPartFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    private void injectFileService(FileResource resource) {
        try {
            Field field = FileResource.class.getDeclaredField("fileService");
            field.setAccessible(true);
            field.set(resource, fileService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void listVehicleImagesReturnsOk() {
        when(fileService.listVehicleImages(1)).thenReturn(Arrays.asList("img1.jpg", "img2.jpg"));

        Response response = target("/file/vehicle/1").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void getVehicleImageReturnsOk() {
        when(fileService.getVehicleImage(1, "img.jpg")).thenReturn(new byte[] { 1, 2, 3 });

        Response response = target("/file/vehicle/1/img.jpg").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void uploadVehicleImageReturnsCreated() {
        doNothing().when(fileService).saveVehicleImage(eq(1), eq("img.jpg"), any());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        StreamDataBodyPart filePart = new StreamDataBodyPart("file", inputStream, "img.jpg", MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.bodyPart(filePart);

        Response response = target("/file/vehicle/1")
                .request()
                .post(Entity.entity(multiPart, multiPart.getMediaType()));

        assertEquals(201, response.getStatus());
    }

    @Test
    void deleteVehicleImageReturnsOk() {
        doNothing().when(fileService).deleteVehicleImage(1, "img.jpg");

        Response response = target("/file/vehicle/1/img.jpg").request().delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    void getUserAvatarReturnsOk() {
        when(fileService.getUserAvatar(2)).thenReturn(new byte[] { 4, 5, 6 });

        Response response = target("/file/user-avatar/2").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void uploadUserAvatarReturnsCreated() {
        doNothing().when(fileService).saveUserAvatar(eq(2), any());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 7, 8, 9 });
        StreamDataBodyPart filePart = new StreamDataBodyPart("file", inputStream, "avatar.jpg", MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.bodyPart(filePart);

        Response response = target("/file/user-avatar/2")
                .request()
                .post(Entity.entity(multiPart, multiPart.getMediaType()));

        assertEquals(201, response.getStatus());
    }

    @Test
    void getEmployeeAvatarReturnsOk() {
        when(fileService.getEmployeeAvatar(3)).thenReturn(new byte[] { 10, 11, 12 });

        Response response = target("/file/employee-avatar/3").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void uploadEmployeeAvatarReturnsCreated() {
        doNothing().when(fileService).saveEmployeeAvatar(eq(3), any());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 13, 14, 15 });
        StreamDataBodyPart filePart = new StreamDataBodyPart("file", inputStream, "employee.jpg", MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.bodyPart(filePart);

        Response response = target("/file/employee-avatar/3")
                .request()
                .post(Entity.entity(multiPart, multiPart.getMediaType()));

        assertEquals(201, response.getStatus());
    }
}
