package com.pinguela.rentexpress.rest.api;

import java.util.Collections;

import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;

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
public class RestApiApplication extends ResourceConfig {

    public RestApiApplication() {
        String resourcePackage = RestApiApplication.class.getPackage().getName();

        packages(resourcePackage, OpenApiResource.class.getPackage().getName());
        register(JaxrsProviders.class);
        register(SwaggerSerializers.class);

        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration()
                .prettyPrint(true)
                .resourcePackages(Collections.singleton(resourcePackage));

        register(new OpenApiResource().openApiConfiguration(swaggerConfiguration));
        register(new AcceptHeaderOpenApiResource().openApiConfiguration(swaggerConfiguration));
    }
}
