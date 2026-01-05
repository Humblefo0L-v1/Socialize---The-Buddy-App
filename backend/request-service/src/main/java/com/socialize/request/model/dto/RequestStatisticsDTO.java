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
public class RequestStatisticsDTO {
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long declinedRequests;
    private Long cancelledRequests;
    private Long expiredRequests;
    private Double approvalRate;
    private Long averageResponseTimeMinutes;
}