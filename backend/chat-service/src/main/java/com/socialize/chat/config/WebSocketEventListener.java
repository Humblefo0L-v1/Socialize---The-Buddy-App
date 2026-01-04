package com.socialize.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection established: {}", 
            event.getMessage().getHeaders().get("simpSessionId"));
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("WebSocket connection closed: {}", 
            event.getSessionId());
        
        // Handle user disconnect logic (e.g., update typing indicators)
    }
    
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage()
            .getHeaders().get("simpDestination");
        log.info("User subscribed to: {}", destination);
    }
    
    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        log.info("User unsubscribed from session: {}", event.getMessage().getHeaders().get("simpSessionId"));
    }
}