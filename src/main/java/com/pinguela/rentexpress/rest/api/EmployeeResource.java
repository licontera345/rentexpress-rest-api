package com.pinguela.rentexpress.rest.api;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.RentexpresExceptionMapper;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.dto.ErrorResponseDTO;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpress.rest.api.util.JwtUtil;
import com.pinguela.rentexpress.rest.api.util.RedisCache;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.inject.Inject;

@Path("/employees")
@Tag(name = "Employees", description = "Operations for employee management")
public class EmployeeResource {

    private static final Logger logger = Logger.getLogger(EmployeeResource.class.getName());

    private final EmployeeService employeeService;

    @Inject
    public EmployeeResource(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "findEmployeeById",
        summary = "Find employee by ID",
        description = "Retrieves an employee using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Employee retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmployeeDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid employee identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the employee")
        }
    )
    public Response findById(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee ID is required");
        }
        EmployeeDTO employee = employeeService.findById(id);
        if (employee == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(employee).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "createEmployee",
        summary = "Create employee",
        description = "Creates a new employee with the provided information",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Employee created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmployeeDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid employee data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while creating the employee")
        }
    )
    public Response create(EmployeeDTO employee) throws RentexpresException {
        if (employee == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee data is required");
        }
        boolean created = employeeService.create(employee);
        if (!created) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee could not be created");
        }
        EmployeeDTO createdEmployee = employee.getId() != null ? employeeService.findById(employee.getId()) : employee;
        RedisCache.deleteByPrefix("employees:");
        return Response.status(Status.CREATED).entity(createdEmployee).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "updateEmployee",
        summary = "Update employee",
        description = "Updates an existing employee using the provided information",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Employee updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmployeeDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid employee data supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while updating the employee")
        }
    )
    public Response update(@PathParam("id") Integer id, EmployeeDTO employee) throws RentexpresException {
        if (id == null || employee == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee ID and data are required");
        }
        employee.setId(id);
        boolean updated = employeeService.update(employee);
        if (!updated) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Employee not found or not updated");
        }
        EmployeeDTO updatedEmployee = employeeService.findById(employee.getId());
        RedisCache.deleteByPrefix("employees:");
        return Response.ok(updatedEmployee).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "deleteEmployee",
        summary = "Delete employee",
        description = "Deletes an employee using its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Employee deleted successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid employee identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while deleting the employee")
        }
    )
    public Response delete(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee ID is required");
        }
        EmployeeDTO employeeToDelete = new EmployeeDTO();
        employeeToDelete.setId(id);
        boolean deleted = employeeService.delete(employeeToDelete, id);
        if (!deleted) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Employee not found");
        }
        RedisCache.deleteByPrefix("employees:");
        return ErrorResponseHelper.ok("OK", "Employee deleted successfully");
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN" })
    @Operation(
    operationId = "searchEmployees",
    summary = "Search employees by criteria",
    description = "Retrieves employees that match the provided search criteria",
    responses = {
    @ApiResponse(
    responseCode = "200",
    description = "Employees retrieved successfully",
    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))
    ),
    @ApiResponse(responseCode = "204", description = "No employees found for the provided criteria"),
    @ApiResponse(responseCode = "400", description = "Invalid search criteria supplied"),
    @ApiResponse(responseCode = "500", description = "Unexpected error while searching for employees")
    }
    )
    public Response findByCriteria(
    @QueryParam("employeeId") Integer employeeId,
    @QueryParam("employeeName") String employeeName,
    @QueryParam("roleId") Integer roleId,
    @QueryParam("headquartersId") Integer headquartersId,
    @QueryParam("firstName") String firstName,
    @QueryParam("lastName1") String lastName1,
    @QueryParam("lastName2") String lastName2,
    @QueryParam("email") String email,
    @QueryParam("phone") String phone,
    @QueryParam("activeStatus") Boolean activeStatus,
    @QueryParam("pageNumber") Integer pageNumber,
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("createdAtFrom") java.time.LocalDateTime createdAtFrom,
    @QueryParam("createdAtTo") java.time.LocalDateTime createdAtTo,
    @QueryParam("updatedAtFrom") java.time.LocalDateTime updatedAtFrom,
    @QueryParam("updatedAtTo") java.time.LocalDateTime updatedAtTo
    ) {
    EmployeeCriteria criteria = new EmployeeCriteria();
    criteria.setEmployeeId(employeeId);
    criteria.setEmployeeName(employeeName);
    criteria.setRoleId(roleId);
    criteria.setHeadquartersId(headquartersId);
    criteria.setFirstName(firstName);
    criteria.setLastName1(lastName1);
    criteria.setLastName2(lastName2);
    criteria.setEmail(email);
    criteria.setPhone(phone);
    criteria.setActiveStatus(activeStatus);
    int defaultPageNumber = 1;
    int defaultPageSize = 10;

    criteria.setPageNumber(pageNumber != null && pageNumber > 0 ? pageNumber : defaultPageNumber);
    criteria.setPageSize(pageSize != null && pageSize > 0 ? pageSize : defaultPageSize);

    criteria.setCreatedAtFrom(createdAtFrom);
    criteria.setCreatedAtTo(createdAtTo);
    criteria.setUpdatedAtFrom(updatedAtFrom);
    criteria.setUpdatedAtTo(updatedAtTo);

    try {
        String cacheKey = "employees:"
                + RedisCache.keyPart(employeeId) + ":"
                + RedisCache.keyPart(employeeName) + ":"
                + RedisCache.keyPart(roleId) + ":"
                + RedisCache.keyPart(headquartersId) + ":"
                + RedisCache.keyPart(firstName) + ":"
                + RedisCache.keyPart(lastName1) + ":"
                + RedisCache.keyPart(lastName2) + ":"
                + RedisCache.keyPart(email) + ":"
                + RedisCache.keyPart(phone) + ":"
                + RedisCache.keyPart(activeStatus) + ":"
                + criteria.getPageNumber() + ":"
                + criteria.getPageSize() + ":"
                + RedisCache.keyPart(createdAtFrom) + ":"
                + RedisCache.keyPart(createdAtTo) + ":"
                + RedisCache.keyPart(updatedAtFrom) + ":"
                + RedisCache.keyPart(updatedAtTo);
        Type type = new TypeToken<Results<EmployeeDTO>>(){}.getType();
        Results<EmployeeDTO> cached = RedisCache.getObject(cacheKey, type);
        if (cached != null) {
            return Response.ok(cached).build();
        }
        Results<EmployeeDTO> results = employeeService.findByCriteria(criteria);
        if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
            return Response.status(Status.NO_CONTENT).build();
        }
        RedisCache.setObject(cacheKey, results, RedisCache.DEFAULT_TTL_SECONDS);
        return Response.ok(results).build();
    } catch (RentexpresException e) {
        logger.warning(e.getMessage());
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO("INTERNAL", "An unexpected error occurred"))
                .type(MediaType.APPLICATION_JSON).build();
    }

    }


    @POST
    @Path("/open/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "authenticateEmployee",
        summary = "Authenticate employee",
        description = "Authenticates an employee using username and password and returns token and employee data",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Employee authenticated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON)
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials supplied"),
            @ApiResponse(responseCode = "400", description = "Username or password missing"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while authenticating the employee")
        }
    )
    public Response authenticate(Map<String, String> credentials) throws RentexpresException {
        if (credentials == null || !credentials.containsKey("username") || !credentials.containsKey("password")) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Username and password are required");
        }
        EmployeeDTO employee = employeeService.autenticar(credentials.get("username"), credentials.get("password"));
        if (employee == null) {
            return ErrorResponseHelper.unauthorized("UNAUTHORIZED", "Invalid credentials");
        }
        String token = JwtUtil.generateEmployeeToken(employee.getId());

        // Crear objeto de respuesta con token y empleado
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("employee", employee);

        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/activate")
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(
        operationId = "activateEmployee",
        summary = "Activate employee",
        description = "Activates an employee using its unique identifier",
        responses = {
            @ApiResponse(responseCode = "200", description = "Employee activated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid employee identifier supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while activating the employee")
        }
    )
    public Response activate(@PathParam("id") Integer id) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Employee ID is required");
        }
        boolean activated = employeeService.activate(id);
        if (!activated) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Employee not found");
        }
        return ErrorResponseHelper.ok("OK", "Employee activated successfully");
    }
}
