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
public class EligibilityCheckResult {
    private Boolean eligible;
    private List<String> reasons;
    private Double requesterRating;
    private Double requiredRating;
    private Boolean ratingMet;
    private Boolean locationMet;
    private Boolean customRequirementsMet;
}