package com.socialize.chat.service;

import com.socialize.chat.model.dto.MessageDTO;
import com.socialize.chat.model.dto.TypingIndicatorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSenderService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send message to group chat topic
     */
    public void sendMessageToGroup(String groupChatId, MessageDTO message) {
        String destination = "/topic/group/" + groupChatId;
        log.info("Sending message to destination: {}", destination);
        messagingTemplate.convertAndSend(destination, message);
    }
    
    /**
     * Send typing indicator to group
     */
    public void sendTypingIndicator(String groupChatId, TypingIndicatorDTO indicator) {
        String destination = "/topic/group/" + groupChatId + "/typing";
        messagingTemplate.convertAndSend(destination, indicator);
    }
    
    /**
     * Send notification to specific user
     */
    public void sendToUser(String username, String destination, Object payload) {
        log.info("Sending to user: {} at destination: {}", username, destination);
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }
    
    /**
     * Send system message to group
     */
    public void sendSystemMessage(String groupChatId, String message) {
        String destination = "/topic/group/" + groupChatId + "/system";
        messagingTemplate.convertAndSend(destination, message);
    }
    
    /**
     * Broadcast message to all connected users
     */
    public void broadcastMessage(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}