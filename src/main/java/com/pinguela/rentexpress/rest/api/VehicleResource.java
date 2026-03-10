package com.pinguela.rentexpress.rest.api;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Logger;

import com.google.gson.reflect.TypeToken;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleCriteria;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.service.VehicleService;
import com.pinguela.rentexpress.rest.api.base.BaseCrudResource;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.dto.FinalizarMantenimiento;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpress.rest.api.util.RedisCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.inject.Inject;

@Path("/vehicles")
@Tag(name = "Vehicles", description = "Operations for vehicle management")
public class VehicleResource extends BaseCrudResource<VehicleDTO, VehicleService> {

    private static final Logger logger = Logger.getLogger(VehicleResource.class.getName());

    private final VehicleService vehicleService;

    @Inject
    public VehicleResource(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }
    
    @POST
    @Path("/finMantenimiento")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "notificarFinMantenimiento",
        summary = "Notificar fin de mantenimiento",
        description = "Registra una descripción del trabajo realizado sin cambiar el estado del vehículo, para revisión manual del empleado.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Notificación registrada correctamente"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "La matrícula es obligatoria"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno al procesar la notificación"
            )
        }
    )
    public Response notificarFinMantenimiento(FinalizarMantenimiento dto) throws RentexpresException {
        if (dto == null || dto.getMatricula() == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "La matrícula es obligatoria");
        }

        vehicleService.finishMaintenance(dto.getMatricula(), dto.getDescripcion());
        RedisCache.deleteByPrefix("vehicles:");
        return ErrorResponseHelper.ok("OK", "Aviso de mantenimiento registrado correctamente");
    }
    @GET
    @Path("/open/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findVehicleById",
        summary = "Find vehicle by ID",
        description = "Retrieves a vehicle using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle retrieved successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = VehicleDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Vehicle not found"
            ),
            @ApiResponse(
                responseCode = "304",
                description = "Not Modified (cache válido)"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid vehicle identifier supplied"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while retrieving the vehicle"
            )
        }
    )
    public Response findById(@PathParam("id") Integer id, @jakarta.ws.rs.core.Context Request request) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Vehicle ID is required");
        }
        VehicleDTO vehicle = vehicleService.findById(id);
        if (vehicle == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String etagValue = computeETag(vehicle);
        EntityTag etag = new EntityTag(etagValue);
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.build();
        }
        return Response.ok(vehicle).tag(etag).build();
    }

    /**
     * Computes a weak ETag from vehicle identifier and updated timestamp so that
     * cache revalidation can return 304 Not Modified when content is unchanged.
     */
    private static String computeETag(VehicleDTO vehicle) {
        StringBuilder sb = new StringBuilder();
        sb.append(vehicle.getVehicleId() != null ? vehicle.getVehicleId() : "");
        if (vehicle.getUpdatedAt() != null) {
            sb.append(":").append(vehicle.getUpdatedAt().toString());
        } else {
            sb.append(":").append(System.currentTimeMillis());
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return "W/\"" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            return "W/\"" + Integer.toHexString(sb.toString().hashCode()) + "\"";
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "createVehicle",
        summary = "Create a new vehicle",
        description = "Creates a new vehicle and returns the created entity",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Vehicle created successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = VehicleDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or incomplete vehicle data supplied"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while creating the vehicle"
            )
        }
    )
    public Response create(VehicleDTO vehicle) throws RentexpresException {
        return doCreate(vehicle, vehicleService, VehicleDTO::getVehicleId, "vehicles:");
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "updateVehicle",
        summary = "Update an existing vehicle",
        description = "Updates a vehicle and returns the updated entity",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle updated successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = VehicleDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid vehicle data supplied"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Vehicle not found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while updating the vehicle"
            )
        }
    )
    public Response update(@PathParam("id") Integer id, VehicleDTO vehicle) throws RentexpresException {
        return doUpdate(id, vehicle, vehicleService, VehicleDTO::setVehicleId, "vehicles:");
    }

    @DELETE
    @Path("/{id}")
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "deleteVehicle",
        summary = "Delete a vehicle",
        description = "Deletes a vehicle using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicle deleted successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Vehicle not found"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid vehicle identifier supplied"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while deleting the vehicle"
            )
        }
    )
    public Response delete(@PathParam("id") Integer id) throws RentexpresException {
        return doDelete(id, vehicleService, "vehicles:");
    }

    @GET
    @Path("/open/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findVehiclesByCriteria",
        summary = "Find vehicles by criteria",
        description = "Searches vehicles using the provided criteria. Optional sortBy (year, price, mileage, brand, model) and order (asc, desc).",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Search executed successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Results.class)
                )
            ),
            @ApiResponse(
                responseCode = "204",
                description = "No vehicles found matching the criteria"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid search criteria supplied"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while searching vehicles"
            )
        }
    )
    public Response findByCriteria(
        @QueryParam("vehicleId") Integer vehicleId,
        @QueryParam("vehicleStatusId") Integer vehicleStatusId,
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("currentHeadquartersId") Integer currentHeadquartersId,
        @QueryParam("brand") String brand,
        @QueryParam("model") String model,
        @QueryParam("licensePlate") String licensePlate,
        @QueryParam("vinNumber") String vinNumber,
        @QueryParam("manufactureYearFrom") Integer manufactureYearFrom,
        @QueryParam("manufactureYearTo") Integer manufactureYearTo,
        @QueryParam("dailyPriceMin") java.math.BigDecimal dailyPriceMin,
        @QueryParam("dailyPriceMax") java.math.BigDecimal dailyPriceMax,
        @QueryParam("currentMileageMin") Integer currentMileageMin,
        @QueryParam("currentMileageMax") Integer currentMileageMax,
        @QueryParam("activeStatus") Boolean activeStatus,
        @QueryParam("pageNumber") Integer pageNumber,
        @QueryParam("pageSize") Integer pageSize,
        @QueryParam("sortBy") String sortBy,
        @QueryParam("order") String order,
        @QueryParam("createdAtFrom") LocalDateTime createdAtFrom,
        @QueryParam("createdAtTo") LocalDateTime createdAtTo,
        @QueryParam("updatedAtFrom") LocalDateTime updatedAtFrom,
        @QueryParam("updatedAtTo") LocalDateTime updatedAtTo,
        @QueryParam("description") String description,
        @QueryParam("availableFrom") String availableFromStr,
        @QueryParam("availableTo") String availableToStr
    ) throws RentexpresException {
        VehicleCriteria criteria = new VehicleCriteria();
        criteria.setVehicleId(vehicleId);
        criteria.setVehicleStatusId(vehicleStatusId);
        criteria.setCategoryId(categoryId);
        criteria.setCurrentHeadquartersId(currentHeadquartersId);
        criteria.setBrand(brand);
        criteria.setModel(model);
        criteria.setLicensePlate(licensePlate);
        criteria.setVinNumber(vinNumber);
        criteria.setManufactureYearFrom(manufactureYearFrom);
        criteria.setManufactureYearTo(manufactureYearTo);
        criteria.setDailyPriceMin(dailyPriceMin);
        criteria.setDailyPriceMax(dailyPriceMax);
        criteria.setCurrentMileageMin(currentMileageMin);
        criteria.setCurrentMileageMax(currentMileageMax);
        criteria.setActiveStatus(activeStatus);
        // Defaults de paginación en capa API; el middleware recibe criterios ya consistentes.
        criteria.setPageNumber((pageNumber != null && pageNumber >= 1) ? pageNumber : 1);
        criteria.setPageSize((pageSize != null && pageSize >= 1) ? pageSize : 10);
        criteria.setSortBy(sortBy);
        criteria.setOrderBy(order);
        criteria.setCreatedAtFrom(createdAtFrom);
        criteria.setCreatedAtTo(createdAtTo);
        criteria.setUpdatedAtFrom(updatedAtFrom);
        criteria.setUpdatedAtTo(updatedAtTo);
        criteria.setDescription(description);
        LocalDateTime availableFrom = parseDateTime(availableFromStr);
        LocalDateTime availableTo = parseDateTime(availableToStr);
        if (availableFrom != null && availableTo != null) {
            criteria.setAvailableFrom(availableFrom);
            criteria.setAvailableTo(availableTo);

        }
        String cacheKey = "vehicles:"
                    + RedisCache.keyPart(vehicleId) + ":"
                    + RedisCache.keyPart(vehicleStatusId) + ":"
                    + RedisCache.keyPart(categoryId) + ":"
                    + RedisCache.keyPart(currentHeadquartersId) + ":"
                    + RedisCache.keyPart(brand) + ":"
                    + RedisCache.keyPart(model) + ":"
                    + RedisCache.keyPart(licensePlate) + ":"
                    + RedisCache.keyPart(vinNumber) + ":"
                    + RedisCache.keyPart(manufactureYearFrom) + ":"
                    + RedisCache.keyPart(manufactureYearTo) + ":"
                    + RedisCache.keyPart(dailyPriceMin) + ":"
                    + RedisCache.keyPart(dailyPriceMax) + ":"
                    + RedisCache.keyPart(currentMileageMin) + ":"
                    + RedisCache.keyPart(currentMileageMax) + ":"
                    + RedisCache.keyPart(activeStatus) + ":"
                    + criteria.getPageNumber() + ":"
                    + criteria.getPageSize() + ":"
                    + RedisCache.keyPart(sortBy) + ":"
                    + RedisCache.keyPart(order) + ":"
                    + RedisCache.keyPart(createdAtFrom) + ":"
                    + RedisCache.keyPart(createdAtTo) + ":"
                    + RedisCache.keyPart(updatedAtFrom) + ":"
                    + RedisCache.keyPart(updatedAtTo) + ":"
                    + RedisCache.keyPart(description) + ":"
                    + RedisCache.keyPart(availableFromStr) + ":"
                    + RedisCache.keyPart(availableToStr);
            Type type = new TypeToken<Results<VehicleDTO>>(){}.getType();
            Results<VehicleDTO> cached = RedisCache.getObject(cacheKey, type);
            if (cached != null) {
                logger.info("Cache HIT vehicles");
                return Response.ok(cached).build();
            }
            logger.info("Cache MISS vehicles");
            Results<VehicleDTO> results = vehicleService.findByCriteria(criteria);
            RedisCache.setObject(cacheKey, results, RedisCache.DEFAULT_TTL_SECONDS);
            return Response.ok(results).build();
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

  }
