package com.pinguela.rentexpress.rest.api;

import java.util.List;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RoleDTO;
import com.pinguela.rentexpres.service.RoleService;
import com.pinguela.rentexpres.service.impl.RoleServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/roles")
@Tag(name = "Roles", description = "Operations for role reference data")
public class ZOpenRoleResourse {

    private static final Logger logger = Logger.getLogger(ZOpenRoleResourse.class.getName());

    private final RoleService roleService;

    public ZOpenRoleResourse() {
        this.roleService = new RoleServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find all roles")
    public Response findAll() {
        try {
            List<RoleDTO> roles = roleService.findAll();
            if (roles == null || roles.isEmpty()) {
                return Response.status(Status.NO_CONTENT).build();
            }
            return Response.ok(roles).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find role by ID")
    public Response findById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).entity("Role ID is required").build();
        }
        try {
            RoleDTO role = roleService.findById(id);
            if (role == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(role).build();
        } catch (RentexpresException e) {
            logger.warning(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
