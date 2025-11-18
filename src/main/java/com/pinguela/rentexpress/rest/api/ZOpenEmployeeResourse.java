package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/employees")
@Tag(name = "Employees", description = "Operations for employee management")
public class ZOpenEmployeeResourse {

    private static final Logger logger = Logger.getLogger(ZOpenEmployeeResourse.class.getName());

    private final EmployeeService employeeService;

    public ZOpenEmployeeResourse() {
        this.employeeService = new EmployeeServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find all employees")
    public Response findAll() {
        try {
            List<EmployeeDTO> employees = employeeService.findAll();
            if (employees == null || employees.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(employees).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
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
    @Operation(summary = "Create employee")
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update employee")
    public Response update(EmployeeDTO employee) {
        if (employee == null || employee.getId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID and data are required").build();
        }
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
    @Operation(summary = "Delete employee")
    public Response delete(@PathParam("id") Integer id, EmployeeDTO employee) {
        if (id == null || employee == null || employee.getId() == null) {
            return Response.status(Status.BAD_REQUEST).entity("Employee ID and data are required").build();
        }
        try {
            boolean deleted = employeeService.delete(employee, id);
            if (!deleted) {
                return Response.status(Status.NOT_FOUND).entity("Employee not found").build();
            }
            return Response.ok().entity("Employee deleted successfully").build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search employees by criteria")
    public Response findByCriteria(EmployeeCriteria criteria) {
        if (criteria == null) {
            return Response.status(Status.BAD_REQUEST).entity("Search criteria is required").build();
        }
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
    @Operation(summary = "Authenticate employee")
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
    @Operation(summary = "Activate employee")
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
