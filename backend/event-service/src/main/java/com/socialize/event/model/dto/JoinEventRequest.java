package com.socialize.event.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinEventRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    private String message;
}

