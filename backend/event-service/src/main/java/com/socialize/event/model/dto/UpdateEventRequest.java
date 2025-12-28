package com.socialize.event.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxParticipants;

    @Size(max = 1000, message = "Requirements must not exceed 1000 characters")
    private String requirements;
}

