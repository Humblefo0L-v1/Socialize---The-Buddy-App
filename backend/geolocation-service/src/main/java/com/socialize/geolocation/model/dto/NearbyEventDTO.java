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
public class NearbyEventDTO {
    private Long eventId;
    private String title;
    private String description;
    private Long hostUserId;
    private String hostUsername;
    private Double latitude;
    private Double longitude;
    private Double distance; // in meters
    private LocalDateTime startTime;
    private Integer currentParticipants;
    private Integer maxParticipants;
}