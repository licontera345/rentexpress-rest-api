package com.pinguela.rentexpress.rest.api;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ConversationDTO;
import com.pinguela.rentexpres.model.MessageDTO;
import com.pinguela.rentexpres.service.ConversationService;
import com.pinguela.rentexpres.service.MessageService;
import com.pinguela.rentexpres.service.impl.ConversationServiceImpl;
import com.pinguela.rentexpres.service.impl.MessageServiceImpl;
import com.pinguela.rentexpress.rest.api.security.Secured;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

@Path("/conversations")
@Tag(name = "Conversations", description = "Chat de soporte usuario–empleado")
public class ConversationResource {

    private final ConversationService conversationService = new ConversationServiceImpl();
    private final MessageService messageService = new MessageServiceImpl();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT" })
    @Operation(summary = "Crear conversación", description = "Un usuario (cliente) crea una nueva conversación de soporte.")
    public Response create(@Context SecurityContext securityContext) {
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        try {
            Integer userId = Integer.valueOf(principalId);
            ConversationDTO dto = new ConversationDTO();
            dto.setUserId(userId);
            dto.setStatus("OPEN");
            boolean ok = conversationService.create(dto);
            if (!ok) {
                return Response.status(Status.BAD_REQUEST).build();
            }
            ConversationDTO created = conversationService.findById(dto.getConversationId());
            return Response.status(Status.CREATED).entity(created).build();
        } catch (NumberFormatException e) {
            return Response.status(Status.FORBIDDEN).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Mis conversaciones", description = "Lista conversaciones del usuario o del empleado.")
    public Response listMine(@Context SecurityContext securityContext) {
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        try {
            Integer id = Integer.valueOf(principalId);
            List<ConversationDTO> list;
            if (isEmployee(securityContext)) {
                list = conversationService.findByEmployeeId(id);
            } else {
                list = conversationService.findByUserId(id);
            }
            return Response.ok(list).build();
        } catch (NumberFormatException e) {
            return Response.status(Status.FORBIDDEN).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Obtener conversación por ID")
    public Response getById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        if (id == null) return Response.status(Status.BAD_REQUEST).build();
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
        try {
            ConversationDTO dto = conversationService.findById(id);
            if (dto == null) return Response.status(Status.NOT_FOUND).build();
            Integer pid = Integer.valueOf(principalId);
            if (!dto.getUserId().equals(pid) && (dto.getEmployeeId() == null || !dto.getEmployeeId().equals(pid))) {
                if (!isEmployee(securityContext)) {
                    return Response.status(Status.FORBIDDEN).build();
                }
            }
            return Response.ok(dto).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @GET
    @Path("/{id}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Mensajes de una conversación")
    public Response getMessages(
            @PathParam("id") Integer conversationId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @Context SecurityContext securityContext) {
        if (conversationId == null) return Response.status(Status.BAD_REQUEST).build();
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
        try {
            ConversationDTO conv = conversationService.findById(conversationId);
            if (conv == null) return Response.status(Status.NOT_FOUND).build();
            Integer pid = Integer.valueOf(principalId);
            if (!conv.getUserId().equals(pid) && (conv.getEmployeeId() == null || !conv.getEmployeeId().equals(pid))) {
                if (!isEmployee(securityContext)) {
                    return Response.status(Status.FORBIDDEN).build();
                }
            }
            List<MessageDTO> messages = messageService.findByConversationId(conversationId, limit, offset);
            return Response.ok(messages).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    @PUT
    @Path("/{id}/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Asignar empleado a conversación")
    public Response assignEmployee(@PathParam("id") Integer id, java.util.Map<String, Integer> body, @Context SecurityContext securityContext) {
        if (id == null) return Response.status(Status.BAD_REQUEST).build();
        Integer employeeId = body != null ? body.get("employeeId") : null;
        if (employeeId == null) return Response.status(Status.BAD_REQUEST).entity("employeeId required").build();
        try {
            ConversationDTO dto = conversationService.findById(id);
            if (dto == null) return Response.status(Status.NOT_FOUND).build();
            dto.setEmployeeId(employeeId);
            dto.setStatus("IN_PROGRESS");
            boolean ok = conversationService.update(dto);
            if (!ok) return Response.status(Status.BAD_REQUEST).build();
            return Response.ok(conversationService.findById(id)).build();
        } catch (RentexpresException e) {
            return RentexpresExceptionMapper.toResponse(e);
        }
    }

    private static String getPrincipalId(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        return sc.getUserPrincipal().getName();
    }

    private static boolean isEmployee(SecurityContext sc) {
        return sc != null && (sc.isUserInRole("ADMIN") || sc.isUserInRole("EMPLOYEE"));
    }
}
