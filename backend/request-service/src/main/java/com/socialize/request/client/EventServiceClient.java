package com.socialize.request.client;

import com.socialize.request.model.dto.EventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service", path = "/api/events")
public interface EventServiceClient {
    
    @GetMapping("/{eventId}")
    EventDTO getEventById(@PathVariable("eventId") Long eventId);
    
    @PostMapping("/{eventId}/participants/{userId}")
    void addParticipant(
        @PathVariable("eventId") Long eventId,
        @PathVariable("userId") Long userId
    );
    
    @GetMapping("/{eventId}/is-full")
    boolean isEventFull(@PathVariable("eventId") Long eventId);
}