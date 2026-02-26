package com.pinguela.rentexpress.rest.api;

import org.glassfish.jersey.server.ResourceConfig;

import com.pinguela.rentexpress.rest.api.param.JavaTimeParamConverterProvider;
import com.pinguela.rentexpress.rest.api.util.DateTimeJsonbProvider;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.ApplicationPath;

@OpenAPIDefinition(info = @Info(
		title = "Rest API", 
		version = "1.0", 
		description = "demo API", 
		contact = @Contact(
				name = "API Support", 
				email = "support@restapi.local", 
				url = "https://restapi.local"), 
		license = @License(
				name = "MIT", 
				url = "https://localhost:8081/rentexpress-rest-api/swagger-ui/index.html")), 
servers = {
		@Server(url = "http://localhost:8081/rentexpress-rest-api", description = "Servidor Local"),
		
		@Server(url = "https://94.130.104.92:8443/rentexpress-rest-api", description = "Servidor Producción") })
@ApplicationPath("/api")
public class RestApiApplication extends ResourceConfig {
	public RestApiApplication() {
		packages("com.pinguela.rentexpress.rest.api");
		register(org.glassfish.jersey.jsonb.JsonBindingFeature.class);
		register(DateTimeJsonbProvider.class);
		register(JavaTimeParamConverterProvider.class);
		register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);
		register(com.pinguela.rentexpress.rest.api.security.CorsFilter.class);
	}
}