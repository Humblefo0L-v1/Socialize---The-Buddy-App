package com.socialize.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "typing_indicators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {
    
    @Id
    private String id;
    
    @Indexed
    private String groupChatId;
    
    private Long userId;
    
    private String username;
    
    @Builder.Default
    private Boolean isTyping = true;
    
    @Indexed(expireAfterSeconds = 5)  // Auto-delete after 5 seconds
    private LocalDateTime timestamp;
}