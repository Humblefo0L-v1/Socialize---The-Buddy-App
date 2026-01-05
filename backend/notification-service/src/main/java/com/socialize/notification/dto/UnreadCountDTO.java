package com.socialize.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UnreadCountDTO - Unread notification count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountDTO {
    private Long userId;
    private Long unreadCount;
}