package com.socialize.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "group_chats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChat {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private Long eventId;
    
    private String groupName;
    
    private String groupImage;
    
    @Builder.Default
    private Set<Long> participants = new HashSet<>();
    
    @Builder.Default
    private Set<Long> admins = new HashSet<>();
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastMessageAt;
    
    private String lastMessage;
    
    private Long lastMessageSenderId;
    
    @Builder.Default
    private Integer messageCount = 0;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private LocalDateTime archivedAt;
    
    // Group settings
    @Builder.Default
    private GroupSettings settings = new GroupSettings();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSettings {
        @Builder.Default
        private Boolean onlyAdminsCanSend = false;
        
        @Builder.Default
        private Boolean allowFileSharing = true;
        
        @Builder.Default
        private Boolean allowVoiceMessages = true;
        
        @Builder.Default
        private Boolean muteNotifications = false;
    }
}