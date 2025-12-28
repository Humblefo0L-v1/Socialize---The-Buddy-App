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
public class EventMessage {
    private Long eventId;
    private Long hostId;
    private String eventTitle;
    private String eventType;
    private Long userId;
    private String status;
    private LocalDateTime timestamp;
}

