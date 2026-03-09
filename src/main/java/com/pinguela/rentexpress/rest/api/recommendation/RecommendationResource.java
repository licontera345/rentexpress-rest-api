package com.pinguela.rentexpress.rest.api.recommendation;

import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;
import com.pinguela.rentexpress.rest.api.dto.RecommendationRequestDTO;
import com.pinguela.rentexpress.rest.api.dto.RecommendationResponseDTO;
import com.pinguela.rentexpress.rest.api.service.GroqService;
import com.pinguela.rentexpress.rest.api.adapter.GroqApiException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.inject.Inject;

@Path("/open/recommendations")
@Tag(name = "Recommendations", description = "AI-powered vehicle recommendations")
public class RecommendationResource {

    private static final Logger logger = Logger.getLogger(RecommendationResource.class.getName());

    private final GroqService groqService;

    @Inject
    public RecommendationResource(GroqService groqService) {
        this.groqService = groqService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "getVehicleRecommendations",
        summary = "Get AI vehicle recommendations",
        description = "Returns vehicle recommendations based on user preferences and available vehicles",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Recommendations generated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RecommendationResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request - missing preferences or vehicles"),
            @ApiResponse(responseCode = "503", description = "Recommendation service temporarily unavailable"),
            @ApiResponse(responseCode = "500", description = "Could not generate recommendations")
        }
    )
    public Response getRecommendations(RecommendationRequestDTO request) {
        try {
            if (request == null) {
                return Response.status(Status.BAD_REQUEST)
                        .entity(new ErrorResponseDTO("BAD_REQUEST", "El cuerpo de la solicitud es obligatorio"))
                        .type(MediaType.APPLICATION_JSON).build();
            }

            RecommendationResponseDTO result = groqService.recommend(request);
            return Response.ok(result).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("BAD_REQUEST", "Invalid request"))
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IllegalStateException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE)
                    .entity(new ErrorResponseDTO("SERVICE_UNAVAILABLE", "Servicio no disponible"))
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (GroqApiException e) {
            throw e;
        } catch (Exception e) {
            logger.warning("Error generating recommendations: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("INTERNAL", "No se pudo generar la recomendación"))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }
}
