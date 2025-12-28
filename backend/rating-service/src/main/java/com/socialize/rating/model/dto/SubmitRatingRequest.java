package com.socialize.rating.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRatingRequest {

    @NotNull(message = "Rated user ID is required")
    private Long ratedUserId;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must not exceed 5")
    private Integer score;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    private Boolean isAnonymous = true;
}
