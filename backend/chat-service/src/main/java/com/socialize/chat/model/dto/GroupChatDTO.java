package com.socialize.chat.model.dto;

import com.socialize.chat.model.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatDTO {
    private String id;
    private Long eventId;
    private String groupName;
    private String groupImage;
    private Set<Long> participants;
    private Set<Long> admins;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private Long lastMessageSenderId;
    private Integer messageCount;
    private Integer unreadCount;
    private Boolean isActive;
}