package com.pinguela.rentexpress.rest.api;

import org.glassfish.jersey.server.ResourceConfig;

import com.pinguela.rentexpress.rest.api.param.DateTimeJsonbProvider;
import com.pinguela.rentexpress.rest.api.param.JavaTimeParamConverterProvider;

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
		// 1) Inyección HK2 (servicios, rate limit, etc.)
		register(new com.pinguela.rentexpress.rest.api.inject.RestApiBinder());

		// 2) Filtros de seguridad / limitación y CORS
		register(com.pinguela.rentexpress.rest.api.security.RateLimitFilter.class);
		register(com.pinguela.rentexpress.rest.api.security.CorsFilter.class);

		// 3) Soporte JSON-B y conversions de tipos (fecha/hora)
		register(org.glassfish.jersey.jsonb.JsonBindingFeature.class);
		register(DateTimeJsonbProvider.class);
		register(JavaTimeParamConverterProvider.class);

		// 4) Swagger / OpenAPI
		register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);

		// 5) Escaneo de recursos y providers de la API
		packages("com.pinguela.rentexpress.rest.api");
	}
}