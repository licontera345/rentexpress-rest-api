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
		title = "RentExpress API",
		version = "1.0",
		description = "API REST de RentExpress: reservas, vehículos, usuarios, recomendaciones IA y clima. Documentación técnica completa en Swagger.",
		contact = @Contact(
				name = "RentExpress",
				email = "support@rentexpress.local",
				url = "https://rentexpress.local"),
		license = @License(
				name = "Proyecto interno",
				url = "https://rentexpress.local")),
servers = {
		@Server(url = "http://localhost:8081/rentexpress-rest-api", description = "Servidor local"),
		@Server(url = "/rentexpress-rest-api", description = "Servidor actual (relativo al host de despliegue)") })
@ApplicationPath("/api")
public class RestApiApplication extends ResourceConfig {
	public RestApiApplication() {
		packages("com.pinguela.rentexpress.rest.api");
		register(new com.pinguela.rentexpress.rest.api.inject.RestApiBinder());
		register(org.glassfish.jersey.jsonb.JsonBindingFeature.class);
		register(DateTimeJsonbProvider.class);
		register(JavaTimeParamConverterProvider.class);
		register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);
		register(com.pinguela.rentexpress.rest.api.security.CorsFilter.class);
	}
}