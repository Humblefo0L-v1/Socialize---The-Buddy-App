package com.socialize.request.client;

import com.socialize.request.model.dto.CreateGroupChatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "chat-service", path = "/api/chat")
public interface ChatServiceClient {
    
    @PostMapping("/groups")
    void createGroupChat(@RequestBody CreateGroupChatDTO dto);
    
    @PostMapping("/groups/event/{eventId}/participants/{userId}")
    void addParticipantToEventChat(
        @PathVariable("eventId") Long eventId,
        @PathVariable("userId") Long userId
    );
}