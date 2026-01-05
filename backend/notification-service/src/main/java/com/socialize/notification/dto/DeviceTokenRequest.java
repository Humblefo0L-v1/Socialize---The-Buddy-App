package com.socialize.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeviceTokenRequest - Register device token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotNull(message = "Device type is required")
    private String deviceType; // ANDROID, IOS, WEB
    
    private String deviceId;
    private String deviceName;
}