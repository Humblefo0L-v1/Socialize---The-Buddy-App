package com.socialize.geolocation.model.dto;

import jakarta.validation.constraints.*;
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
public class BatchLocationUpdateRequest {
    @NotNull
    @Size(min = 1, max = 100, message = "Batch size must be between 1 and 100")
    private List<LocationUpdateRequest> locations;
}