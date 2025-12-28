package com.socialize.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantDTO {
    private Long id;
    private Long eventId;
    private Long userId;
    private String status;
    private LocalDateTime joinedAt;
}

