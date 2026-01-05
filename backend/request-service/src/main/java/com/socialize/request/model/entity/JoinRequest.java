package com.socialize.request.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "join_requests", indexes = {
    @Index(name = "idx_event_id", columnList = "event_id"),
    @Index(name = "idx_requester_id", columnList = "requester_user_id"),
    @Index(name = "idx_host_id", columnList = "host_user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_event_status", columnList = "event_id, status"),
    @Index(name = "idx_host_status", columnList = "host_user_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "requester_user_id", nullable = false)
    private Long requesterUserId;
    
    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;
    
    @Column(columnDefinition = "TEXT")
    private String requestMessage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String responseMessage;
    
    // Requester's rating at time of request (for audit trail)
    @Column(nullable = false)
    private Double requesterRating;
    
    // Event's minimum rating requirement at time of request
    @Column(nullable = false)
    private Double eventMinRating;
    
    // Eligibility check result
    @Builder.Default
    @Column(nullable = false)
    private Boolean isEligible = true;
    
    @Column(length = 500)
    private String ineligibilityReason;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;
    
    @Column
    private LocalDateTime respondedAt;
    
    @Column
    private LocalDateTime expiresAt;
    
    // Metadata
    @Column(length = 100)
    private String requesterDeviceInfo;
    
    @Column
    private Integer retryCount;
}