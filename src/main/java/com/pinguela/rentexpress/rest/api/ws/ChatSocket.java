package com.pinguela.rentexpress.rest.api.ws;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ConversationDTO;
import com.pinguela.rentexpres.model.MessageDTO;
import com.pinguela.rentexpres.service.ConversationService;
import com.pinguela.rentexpres.service.MessageService;
import com.pinguela.rentexpres.service.impl.ConversationServiceImpl;
import com.pinguela.rentexpres.service.impl.MessageServiceImpl;
import com.pinguela.rentexpress.rest.api.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket para chat de soporte: usuarios (clientes) con empleados.
 * URL: ws://host/rentexpress-rest-api/ws/chat/{conversationId}?token=JWT
 * El cliente debe enviar el token en query param para autenticación.
 */
@ServerEndpoint("/ws/chat/{conversationId}")
public class ChatSocket {

    private static final Logger logger = Logger.getLogger(ChatSocket.class.getName());
    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    private final ConversationService conversationService = new ConversationServiceImpl();
    private final MessageService messageService = new MessageServiceImpl();

    /** Guarda en la sesión: "senderType" (USER|EMPLOYEE), "senderId" (Integer as string). */
    private static final String SENDER_TYPE = "senderType";
    private static final String SENDER_ID = "senderId";

    @OnOpen
    public void onOpen(Session session, @PathParam("conversationId") String conversationIdParam) {
        try {
            Integer conversationId = parseConversationId(conversationIdParam);
            if (conversationId == null) {
                closeWithError(session, "conversationId inválido");
                return;
            }
            String token = getToken(session);
            if (token == null || token.isEmpty()) {
                closeWithError(session, "token requerido");
                return;
            }
            String subject;
            try {
                subject = JwtUtil.validateToken(token);
            } catch (Exception e) {
                closeWithError(session, "token inválido o expirado");
                return;
            }
            if (subject == null) {
                closeWithError(session, "token inválido");
                return;
            }
            ConversationDTO conv = conversationService.findById(conversationId);
            if (conv == null) {
                closeWithError(session, "conversación no encontrada");
                return;
            }
            String senderType;
            int senderId;
            if (subject.startsWith("EMPLOYEE:")) {
                senderType = MessageDTO.SENDER_TYPE_EMPLOYEE;
                senderId = Integer.parseInt(subject.substring("EMPLOYEE:".length()));
                // Empleado puede unirse a cualquier conversación (soporte)
            } else {
                int start = subject.startsWith("USER:") ? "USER:".length() : 0;
                senderId = Integer.parseInt(start > 0 ? subject.substring(start) : subject);
                senderType = MessageDTO.SENDER_TYPE_USER;
                if (!conv.getUserId().equals(senderId)) {
                    closeWithError(session, "no autorizado para esta conversación");
                    return;
                }
            }
            session.getUserProperties().put(SENDER_TYPE, senderType);
            session.getUserProperties().put(SENDER_ID, String.valueOf(senderId));
            rooms.computeIfAbsent(conversationIdParam, k -> ConcurrentHashMap.newKeySet()).add(session);
            logger.info("WebSocket joined: conversationId=" + conversationId + " " + senderType + ":" + senderId);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en onOpen", e);
            closeWithError(session, "error al conectar");
        }
    }

    @OnMessage
    public void onMessage(String messageJson, Session session, @PathParam("conversationId") String conversationIdParam) {
        String senderType = (String) session.getUserProperties().get(SENDER_TYPE);
        String senderIdStr = (String) session.getUserProperties().get(SENDER_ID);
        if (senderType == null || senderIdStr == null) {
            return;
        }
        Integer conversationId = parseConversationId(conversationIdParam);
        if (conversationId == null) return;
        String body = extractBody(messageJson);
        if (body == null || body.trim().isEmpty()) return;
        body = body.trim();
        try {
            MessageDTO dto = new MessageDTO();
            dto.setConversationId(conversationId);
            dto.setSenderType(senderType);
            dto.setSenderId(Integer.parseInt(senderIdStr));
            dto.setBody(body);
            messageService.create(dto);
            MessageDTO created = messageService.findById(dto.getMessageId());
            String payload = created != null ? toJson(created) : messageJson;
            Set<Session> roomSessions = rooms.get(conversationIdParam);
            if (roomSessions != null) {
                for (Session s : roomSessions) {
                    if (s.isOpen()) {
                        s.getAsyncRemote().sendText(payload);
                    }
                }
            }
        } catch (RentexpresException e) {
            logger.log(Level.WARNING, "Error persistiendo mensaje", e);
            sendError(session, "Error al guardar mensaje");
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("conversationId") String conversationId) {
        Set<Session> set = rooms.get(conversationId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                rooms.remove(conversationId);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.log(Level.WARNING, "WebSocket error", error);
    }

    private static Integer parseConversationId(String param) {
        if (param == null || param.isEmpty()) return null;
        try {
            return Integer.valueOf(param);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static String getToken(Session session) {
        try {
            Map<String, List<String>> params = session.getRequestParameterMap();
            if (params != null && params.containsKey("token")) {
                List<String> values = params.get("token");
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static String extractBody(String messageJson) {
        if (messageJson == null) return null;
        try {
            JsonObject o = gson.fromJson(messageJson, JsonObject.class);
            if (o.has("body")) return o.get("body").getAsString();
            if (o.has("message")) return o.get("message").getAsString();
            return messageJson;
        } catch (Exception e) {
            return messageJson;
        }
    }

    private static String toJson(MessageDTO m) {
        JsonObject o = new JsonObject();
        o.addProperty("messageId", m.getMessageId());
        o.addProperty("conversationId", m.getConversationId());
        o.addProperty("senderType", m.getSenderType());
        o.addProperty("senderId", m.getSenderId());
        o.addProperty("body", m.getBody());
        o.addProperty("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
        return gson.toJson(o);
    }

    private static void closeWithError(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("{\"error\":\"" + message + "\"}");
                session.close();
            }
        } catch (IOException e) {
            logger.fine("Error closing session: " + e.getMessage());
        }
    }

    private static void sendError(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("{\"error\":\"" + message + "\"}");
            }
        } catch (IOException e) {
            logger.fine("Error sending error message: " + e.getMessage());
        }
    }
}
