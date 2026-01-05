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
public class DistanceCalculationRequest {
    @NotNull
    private Double fromLatitude;
    
    @NotNull
    private Double fromLongitude;
    
    @NotNull
    private Double toLatitude;
    
    @NotNull
    private Double toLongitude;
}