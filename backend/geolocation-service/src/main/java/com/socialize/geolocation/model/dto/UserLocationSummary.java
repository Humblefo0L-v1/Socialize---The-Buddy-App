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
public class UserLocationSummary {
    private Long userId;
    private LocationDTO currentLocation;
    private LocalDateTime lastUpdated;
    private Integer nearbyBuddiesCount;
    private Integer nearbyEventsCount;
    private Double totalDistanceTraveled; // in km
}