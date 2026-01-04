package com.socialize.chat.controller;

import com.socialize.chat.model.dto.*;
import com.socialize.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Messages", description = "APIs for chat message operations")
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a message (REST fallback)")
    public ResponseEntity<MessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Principal principal) {
        
        // Extract user details from principal (you'd get this from JWT)
        Long senderId = extractUserId(principal);
        String senderName = principal.getName();
        String senderProfileImage = null; // Get from user service
        
        MessageDTO message = chatService.sendMessage(
            request, senderId, senderName, senderProfileImage);
        
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
    
    @GetMapping("/group/{groupChatId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get message history for a group")
    public ResponseEntity<Page<MessageDTO>> getMessageHistory(
            @PathVariable String groupChatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Page<MessageDTO> messages = chatService.getMessageHistory(groupChatId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/group/{groupChatId}/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread messages in a group")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(
            @PathVariable String groupChatId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        List<MessageDTO> unreadMessages = chatService.getUnreadMessages(groupChatId, userId);
        return ResponseEntity.ok(unreadMessages);
    }
    
    @GetMapping("/group/{groupChatId}/unread/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread message count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable String groupChatId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        Long count = chatService.getUnreadCount(groupChatId, userId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<MessageDTO> markAsRead(
            @PathVariable String messageId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        MessageDTO message = chatService.markMessageAsRead(messageId, userId);
        return ResponseEntity.ok(message);
    }
    
    @PutMapping("/group/{groupChatId}/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all messages as read in a group")
    public ResponseEntity<Long> markAllAsRead(
            @PathVariable String groupChatId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        long count = chatService.markAllAsRead(groupChatId, userId);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/{messageId}/reaction")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add reaction to message")
    public ResponseEntity<MessageDTO> addReaction(
            @PathVariable String messageId,
            @RequestBody AddReactionRequest request,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        MessageDTO message = chatService.addReaction(messageId, userId, request.getEmoji());
        return ResponseEntity.ok(message);
    }
    
    @DeleteMapping("/{messageId}/reaction")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove reaction from message")
    public ResponseEntity<MessageDTO> removeReaction(
            @PathVariable String messageId,
            @RequestParam String emoji,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        MessageDTO message = chatService.removeReaction(messageId, userId, emoji);
        return ResponseEntity.ok(message);
    }
    
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete message")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        chatService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Edit message")
    public ResponseEntity<MessageDTO> editMessage(
            @PathVariable String messageId,
            @RequestBody String newContent) {
        
        MessageDTO message = chatService.editMessage(messageId, newContent);
        return ResponseEntity.ok(message);
    }
    
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search messages")
    public ResponseEntity<List<MessageDTO>> searchMessages(
            @RequestParam String groupChatId,
            @RequestParam String query) {
        
        List<MessageDTO> messages = chatService.searchMessages(groupChatId, query);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/group/{groupChatId}/media")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get media messages in a group")
    public ResponseEntity<List<MessageDTO>> getMediaMessages(
            @PathVariable String groupChatId) {
        
        List<MessageDTO> mediaMessages = chatService.getMediaMessages(groupChatId);
        return ResponseEntity.ok(mediaMessages);
    }
    
    private Long extractUserId(Principal principal) {
        // Extract user ID from JWT token in principal
        // This is a placeholder - implement based on your JWT structure
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}