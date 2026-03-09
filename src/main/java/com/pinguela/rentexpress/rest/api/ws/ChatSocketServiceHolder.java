package com.pinguela.rentexpress.rest.api.ws;

import com.pinguela.rentexpres.service.ConversationService;
import com.pinguela.rentexpres.service.MessageService;

/**
 * Holder de servicios para {@link ChatSocket}. Los WebSocket no son gestionados
 * por Jersey/HK2, por lo que se inicializan desde un filtro que recibe las
 * dependencias inyectadas y las registra aquí.
 */
public final class ChatSocketServiceHolder {

    private static volatile ConversationService conversationService;
    private static volatile MessageService messageService;
    private static volatile boolean initialized;

    private ChatSocketServiceHolder() {
    }

    /**
     * Inicializa una sola vez los servicios desde el contenedor (llamado por
     * {@link ChatSocketInitFilter} con dependencias inyectadas).
     */
    public static void initOnce(ConversationService cs, MessageService ms) {
        if (initialized) {
            return;
        }
        synchronized (ChatSocketServiceHolder.class) {
            if (initialized) {
                return;
            }
            conversationService = cs;
            messageService = ms;
            initialized = true;
        }
    }

    public static ConversationService getConversationService() {
        return conversationService;
    }

    public static MessageService getMessageService() {
        return messageService;
    }
}
