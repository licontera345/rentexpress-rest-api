package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleCategoryDTO;
import com.pinguela.rentexpres.model.VehicleCriteria;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.model.VehicleStatusDTO;
import com.pinguela.rentexpres.service.VehicleCategoryService;
import com.pinguela.rentexpres.service.VehicleService;
import com.pinguela.rentexpres.service.VehicleStatusService;
import com.pinguela.rentexpres.service.impl.VehicleCategoryServiceImpl;
import com.pinguela.rentexpres.service.impl.VehicleServiceImpl;
import com.pinguela.rentexpres.service.impl.VehicleStatusServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/vehicles")
@Tag(name = "Vehicles", description = "Operations for vehicle management")
public class ZOpenVehicleResourse {

    private static final Logger logger = Logger.getLogger(ZOpenVehicleResourse.class.getName());

    private final VehicleService vehicleService;
    private final VehicleCategoryService vehicleCategoryService;
    private final VehicleStatusService vehicleStatusService;
    public ZOpenVehicleResourse() {
        this.vehicleService = new VehicleServiceImpl();
        this.vehicleCategoryService = new VehicleCategoryServiceImpl();
        this.vehicleStatusService = new VehicleStatusServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAllVehicles",
        summary = "Find all vehicles",
        description = "Retrieves all vehicles available in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vehicles retrieved successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = VehicleDTO[].class)
                )
            ),
            @ApiResponse(
                responseCode = "204",
                description = "No vehicles found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while retrieving vehicles"
            )
        }
    )
    public Response findAll() {
        try {
            List<VehicleDTO> vehicles = vehicleService.findAll();
            if (vehicles == null || vehicles.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(vehicles).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
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
                responseCode = "400",
                description = "Invalid vehicle identifier supplied"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Unexpected error while retrieving the vehicle"
            )
        }
    )
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }
        try {
            VehicleDTO vehicle = vehicleService.findById(id);
            if (vehicle == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(vehicle).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response create(VehicleDTO vehicle) {
        if (vehicle == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle data is required").build();
        }
        try {
            boolean created = vehicleService.create(vehicle);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Vehicle could not be created").build();
            }
            VehicleDTO createdVehicle = vehicle.getVehicleId() != null ? vehicleService.findById(vehicle.getVehicleId()) : vehicle;
            return Response.status(Status.CREATED).entity(createdVehicle).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response update(VehicleDTO vehicle) {
        if (vehicle == null || vehicle.getVehicleId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID and data are required").build();
        }
        try {
            boolean updated = vehicleService.update(vehicle);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Vehicle not found or not updated").build();
            }
            VehicleDTO updatedVehicle = vehicleService.findById(vehicle.getVehicleId());
            return Response.ok(updatedVehicle).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
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
    public Response delete(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Vehicle ID is required").build();
        }
        try {
            boolean deleted = vehicleService.delete(id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Vehicle not found").build();
            }
            return Response.ok().entity("Vehicle deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findVehiclesByCriteria",
        summary = "Find vehicles by criteria",
        description = "Searches vehicles using the provided criteria",
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
    public Response findByCriteria(VehicleCriteria criteria) {
        if (criteria == null) {
            return Response.status(Status.BAD_REQUEST).entity("Search criteria is required").build();
        }
        try {
            Results<VehicleDTO> results = vehicleService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(results).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Vehicle Categories", description = "Operations for vehicle category management")
    @Operation(
        operationId = "findAllVehicleCategories",
        summary = "Find all vehicle categories",
        description = "Retrieves all vehicle categories for the provided locale",
        responses = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully", 
            	content = @Content(mediaType = MediaType.APPLICATION_JSON,
            	schema = @Schema(implementation = VehicleCategoryDTO[].class)
            	)
            ),
            @ApiResponse(
            	responseCode = "204",
                description = "No categories found"),
            
            @ApiResponse(
            	responseCode = "400",
                description = "isoCode is required"),
            
            @ApiResponse(
            	responseCode = "500",
                description = "Unexpected error while retrieving categories")
        } 
    )
    public Response findAllCategories(@QueryParam("isoCode") String isoCode) {
        if (isoCode == null || isoCode.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode is required").build();
        }
        try {
            List<VehicleCategoryDTO> categories = vehicleCategoryService.findAll(normalizeIsoCode(isoCode));
            if (categories == null || categories.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(categories).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/categories/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Vehicle Categories", description = "Operations for vehicle category management")
    @Operation(
        operationId = "findVehicleCategoryById",
        summary = "Find vehicle category by ID",
        description = "Retrieves a specific vehicle category for the provided locale",
        responses = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
            		content = @Content(mediaType = MediaType.APPLICATION_JSON,
            		schema = @Schema(implementation = VehicleCategoryDTO.class)
            		)
            ),
            @ApiResponse(
            	responseCode = "404", 
            	description = "Category not found"),
            
            @ApiResponse(
            	responseCode = "400",
            	
            	description = "Invalid parameters supplied"),
            
            @ApiResponse(
            	responseCode = "500",
            	description = "Unexpected error while retrieving the category")
        }
    )
    public Response findCategoryById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) {
        if (id == null || isoCode == null || isoCode.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Category id and isoCode are required").build();
        }
        try {
            VehicleCategoryDTO dto = vehicleCategoryService.findById(id, normalizeIsoCode(isoCode));
            if (dto == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(dto).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/statuses")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Vehicle Statuses", description = "Operations for vehicle status management")
    @Operation(
        operationId = "findAllVehicleStatuses",
        summary = "Find all vehicle statuses",
        description = "Retrieves all vehicle statuses for the provided locale",
        responses = {
            @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully",
            		content = @Content(mediaType = MediaType.APPLICATION_JSON,
            		schema = @Schema(implementation = VehicleStatusDTO[].class)
            		)
            ),          
            @ApiResponse(
            	responseCode = "204", 
            	description = "No statuses found"),
            
            @ApiResponse(
            	responseCode = "400",
            	description = "isoCode is required"),
            
            @ApiResponse(
            	responseCode = "500",
                description = "Unexpected error while retrieving statuses")
        }
    )
    public Response findAllStatuses(@QueryParam("isoCode") String isoCode) {
        if (isoCode == null || isoCode.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("isoCode is required").build();
        }
        try {
            List<VehicleStatusDTO> statuses = vehicleStatusService.findAll(normalizeIsoCode(isoCode));
            if (statuses == null || statuses.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(statuses).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/statuses/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Vehicle Statuses", description = "Operations for vehicle status management")
    @Operation(
        operationId = "findVehicleStatusById",
        summary = "Find vehicle status by ID",
        description = "Retrieves a specific vehicle status for the provided locale",
        responses = {
            @ApiResponse(responseCode = "200",
            	description = "Status retrieved successfully",
            	content = @Content(mediaType = MediaType.APPLICATION_JSON,
            	schema = @Schema(implementation = VehicleStatusDTO.class)
            	)
            ),
            @ApiResponse(
            	responseCode = "404",
                description = "Status not found"),
            
            @ApiResponse(
            	responseCode = "400",
            	description = "Invalid parameters supplied"),
            
            @ApiResponse(
            	responseCode = "500", 
            	description = "Unexpected error while retrieving the status")
        }
    )
    public Response findStatusById(@PathParam("id") Integer id, @QueryParam("isoCode") String isoCode) {
        if (id == null || isoCode == null || isoCode.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Status id and isoCode are required").build();
        }
        try {
            VehicleStatusDTO dto = vehicleStatusService.findById(id, normalizeIsoCode(isoCode));
            if (dto == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(dto).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private String normalizeIsoCode(String isoCode) {
        return isoCode == null ? null : isoCode.trim().toUpperCase();
    }

  }
