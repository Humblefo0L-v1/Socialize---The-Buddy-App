package com.socialize.geolocation.controller;

import com.socialize.geolocation.model.dto.*;
import com.socialize.geolocation.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for location tracking and queries")
public class LocationController {
    
    private final LocationService locationService;
    
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user's current location")
    public ResponseEntity<LocationDTO> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        LocationDTO location = locationService.updateLocation(userId, request);
        return new ResponseEntity<>(location, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's current location")
    public ResponseEntity<LocationDTO> getUserLocation(@PathVariable Long userId) {
        LocationDTO location = locationService.getCurrentLocation(userId);
        return ResponseEntity.ok(location);
    }
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my current location")
    public ResponseEntity<LocationDTO> getMyLocation(Principal principal) {
        Long userId = extractUserId(principal);
        LocationDTO location = locationService.getCurrentLocation(userId);
        return ResponseEntity.ok(location);
    }
    
    @GetMapping("/nearby/buddies")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Find nearby buddies")
    public ResponseEntity<List<NearbyUserDTO>> findNearbyBuddies(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10000") Double radius,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        List<NearbyUserDTO> nearbyUsers = locationService
            .findNearbyBuddies(userId, lat, lng, radius);
        return ResponseEntity.ok(nearbyUsers);
    }
    
    @GetMapping("/nearby/events")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Find nearby events")
    public ResponseEntity<List<NearbyEventDTO>> findNearbyEvents(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10000") Double radius) {
        
        List<NearbyEventDTO> nearbyEvents = locationService
            .findNearbyEvents(lat, lng, radius);
        return ResponseEntity.ok(nearbyEvents);
    }
    
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get location history")
    public ResponseEntity<Page<LocationHistoryDTO>> getLocationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        Page<LocationHistoryDTO> history = locationService
            .getLocationHistory(userId, page, size);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping("/distance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calculate distance between two points")
    public ResponseEntity<DistanceCalculationResponse> calculateDistance(
            @Valid @RequestBody DistanceCalculationRequest request) {
        
        DistanceCalculationResponse response = locationService
            .calculateDistance(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/batch-update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Batch update locations")
    public ResponseEntity<Void> batchUpdateLocations(
            @Valid @RequestBody BatchLocationUpdateRequest request,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        locationService.batchUpdateLocations(userId, request.getLocations());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get location summary")
    public ResponseEntity<UserLocationSummary> getLocationSummary(
            Principal principal) {
        
        Long userId = extractUserId(principal);
        UserLocationSummary summary = locationService.getUserLocationSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    private Long extractUserId(Principal principal) {
        // Extract user ID from JWT token
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
