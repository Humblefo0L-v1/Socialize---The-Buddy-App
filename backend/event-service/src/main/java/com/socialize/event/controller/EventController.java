package com.socialize.event.controller;

import com.socialize.common.dto.ApiResponse;
import com.socialize.common.dto.PageResponse;
import com.socialize.event.model.dto.*;
import com.socialize.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CreateEventRequest request) {
        
        // In real implementation, extract userId from JWT or user service
        // For now, we'll use a header or default value
        Long hostId = userId != null ? userId : 1L;
        
        EventDTO event = eventService.createEvent(hostId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", event));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventDTO>> getEvent(@PathVariable Long eventId) {
        EventDTO event = eventService.getEventById(eventId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<PageResponse<EventDTO>>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<EventDTO> events = eventService.getUpcomingEvents(pageable);
        
        PageResponse<EventDTO> pageResponse = new PageResponse<>(
                events.getContent(),
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isLast()
        );
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getNearbyEvents(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        List<EventDTO> events = eventService.getNearbyEvents(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<ApiResponse<PageResponse<EventDTO>>> getEventsByHost(
            @PathVariable Long hostId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EventDTO> events = eventService.getEventsByHost(hostId, pageable);
        
        PageResponse<EventDTO> pageResponse = new PageResponse<>(
                events.getContent(),
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isLast()
        );
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody UpdateEventRequest request) {
        
        Long hostId = userId != null ? userId : 1L;
        EventDTO event = eventService.updateEvent(eventId, hostId, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Long hostId = userId != null ? userId : 1L;
        eventService.deleteEvent(eventId, hostId);
        return ResponseEntity.ok(ApiResponse.success("Event cancelled successfully", null));
    }

    @PostMapping("/{eventId}/join")
    public ResponseEntity<ApiResponse<ParticipantDTO>> joinEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Long requesterId = userId != null ? userId : 2L;
        ParticipantDTO participant = eventService.joinEvent(eventId, requesterId);
        return ResponseEntity.ok(ApiResponse.success("Join request sent successfully", participant));
    }

    @PostMapping("/{eventId}/participants/{participantId}/approve")
    public ResponseEntity<ApiResponse<ParticipantDTO>> approveParticipant(
            @PathVariable Long eventId,
            @PathVariable Long participantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Long hostId = userId != null ? userId : 1L;
        ParticipantDTO participant = eventService.approveParticipant(eventId, participantId, hostId);
        return ResponseEntity.ok(ApiResponse.success("Participant approved", participant));
    }

    @PostMapping("/{eventId}/participants/{participantId}/decline")
    public ResponseEntity<ApiResponse<ParticipantDTO>> declineParticipant(
            @PathVariable Long eventId,
            @PathVariable Long participantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Long hostId = userId != null ? userId : 1L;
        ParticipantDTO participant = eventService.declineParticipant(eventId, participantId, hostId);
        return ResponseEntity.ok(ApiResponse.success("Participant declined", participant));
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<ApiResponse<List<ParticipantDTO>>> getEventParticipants(
            @PathVariable Long eventId) {
        
        List<ParticipantDTO> participants = eventService.getEventParticipants(eventId);
        return ResponseEntity.ok(ApiResponse.success(participants));
    }

    @GetMapping("/user/{userId}/participations")
    public ResponseEntity<ApiResponse<List<ParticipantDTO>>> getUserEvents(
            @PathVariable Long userId) {
        
        List<ParticipantDTO> events = eventService.getUserEvents(userId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}

