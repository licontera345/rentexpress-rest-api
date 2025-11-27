package com.pinguela.rentexpress.rest.api.employee;

import java.util.Map;
import java.util.logging.Logger;

import com.pinguela.rentexpress.rest.api.auth.filter.Secured;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.impl.EmployeeServiceImpl;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/employee")
@Secured
@RolesAllowed({"EMPLOYEE"})
@Tag(name = "Employees", description = "Operations for employee management")
public class EmployeeResource {

    private static final Logger logger = Logger.getLogger(EmployeeResource.class.getName());

    private final EmployeeService employeeService;

    public EmployeeResource() {
        this.employeeService = new EmployeeServiceImpl();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID is required").build();
        }
        try {
            EmployeeDTO employee = employeeService.findById(id);
            if (employee == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(employee).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response create(EmployeeDTO employee) {
        if (employee == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee data is required").build();
        }
        try {
            boolean created = employeeService.create(employee);
            if (!created) {
                return Response.status(Status.BAD_REQUEST).entity("Employee could not be created").build();
            }
            EmployeeDTO createdEmployee = employee.getId() != null ? employeeService.findById(employee.getId()) : employee;
            return Response.status(Status.CREATED).entity(createdEmployee).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response update(@PathParam("id") Integer id, EmployeeDTO employee) {
        if (id == null || employee == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID and data are required").build();
        }
        employee.setId(id);
        try {
            boolean updated = employeeService.update(employee);
            if (!updated) {
                return Response.status(Status.NOT_FOUND).entity("Employee not found or not updated").build();
            }
            EmployeeDTO updatedEmployee = employeeService.findById(employee.getId());
            return Response.ok(updatedEmployee).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response delete(@PathParam("id") Integer id, EmployeeDTO employee) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID and data are required").build();
        }
        try {
            EmployeeDTO employeeToDelete = new EmployeeDTO();
            employeeToDelete.setId(id);
            boolean deleted = employeeService.delete(employeeToDelete, id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Employee not found").build();
            }
            return Response.ok().entity("Employee deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
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
        criteria.setPageNumber(pageNumber);
        criteria.setPageSize(pageSize);
        criteria.setCreatedAtFrom(createdAtFrom);
        criteria.setCreatedAtTo(createdAtTo);
        criteria.setUpdatedAtFrom(updatedAtFrom);
        criteria.setUpdatedAtTo(updatedAtTo);
        try {
            Results<EmployeeDTO> results = employeeService.findByCriteria(criteria);
            if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(results).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "authenticateEmployee",
        summary = "Authenticate employee",
        description = "Authenticates an employee using username and password",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Employee authenticated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmployeeDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials supplied"),
            @ApiResponse(responseCode = "400", description = "Username or password missing"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while authenticating the employee")
        }
    )
    public Response authenticate(Map<String, String> credentials) {
        if (credentials == null || !credentials.containsKey("username") || !credentials.containsKey("password")) {
            return Response.status(Status.BAD_REQUEST).entity("Username and password are required").build();
        }
        try {
            EmployeeDTO employee = employeeService.autenticar(credentials.get("username"), credentials.get("password"));
            if (employee == null) {
                return Response.status(Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
            return Response.ok(employee).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/activate")
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
    public Response activate(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID is required").build();
        }
        try {
            boolean activated = employeeService.activate(id);
            if (!activated) {
                return Response.status(Status.NOT_FOUND).entity("Employee not found").build();
            }
            return Response.ok().entity("Employee activated successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
