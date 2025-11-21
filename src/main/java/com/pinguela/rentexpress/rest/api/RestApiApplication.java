package com.pinguela.rentexpress.rest.api;

import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.server.ResourceConfig;

import com.pinguela.rentexpress.rest.api.param.DateTimeJsonbProvider;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.ApplicationPath;

@OpenAPIDefinition(
    info = @Info(
        title = "Rest API",
        version = "1.0",
        description = "demo API",
        contact = @Contact(
            name = "API Support",
            email = "support@restapi.local",
            url = "https://restapi.local"
        ),
        license = @License(
            name = "MIT",
            url = "http://localhost:8080/rentexpress-rest-api/swagger-ui/index.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080/rentexpress-rest-api")
    }
)
@ApplicationPath("/api")
public class RestApiApplication extends ResourceConfig {

    public RestApiApplication() {
        packages(RestApiApplication.class.getPackage().getName());
        packages("com.pinguela.rentexpress.rest.api");
        register(JaxrsProviders.class);
        register(DateTimeJsonbProvider.class);
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
        register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);
    }
}