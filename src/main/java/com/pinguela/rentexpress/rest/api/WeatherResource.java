package com.pinguela.rentexpress.rest.api;

import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.dto.WeatherDTO;
import com.pinguela.rentexpress.rest.api.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/open/weather")
@Tag(name = "Weather", description = "Weather information for headquarters locations")
public class WeatherResource {

    private static final Logger logger = Logger.getLogger(WeatherResource.class.getName());

    private final WeatherService weatherService;

    public WeatherResource() {
        this.weatherService = new WeatherService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "getWeatherByCity",
        summary = "Get current weather for a city",
        description = "Returns current weather data for the specified city",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Weather data retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = WeatherDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "City parameter is required"),
            @ApiResponse(responseCode = "500", description = "Could not retrieve weather data")
        }
    )
    public Response getWeatherByCity(
            @Parameter(description = "City name") @QueryParam("city") String city,
            @Parameter(description = "Language code (es, en, fr)") @QueryParam("lang") String lang) {
        try {
            if (city == null || city.trim().isEmpty()) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("El parámetro 'city' es obligatorio").build();
            }

            WeatherDTO weather = weatherService.getWeatherByCity(city, lang);
            return Response.ok(weather).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.warning("Error fetching weather: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("No se pudo obtener el clima").build();
        }
    }
}
