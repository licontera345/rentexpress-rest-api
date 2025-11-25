package com.pinguela.rentexpress.rest.api;

import org.glassfish.jersey.server.ResourceConfig;

import com.pinguela.rentexpress.rest.api.param.DateTimeJsonbProvider;
import com.pinguela.rentexpress.rest.api.param.JavaTimeParamConverterProvider;
import com.pinguela.rentexpress.rest.api.auth.LoginResource;
import com.pinguela.rentexpress.rest.api.auth.filter.AuthFilter;
import com.pinguela.rentexpress.rest.api.ZOpenAddressResourse;
import com.pinguela.rentexpress.rest.api.ZOpenCityResourse;
import com.pinguela.rentexpress.rest.api.ZOpenEmployeeResourse;
import com.pinguela.rentexpress.rest.api.ZOpenHeadquartersResourse;
import com.pinguela.rentexpress.rest.api.ZOpenProvinceResourse;
import com.pinguela.rentexpress.rest.api.ZOpenRentalResourse;
import com.pinguela.rentexpress.rest.api.ZOpenRentalStatusResourse;
import com.pinguela.rentexpress.rest.api.ZOpenReservationResourse;
import com.pinguela.rentexpress.rest.api.ZOpenReservationStatusResourse;
import com.pinguela.rentexpress.rest.api.ZOpenRoleResourse;
import com.pinguela.rentexpress.rest.api.ZOpenUserResourse;
import com.pinguela.rentexpress.rest.api.ZOpenVehicleCategoryResourse;
import com.pinguela.rentexpress.rest.api.ZOpenVehicleResourse;
import com.pinguela.rentexpress.rest.api.ZOpenVehicleStatusResourse;
import com.pinguela.rentexpress.rest.api.FileResource;

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
                register(ZOpenUserResourse.class);
                register(ZOpenEmployeeResourse.class);
                register(ZOpenAddressResourse.class);
                register(ZOpenCityResourse.class);
                register(ZOpenProvinceResourse.class);
                register(ZOpenRoleResourse.class);
                register(ZOpenHeadquartersResourse.class);
                register(ZOpenVehicleCategoryResourse.class);
                register(ZOpenVehicleStatusResourse.class);
                register(ZOpenVehicleResourse.class);
                register(ZOpenReservationStatusResourse.class);
                register(ZOpenReservationResourse.class);
                register(ZOpenRentalStatusResourse.class);
                register(ZOpenRentalResourse.class);
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
