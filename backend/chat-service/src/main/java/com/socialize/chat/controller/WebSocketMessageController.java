package com.socialize.chat.controller;

import com.socialize.chat.model.dto.*;
import com.socialize.chat.service.ChatService;
import com.socialize.chat.service.TypingIndicatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageController {
    
    private final ChatService chatService;
    private final TypingIndicatorService typingIndicatorService;
    
    /**
     * Send a message to a group chat
     * Client sends to: /app/chat.send
     * Broadcast to: /topic/group/{groupChatId}
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/group/{groupChatId}")
    public MessageDTO sendMessage(@Payload SendMessageRequest request, 
                                  Principal principal) {
        log.info("WebSocket message received from user: {}", principal.getName());
        
        // Extract user ID from principal (you may need to parse JWT)
        Long senderId = extractUserIdFromPrincipal(principal);
        
        return chatService.sendMessageViaWebSocket(request, senderId);
    }
    
    /**
     * Typing indicator
     * Client sends to: /app/chat.typing/{groupChatId}
     * Broadcast to: /topic/group/{groupChatId}/typing
     */
    @MessageMapping("/chat.typing/{groupChatId}")
    @SendTo("/topic/group/{groupChatId}/typing")
    public TypingIndicatorDTO sendTypingIndicator(
            @DestinationVariable String groupChatId,
            @Payload TypingIndicatorDTO indicator,
            Principal principal) {
        
        log.info("Typing indicator from user: {} in group: {}", 
            principal.getName(), groupChatId);
        
        Long userId = extractUserIdFromPrincipal(principal);
        return typingIndicatorService.updateTypingIndicator(groupChatId, userId, indicator.getIsTyping());
    }
    
    /**
     * Mark message as read
     * Client sends to: /app/chat.read
     * Send to specific user: /queue/reply
     */
    @MessageMapping("/chat.read")
    @SendToUser("/queue/reply")
    public MessageDTO markMessageAsRead(@Payload MessageReadRequest request, 
                                       Principal principal) {
        log.info("Mark message as read: {} by user: {}", 
            request.getMessageId(), principal.getName());
        
        Long userId = extractUserIdFromPrincipal(principal);
        return chatService.markMessageAsRead(request.getMessageId(), userId);
    }
    
    /**
     * Add reaction to message
     * Client sends to: /app/chat.reaction
     * Broadcast to: /topic/group/{groupChatId}
     */
    @MessageMapping("/chat.reaction")
    @SendTo("/topic/group/{groupChatId}")
    public MessageDTO addReaction(@Payload AddReactionRequest request,
                                  Principal principal) {
        log.info("Add reaction to message: {} by user: {}", 
            request.getMessageId(), principal.getName());
        
        Long userId = extractUserIdFromPrincipal(principal);
        return chatService.addReaction(request.getMessageId(), userId, request.getEmoji());
    }
    
    /**
     * User joined group
     * Broadcast to: /topic/group/{groupChatId}/system
     */
    @MessageMapping("/chat.join/{groupChatId}")
    @SendTo("/topic/group/{groupChatId}/system")
    public MessageDTO userJoinedGroup(@DestinationVariable String groupChatId,
                                     Principal principal) {
        log.info("User joined group: {} - {}", groupChatId, principal.getName());
        
        // Create system message
        return chatService.createSystemMessage(
            groupChatId, 
            principal.getName() + " joined the group"
        );
    }
    
    /**
     * User left group
     * Broadcast to: /topic/group/{groupChatId}/system
     */
    @MessageMapping("/chat.leave/{groupChatId}")
    @SendTo("/topic/group/{groupChatId}/system")
    public MessageDTO userLeftGroup(@DestinationVariable String groupChatId,
                                   Principal principal) {
        log.info("User left group: {} - {}", groupChatId, principal.getName());
        
        // Create system message
        return chatService.createSystemMessage(
            groupChatId, 
            principal.getName() + " left the group"
        );
    }
    
    /**
     * Helper method to extract user ID from JWT token in Principal
     */
    private Long extractUserIdFromPrincipal(Principal principal) {
        // You'll need to implement this based on your JWT structure
        // For now, returning a placeholder
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            log.error("Error parsing user ID from principal: {}", e.getMessage());
            return 0L;
        }
    }
}