package com.socialize.request.controller;

import com.socialize.request.model.dto.*;
import com.socialize.request.service.RequestService;
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
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Join Requests", description = "APIs for event join request management")
public class RequestController {
    
    private final RequestService requestService;
    
    @PostMapping("/join/{eventId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send join request to event")
    public ResponseEntity<JoinRequestDTO> createJoinRequest(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateJoinRequestDTO dto,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        dto.setEventId(eventId);
        
        JoinRequestDTO request = requestService.createJoinRequest(userId, dto);
        return new ResponseEntity<>(request, HttpStatus.CREATED);
    }
    
    @GetMapping("/{requestId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get request by ID")
    public ResponseEntity<JoinRequestDTO> getRequest(@PathVariable Long requestId) {
        JoinRequestDTO request = requestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }
    
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all sent requests")
    public ResponseEntity<Page<JoinRequestDTO>> getSentRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        Page<JoinRequestDTO> requests = requestService.getSentRequests(userId, page, size);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all received requests (as host)")
    public ResponseEntity<Page<JoinRequestDTO>> getReceivedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        Page<JoinRequestDTO> requests = requestService.getReceivedRequests(userId, page, size);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/received/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get pending requests for host")
    public ResponseEntity<List<JoinRequestDTO>> getPendingRequests(Principal principal) {
        Long userId = extractUserId(principal);
        List<JoinRequestDTO> requests = requestService.getPendingRequestsForHost(userId);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all requests for an event")
    public ResponseEntity<Page<JoinRequestDTO>> getRequestsForEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<JoinRequestDTO> requests = requestService.getRequestsForEvent(eventId, page, size);
        return ResponseEntity.ok(requests);
    }
    
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve join request (host only)")
    public ResponseEntity<JoinRequestDTO> approveRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String responseMessage,
            Principal principal) {
        
        Long hostId = extractUserId(principal);
        JoinRequestDTO request = requestService.approveRequest(
            hostId, requestId, responseMessage);
        return ResponseEntity.ok(request);
    }
    
    @PutMapping("/{requestId}/decline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Decline join request (host only)")
    public ResponseEntity<JoinRequestDTO> declineRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String responseMessage,
            Principal principal) {
        
        Long hostId = extractUserId(principal);
        JoinRequestDTO request = requestService.declineRequest(
            hostId, requestId, responseMessage);
        return ResponseEntity.ok(request);
    }
    
    @DeleteMapping("/{requestId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel join request (requester only)")
    public ResponseEntity<JoinRequestDTO> cancelRequest(
            @PathVariable Long requestId,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        JoinRequestDTO request = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(request);
    }
    
    @PostMapping("/bulk-respond")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk approve/decline requests (host only)")
    public ResponseEntity<Void> bulkRespondToRequests(
            @Valid @RequestBody BulkRespondDTO dto,
            Principal principal) {
        
        Long hostId = extractUserId(principal);
        requestService.bulkRespondToRequests(hostId, dto);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get request statistics")
    public ResponseEntity<RequestStatisticsDTO> getStatistics(
            @RequestParam(defaultValue = "false") boolean asHost,
            Principal principal) {
        
        Long userId = extractUserId(principal);
        RequestStatisticsDTO stats = requestService.getRequestStatistics(userId, asHost);
        return ResponseEntity.ok(stats);
    }
    
    private Long extractUserId(Principal principal) {
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}