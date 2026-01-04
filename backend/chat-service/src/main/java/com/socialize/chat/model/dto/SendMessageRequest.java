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
public class SendMessageRequest {
    @NotBlank(message = "Group chat ID is required")
    private String groupChatId;
    
    @NotNull(message = "Message type is required")
    private MessageType messageType;
    
    private String content;
    private String mediaUrl;
    private String replyToMessageId;
}