package com.socialize.chat.controller;

import com.socialize.chat.model.dto.CreateGroupChatRequest;
import com.socialize.chat.model.dto.GroupChatDTO;
import com.socialize.chat.model.entity.GroupChat;
import com.socialize.chat.service.GroupChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat/groups")
@RequiredArgsConstructor
@Tag(name = "Group Chat", description = "APIs for group chat management")
public class GroupChatController {
    
    private final GroupChatService groupChatService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new group chat")
    public ResponseEntity<GroupChatDTO> createGroupChat(
            @Valid @RequestBody CreateGroupChatRequest request,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        request.setCreatedBy(userId);
        
        GroupChatDTO groupChat = groupChatService.createGroupChat(request);
        return new ResponseEntity<>(groupChat, HttpStatus.CREATED);
    }
    
    @GetMapping("/{groupChatId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get group chat by ID")
    public ResponseEntity<GroupChatDTO> getGroupChat(
            @PathVariable String groupChatId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        GroupChatDTO groupChat = groupChatService.getGroupChatById(groupChatId, userId);
        return ResponseEntity.ok(groupChat);
    }
    
    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get group chat by event ID")
    public ResponseEntity<GroupChatDTO> getGroupChatByEvent(
            @PathVariable Long eventId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        GroupChatDTO groupChat = groupChatService.getGroupChatByEventId(eventId, userId);
        return ResponseEntity.ok(groupChat);
    }
    
    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all group chats for current user")
    public ResponseEntity<List<GroupChatDTO>> getUserGroupChats(Principal principal) {
        Long userId = extractUserId(principal);
        List<GroupChatDTO> groupChats = groupChatService.getUserGroupChats(userId);
        return ResponseEntity.ok(groupChats);
    }
    
    @PostMapping("/{groupChatId}/participants/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add participant to group")
    public ResponseEntity<Void> addParticipant(
            @PathVariable String groupChatId,
            @PathVariable Long userId) {
        
        groupChatService.addParticipant(groupChatId, userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{groupChatId}/participants/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove participant from group")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable String groupChatId,
            @PathVariable Long userId) {
        
        groupChatService.removeParticipant(groupChatId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{groupChatId}/settings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update group settings")
    public ResponseEntity<Void> updateSettings(
            @PathVariable String groupChatId,
            @RequestBody GroupChat.GroupSettings settings) {
        
        groupChatService.updateGroupSettings(groupChatId, settings);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{groupChatId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Archive group chat")
    public ResponseEntity<Void> archiveGroupChat(@PathVariable String groupChatId) {
        groupChatService.archiveGroupChat(groupChatId);
        return ResponseEntity.noContent().build();
    }
    
    private Long extractUserId(Principal principal) {
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}