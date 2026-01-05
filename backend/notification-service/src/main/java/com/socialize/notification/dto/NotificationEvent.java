package com.socialize.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * NotificationEvent - Kafka event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventType; // REQUEST_RECEIVED, REQUEST_APPROVED, etc.
    private Long userId;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private Map<String, Object> data;
    
    // Email specific
    private String recipientEmail;
    private String recipientName;
}