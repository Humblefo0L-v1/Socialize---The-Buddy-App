package com.socialize.request.service;

import com.socialize.request.model.dto.EligibilityCheckResult;
import com.socialize.request.model.dto.EventDTO;
import com.socialize.request.model.dto.RatingSummaryDTO;
import com.socialize.request.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityCheckService {
    
    @Value("${request.default-min-rating}")
    private Double defaultMinRating;
    
    /**
     * Check if user is eligible to join event
     */
    public EligibilityCheckResult checkEligibility(
            EventDTO event, 
            UserDTO user, 
            RatingSummaryDTO ratingSummary) {
        
        log.info("Checking eligibility for user {} to join event {}", 
            user.getId(), event.getId());
        
        List<String> reasons = new ArrayList<>();
        boolean eligible = true;
        
        // Check 1: Rating requirement
        Double requiredRating = event.getMinRating() != null 
            ? event.getMinRating() 
            : defaultMinRating;
        
        Double userRating = ratingSummary.getAverageRating() != null 
            ? ratingSummary.getAverageRating() 
            : 0.0;
        
        boolean ratingMet = true;
        if (requiredRating != null && requiredRating > 0) {
            if (userRating < requiredRating) {
                ratingMet = false;
                eligible = false;
                reasons.add(String.format(
                    "Rating requirement not met. Required: %.1f, Current: %.1f", 
                    requiredRating, userRating));
            }
        }
        
        // Check 2: Event is not full
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            eligible = false;
            reasons.add("Event is already full");
        }
        
        // Check 3: Custom eligibility criteria (if any)
        boolean customRequirementsMet = checkCustomCriteria(event, user);
        if (!customRequirementsMet) {
            eligible = false;
            reasons.add("Custom event requirements not met");
        }
        
        // Check 4: User is not the host
        if (user.getId().equals(event.getHostUserId())) {
            eligible = false;
            reasons.add("Host cannot join their own event");
        }
        
        log.info("Eligibility check result for user {}: {}", 
            user.getId(), eligible ? "ELIGIBLE" : "NOT ELIGIBLE");
        
        return EligibilityCheckResult.builder()
            .eligible(eligible)
            .reasons(reasons)
            .requesterRating(userRating)
            .requiredRating(requiredRating)
            .ratingMet(ratingMet)
            .locationMet(true) // TODO: Implement location-based eligibility
            .customRequirementsMet(customRequirementsMet)
            .build();
    }
    
    /**
     * Check custom criteria (can be extended)
     */
    private boolean checkCustomCriteria(EventDTO event, UserDTO user) {
        // Parse eligibility criteria JSON if present
        // For now, return true
        return true;
    }
}