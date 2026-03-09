package com.pinguela.rentexpress.rest.api.inject;

import com.pinguela.rentexpres.service.ConversationService;
import com.pinguela.rentexpres.service.MessageService;
import com.pinguela.rentexpress.rest.api.ws.ChatSocketServiceHolder;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Filtro que inicializa {@link ChatSocketServiceHolder} con los servicios
 * inyectados en la primera petición, para que {@link com.pinguela.rentexpress.rest.api.ws.ChatSocket}
 * use DI en lugar de {@code new XxxServiceImpl()}.
 */
@Provider
public class ChatSocketInitFilter implements ContainerRequestFilter {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @Inject
    public ChatSocketInitFilter(ConversationService conversationService, MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ChatSocketServiceHolder.initOnce(conversationService, messageService);
    }
}
