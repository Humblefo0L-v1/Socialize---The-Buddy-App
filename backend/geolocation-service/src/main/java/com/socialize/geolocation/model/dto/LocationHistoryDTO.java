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
public class LocationHistoryDTO {
    private Long id;
    private Long userId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private LocalDateTime timestamp;
    private String activityType;
}