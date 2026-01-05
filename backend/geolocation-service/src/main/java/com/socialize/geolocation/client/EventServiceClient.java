package com.socialize.geolocation.client;

import com.socialize.geolocation.model.dto.NearbyEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "event-service", path = "/api/events")
public interface EventServiceClient {
    
    @GetMapping("/nearby")
    List<NearbyEventDTO> getNearbyEvents(
        @RequestParam("lat") Double latitude,
        @RequestParam("lng") Double longitude,
        @RequestParam("radius") Double radius
    );
    
    @GetMapping("/user/{userId}/active/exists")
    boolean hasActiveEvents(@PathVariable("userId") Long userId);
}