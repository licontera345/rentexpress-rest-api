package com.pinguela.rentexpress.rest.api;

import org.glassfish.jersey.server.ResourceConfig;

import com.pinguela.rentexpress.rest.api.address.AddressResource;
import com.pinguela.rentexpress.rest.api.auth.LoginResource;
import com.pinguela.rentexpress.rest.api.auth.filter.AuthFilter;
import com.pinguela.rentexpress.rest.api.role.RoleResource;
import com.pinguela.rentexpress.rest.api.employee.EmployeeResource;
import com.pinguela.rentexpress.rest.api.file.FileResource;
import com.pinguela.rentexpress.rest.api.province.ProvinceResource;
import com.pinguela.rentexpress.rest.api.reservationstatus.ReservationStatusResource;
import com.pinguela.rentexpress.rest.api.rentalstatus.RentalStatusResource;
import com.pinguela.rentexpress.rest.api.user.UserResource;
import com.pinguela.rentexpress.rest.api.city.CityResource;
import com.pinguela.rentexpress.rest.api.headquarters.HeadquartersResource;
import com.pinguela.rentexpress.rest.api.param.DateTimeJsonbProvider;
import com.pinguela.rentexpress.rest.api.param.JavaTimeParamConverterProvider;
import com.pinguela.rentexpress.rest.api.reservation.ReservationResource;
import com.pinguela.rentexpress.rest.api.rental.RentalResource;
import com.pinguela.rentexpress.rest.api.vehicle.VehicleResource;
import com.pinguela.rentexpress.rest.api.vehiclecategory.VehicleCategoryResource;
import com.pinguela.rentexpress.rest.api.vehiclestatus.VehicleStatusResource;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.ApplicationPath;

@OpenAPIDefinition(info = @Info(title = "Rest API",
version = "1.0", description = "demo API",
contact = @Contact(name = "API Support",
email = "support@restapi.local",
url = "https://restapi.local"),
license = @License(name = "MIT",
url = "http://localhost:8080/rentexpress-rest-api/swagger-ui/index.html")), servers = {
                @Server(url = "http://localhost:8080/rentexpress-rest-api") })
@ApplicationPath("/api")
public class RestApiApplication extends ResourceConfig {

        public RestApiApplication() {
                // Recursos REST
                register(UserResource.class);
                register(EmployeeResource.class);
                register(AddressResource.class);
                register(CityResource.class);
                register(ProvinceResource.class);
                register(RoleResource.class);
                register(HeadquartersResource.class);
                register(VehicleCategoryResource.class);
                register(VehicleStatusResource.class);
                register(VehicleResource.class);
                register(ReservationStatusResource.class);
                register(ReservationResource.class);
                register(RentalStatusResource.class);
                register(RentalResource.class);
                register(FileResource.class);
                register(LoginResource.class);

                // Filtro de autenticación
                register(AuthFilter.class);

                // Proveedores y utilidades
                register(DateTimeJsonbProvider.class);
                // jersey-media-multipart se autodetecta; no se registra manualmente para evitar duplicados.
                register(JavaTimeParamConverterProvider.class);
                register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);
        }
}
