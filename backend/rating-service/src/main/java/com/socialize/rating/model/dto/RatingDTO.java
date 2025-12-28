package com.socialize.rating.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {
    private Long id;
    private Long raterId;
    private Long ratedUserId;
    private Long eventId;
    private Integer score;
    private String comment;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;
}
