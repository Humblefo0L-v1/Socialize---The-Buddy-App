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
public class MessageDTO {
    private String id;
    
    @NotBlank(message = "Group chat ID is required")
    private String groupChatId;
    
    @NotNull(message = "Sender ID is required")
    private Long senderId;
    
    private String senderName;
    private String senderProfileImage;
    
    @NotNull(message = "Message type is required")
    private MessageType messageType;
    
    private String content;
    private String mediaUrl;
    private String thumbnailUrl;
    private Long fileSize;
    private String fileName;
    private String mimeType;
    private String replyToMessageId;
    private List<ReactionDTO> reactions;
    private Set<Long> readBy;
    private Set<Long> deliveredTo;
    private LocalDateTime timestamp;
    private Boolean isDeleted;
    private Boolean isEdited;
}