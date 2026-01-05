package com.socialize.request.client;

import com.socialize.request.model.dto.RatingSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rating-service", path = "/api/ratings")
public interface RatingServiceClient {
    
    @GetMapping("/summary/{userId}")
    RatingSummaryDTO getRatingSummary(@PathVariable("userId") Long userId);
}