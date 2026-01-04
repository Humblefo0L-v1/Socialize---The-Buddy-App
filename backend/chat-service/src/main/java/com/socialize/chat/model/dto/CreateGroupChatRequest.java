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
public class CreateGroupChatRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotBlank(message = "Group name is required")
    private String groupName;
    
    @NotNull(message = "Participants are required")
    private Set<Long> participants;
    
    private Long createdBy;
}