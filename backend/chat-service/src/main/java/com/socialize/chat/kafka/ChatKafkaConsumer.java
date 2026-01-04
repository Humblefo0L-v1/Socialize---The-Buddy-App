package com.socialize.chat.kafka;

import com.socialize.chat.model.dto.CreateGroupChatRequest;
import com.socialize.chat.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatKafkaConsumer {
    
    private final GroupChatService groupChatService;
    
    /**
     * Listen for request-approved events to create group chats
     */
    @KafkaListener(topics = "request-approved", groupId = "chat-service-group")
    public void handleRequestApproved(Map<String, Object> event) {
        log.info("Received request-approved event: {}", event);
        
        try {
            Long eventId = ((Number) event.get("eventId")).longValue();
            String eventName = (String) event.get("eventName");
            Long hostId = ((Number) event.get("hostId")).longValue();
            Long approverId = ((Number) event.get("approverId")).longValue();
            
            @SuppressWarnings("unchecked")
            List<Number> participantsList = (List<Number>) event.get("participants");
            Set<Long> participants = new HashSet<>();
            for (Number num : participantsList) {
                participants.add(num.longValue());
            }
            
            // Create group chat request
            CreateGroupChatRequest request = CreateGroupChatRequest.builder()
                .eventId(eventId)
                .groupName(eventName + " - Group Chat")
                .participants(participants)
                .createdBy(hostId)
                .build();
            
            // Create group chat
            groupChatService.createGroupChat(request);
            
            log.info("Group chat created for event: {}", eventId);
            
        } catch (Exception e) {
            log.error("Error creating group chat from approved request: {}", e.getMessage(), e);
        }
    }
}