package com.socialize.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DeviceTokenDTO - Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenDTO {
    private Long id;
    private Long userId;
    private String deviceType;
    private String deviceId;
    private String deviceName;
    private Boolean isActive;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}