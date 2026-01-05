package com.socialize.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NotificationPreferenceDTO - Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private Long userId;
    private Boolean emailEnabled;
    private Boolean emailRequestReceived;
    private Boolean emailRequestApproved;
    private Boolean emailEventUpdates;
    private Boolean pushEnabled;
    private Boolean pushRequestReceived;
    private Boolean pushRequestApproved;
    private Boolean pushChatMessages;
    private Boolean pushEventUpdates;
    private Boolean inAppEnabled;
    private Boolean quietHoursEnabled;
    private Integer quietHoursStart;
    private Integer quietHoursEnd;
}