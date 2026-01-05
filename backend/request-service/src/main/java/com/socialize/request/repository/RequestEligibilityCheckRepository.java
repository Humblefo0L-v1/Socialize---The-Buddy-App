package com.socialize.request.repository;

import com.socialize.request.model.entity.RequestEligibilityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestEligibilityCheckRepository extends JpaRepository<RequestEligibilityCheck, Long> {
    
    /**
     * Find all eligibility checks for a request
     */
    List<RequestEligibilityCheck> findByRequestIdOrderByCheckedAtDesc(Long requestId);
    
    /**
     * Find failed checks for a request
     */
    List<RequestEligibilityCheck> findByRequestIdAndPassedFalse(Long requestId);
    
    /**
     * Check if request passed all eligibility checks
     */
    boolean existsByRequestIdAndPassedFalse(Long requestId);
}