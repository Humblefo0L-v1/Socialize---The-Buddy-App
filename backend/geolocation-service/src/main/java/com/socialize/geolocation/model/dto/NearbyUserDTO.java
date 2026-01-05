package com.socialize.geolocation.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyUserDTO {
    private Long userId;
    private String username;
    private String profileImageUrl;
    private Double latitude;
    private Double longitude;
    private Double distance; // in meters
    private LocalDateTime lastUpdated;
    private Boolean hasActiveEvents;
}