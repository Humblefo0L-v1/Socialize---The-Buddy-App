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
public class RespondToRequestDTO {
    @NotNull(message = "Request ID is required")
    private Long requestId;
    
    @NotNull(message = "Decision is required")
    private Boolean approved;
    
    @Size(max = 1000, message = "Response message cannot exceed 1000 characters")
    private String responseMessage;
}