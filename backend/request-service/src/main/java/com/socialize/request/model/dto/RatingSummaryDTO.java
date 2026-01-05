package com.socialize.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingSummaryDTO {
    private Long userId;
    private Double averageRating;
    private Integer totalRatings;
    private Double reliabilityScore;
    private Double communicationScore;
    private Double friendlinessScore;
}