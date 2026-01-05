package com.socialize.request.model.dto;

import com.socialize.request.model.entity.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateJoinRequestDTO {
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @Size(max = 1000, message = "Request message cannot exceed 1000 characters")
    private String requestMessage;
    
    private String deviceInfo;
}