package com.socialize.event.model.dto;

import com.socialize.common.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {
    private Long id;
    private Long hostId;
    private String title;
    private String description;
    private Long categoryId;
    private LocationDTO location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String requirements;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

