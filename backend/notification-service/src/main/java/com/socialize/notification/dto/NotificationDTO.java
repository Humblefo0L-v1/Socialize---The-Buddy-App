package com.socialize.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationDTO - Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type;
    private Long referenceId;
    private String referenceType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private String data;
}