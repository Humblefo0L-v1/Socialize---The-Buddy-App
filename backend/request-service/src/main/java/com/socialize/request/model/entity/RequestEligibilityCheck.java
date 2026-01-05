package com.socialize.request.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_eligibility_checks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestEligibilityCheck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", nullable = false)
    private Long requestId;
    
    @Column(nullable = false)
    private String checkType; // RATING, AGE, LOCATION, CUSTOM
    
    @Column(nullable = false)
    private Boolean passed;
    
    @Column(length = 500)
    private String failureReason;
    
    @Column(columnDefinition = "TEXT")
    private String checkDetails;
    
    @Column(nullable = false)
    private LocalDateTime checkedAt;
}