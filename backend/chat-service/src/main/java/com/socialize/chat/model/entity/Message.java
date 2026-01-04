package com.socialize.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "messages")
@CompoundIndex(name = "group_timestamp_idx", def = "{'groupChatId': 1, 'timestamp': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    private String id;
    
    @Indexed
    private String groupChatId;
    
    @Indexed
    private Long senderId;
    
    private String senderName;
    
    private String senderProfileImage;
    
    private MessageType messageType;
    
    private String content;  // Text content or file description
    
    private String mediaUrl;  // URL for files, images, videos, voice notes
    
    private String thumbnailUrl;  // Thumbnail for images/videos
    
    private Long fileSize;  // File size in bytes
    
    private String fileName;  // Original file name
    
    private String mimeType;  // MIME type of the file
    
    private String replyToMessageId;  // ID of message being replied to
    
    @Builder.Default
    private List<Reaction> reactions = new ArrayList<>();
    
    @Builder.Default
    private Set<Long> readBy = new HashSet<>();
    
    @Builder.Default
    private Set<Long> deliveredTo = new HashSet<>();
    
    @Indexed
    private LocalDateTime timestamp;
    
    @Builder.Default
    private Boolean isDeleted = false;
    
    private LocalDateTime deletedAt;
    
    @Builder.Default
    private Boolean isEdited = false;
    
    private LocalDateTime editedAt;
    
    // Nested class for reactions
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reaction {
        private Long userId;
        private String emoji;
        private LocalDateTime timestamp;
    }
}
