package com.pinguela.rentexpress.rest.api.chat;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ConversationDTO;
import com.pinguela.rentexpres.model.MessageDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.service.ConversationService;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.MessageService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpress.rest.api.security.Secured;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;

import jakarta.inject.Inject;

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

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;
    private final EmployeeService employeeService;

    @Inject
    public ConversationResource(ConversationService conversationService, MessageService messageService,
            UserService userService, EmployeeService employeeService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT" })
    @Operation(summary = "Crear o obtener conversación", description = "Cliente crea una nueva conversación con un empleado o obtiene la existente. Body: { \"employeeId\": number }.")
    public Response create(@Context SecurityContext securityContext, java.util.Map<String, Object> body) throws RentexpresException {
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        Integer employeeId = null;
        if (body != null && body.get("employeeId") != null) {
            try {
                employeeId = body.get("employeeId") instanceof Number
                    ? ((Number) body.get("employeeId")).intValue()
                    : Integer.parseInt(body.get("employeeId").toString());
            } catch (NumberFormatException e) {
                return ErrorResponseHelper.badRequest("BAD_REQUEST", "employeeId inválido");
            }
        }
        try {
            Integer userId = Integer.valueOf(principalId);
            ConversationDTO result;
            if (employeeId != null) {
                result = conversationService.findOrCreateByUserAndEmployee(userId, employeeId);
            } else {
                ConversationDTO dto = new ConversationDTO();
                dto.setUserId(userId);
                dto.setStatus("OPEN");
                boolean ok = conversationService.create(dto);
                if (!ok) return Response.status(Status.BAD_REQUEST).build();
                result = conversationService.findById(dto.getConversationId());
            }
            if (result == null) return Response.status(Status.BAD_REQUEST).build();
            return Response.status(Status.CREATED).entity(result).build();
        } catch (NumberFormatException e) {
            return Response.status(Status.FORBIDDEN).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Mis conversaciones", description = "Lista conversaciones del usuario o del empleado.")
    public Response listMine(@Context SecurityContext securityContext) throws RentexpresException {
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
        }
    }

    @GET
    @Path("support/employees-by-headquarters")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Empleados por sede", description = "Lista empleados de una sede para iniciar chat (cliente elige empleado).")
    public Response listEmployeesByHeadquarters(@QueryParam("headquartersId") Integer headquartersId, @Context SecurityContext securityContext) throws RentexpresException {
        if (headquartersId == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "headquartersId requerido");
        }
        EmployeeCriteria criteria = new EmployeeCriteria();
        criteria.setHeadquartersId(headquartersId);
        criteria.setActiveStatus(true);
        criteria.setPageNumber(1);
        criteria.setPageSize(200);
        Results<EmployeeDTO> results = employeeService.findByCriteria(criteria);
        List<EmployeeDTO> list = (results != null && results.getResults() != null) ? results.getResults() : List.of();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Obtener conversación por ID")
    public Response getById(@PathParam("id") Integer id, @Context SecurityContext securityContext) throws RentexpresException {
        if (id == null) return Response.status(Status.BAD_REQUEST).build();
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
        ConversationDTO dto = conversationService.findById(id);
        if (dto == null) return Response.status(Status.NOT_FOUND).build();
        Integer pid = Integer.valueOf(principalId);
        if (!dto.getUserId().equals(pid) && (dto.getEmployeeId() == null || !dto.getEmployeeId().equals(pid))) {
            if (!isEmployee(securityContext)) {
                return Response.status(Status.FORBIDDEN).build();
            }
        }
        return Response.ok(dto).build();
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
            @Context SecurityContext securityContext) throws RentexpresException {
        if (conversationId == null) return Response.status(Status.BAD_REQUEST).build();
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
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
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Actualizar conversación", description = "Actualiza estado (OPEN, IN_PROGRESS, CLOSED). Solo empleados/admin pueden poner CLOSED. Body: { \"status\": \"CLOSED\" }.")
    public Response update(@PathParam("id") Integer id, java.util.Map<String, Object> body, @Context SecurityContext securityContext) throws RentexpresException {
        if (id == null) return Response.status(Status.BAD_REQUEST).build();
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
        ConversationDTO dto = conversationService.findById(id);
        if (dto == null) return Response.status(Status.NOT_FOUND).build();
        Integer pid = Integer.valueOf(principalId);
        boolean isOwner = dto.getUserId().equals(pid) || (dto.getEmployeeId() != null && dto.getEmployeeId().equals(pid));
        if (!isOwner && !isEmployee(securityContext)) return Response.status(Status.FORBIDDEN).build();
        String status = body != null && body.get("status") != null ? body.get("status").toString().trim() : null;
        if (status != null && !status.isEmpty()) {
            if ("CLOSED".equals(status) && !isEmployee(securityContext)) return Response.status(Status.FORBIDDEN).build();
            if ("OPEN".equals(status) || "IN_PROGRESS".equals(status) || "CLOSED".equals(status)) {
                dto.setStatus(status);
            }
        }
        boolean ok = conversationService.update(dto);
        if (!ok) return Response.status(Status.BAD_REQUEST).build();
        return Response.ok(conversationService.findById(id)).build();
    }

    @PUT
    @Path("/{id}/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Asignar empleado a conversación")
    public Response assignEmployee(@PathParam("id") Integer id, java.util.Map<String, Integer> body, @Context SecurityContext securityContext) throws RentexpresException {
        if (id == null) return Response.status(Status.BAD_REQUEST).build();
        Integer employeeId = body != null ? body.get("employeeId") : null;
        if (employeeId == null) return ErrorResponseHelper.badRequest("BAD_REQUEST", "employeeId required");
        ConversationDTO dto = conversationService.findById(id);
        if (dto == null) return Response.status(Status.NOT_FOUND).build();
        dto.setEmployeeId(employeeId);
        dto.setStatus("IN_PROGRESS");
        boolean ok = conversationService.update(dto);
        if (!ok) return Response.status(Status.BAD_REQUEST).build();
        return Response.ok(conversationService.findById(id)).build();
    }

    @PUT
    @Path("/{id}/read")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "CLIENT", "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Marcar conversación como leída")
    public Response markAsRead(@PathParam("id") Integer id, @Context SecurityContext securityContext) throws RentexpresException {
        if (id == null) return Response.status(Status.BAD_REQUEST).build();
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
        ConversationDTO conv = conversationService.findById(id);
        if (conv == null) return Response.status(Status.NOT_FOUND).build();
        Integer pid = Integer.valueOf(principalId);
        if (conv.getUserId().equals(pid)) {
            conversationService.markAsReadForUser(id);
        } else if (conv.getEmployeeId() != null && conv.getEmployeeId().equals(pid)) {
            conversationService.markAsReadForEmployee(id);
        } else if (!isEmployee(securityContext)) {
            return Response.status(Status.FORBIDDEN).build();
        } else {
            conversationService.markAsReadForEmployee(id);
        }
        return Response.ok(conversationService.findById(id)).build();
    }

    @GET
    @Path("/find-by-phone")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @RolesAllowed({ "ADMIN", "EMPLOYEE" })
    @Operation(summary = "Buscar usuario por teléfono y obtener/crear conversación", description = "Para empleados: busca un cliente por número de teléfono y devuelve el usuario y la conversación con él (si existe).")
    public Response findByPhone(@QueryParam("phone") String phone, @Context SecurityContext securityContext) throws RentexpresException {
        if (phone == null || phone.trim().isEmpty()) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "phone requerido");
        }
        String principalId = getPrincipalId(securityContext);
        if (principalId == null) return Response.status(Status.UNAUTHORIZED).build();
        Integer employeeId = Integer.valueOf(principalId);
        UserCriteria criteria = new UserCriteria();
        criteria.setPhone(phone.trim());
        criteria.setPageNumber(1);
        criteria.setPageSize(1);
        Results<UserDTO> results = userService.findByCriteria(criteria);
        if (results == null || results.getResults() == null || results.getResults().isEmpty()) {
            return Response.ok(java.util.Map.of("user", (Object) null, "conversation", (Object) null)).build();
        }
        UserDTO user = results.getResults().get(0);
        if (user.getUserId() == null) return Response.ok(java.util.Map.of("user", (Object) null, "conversation", (Object) null)).build();
        user.setPassword(null);
        ConversationDTO conv = conversationService.findOrCreateByUserAndEmployee(user.getUserId(), employeeId);
        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("user", user);
        out.put("conversation", conv);
        return Response.ok(out).build();
    }

    private static String getPrincipalId(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        return sc.getUserPrincipal().getName();
    }

    private static boolean isEmployee(SecurityContext sc) {
        return sc != null && (sc.isUserInRole("ADMIN") || sc.isUserInRole("EMPLOYEE"));
    }
}
