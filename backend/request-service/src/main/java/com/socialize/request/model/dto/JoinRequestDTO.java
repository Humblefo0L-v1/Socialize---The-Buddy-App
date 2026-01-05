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
public class JoinRequestDTO {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventHostName;
    private Long requesterUserId;
    private String requesterUsername;
    private String requesterProfileImage;
    private Long hostUserId;
    private String hostUsername;
    private String requestMessage;
    private RequestStatus status;
    private String responseMessage;
    private Double requesterRating;
    private Double eventMinRating;
    private Boolean isEligible;
    private String ineligibilityReason;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;
}