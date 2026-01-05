package com.socialize.request.service;

import com.socialize.request.client.ChatServiceClient;
import com.socialize.request.client.EventServiceClient;
import com.socialize.request.client.RatingServiceClient;
import com.socialize.request.client.UserServiceClient;
import com.socialize.request.exception.DuplicateRequestException;
import com.socialize.request.exception.RequestNotFoundException;
import com.socialize.request.exception.UnauthorizedException;
import com.socialize.request.kafka.RequestKafkaProducer;
import com.socialize.request.model.dto.*;
import com.socialize.request.model.entity.JoinRequest;
import com.socialize.request.model.entity.RequestStatus;
import com.socialize.request.repository.JoinRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    
    private final JoinRequestRepository joinRequestRepository;
    private final EligibilityCheckService eligibilityCheckService;
    private final RequestKafkaProducer kafkaProducer;
    private final EventServiceClient eventServiceClient;
    private final UserServiceClient userServiceClient;
    private final RatingServiceClient ratingServiceClient;
    private final ChatServiceClient chatServiceClient;
    
    @Value("${request.auto-expire-days}")
    private int autoExpireDays;
    
    @Value("${request.max-pending-requests}")
    private int maxPendingRequests;
    
    @Value("${request.allow-duplicate-requests}")
    private boolean allowDuplicateRequests;
    
    /**
     * Create a join request
     */
    @Transactional
    public JoinRequestDTO createJoinRequest(Long requesterUserId, CreateJoinRequestDTO dto) {
        log.info("Creating join request for event {} by user {}", 
            dto.getEventId(), requesterUserId);
        
        // Check max pending requests
        Long pendingCount = joinRequestRepository.countPendingRequestsByUser(requesterUserId);
        if (pendingCount >= maxPendingRequests) {
            throw new IllegalStateException(
                "Maximum pending requests limit reached: " + maxPendingRequests);
        }
        
        // Check for duplicate request
        if (!allowDuplicateRequests) {
            boolean exists = joinRequestRepository.existsByEventIdAndRequesterUserIdAndStatusIn(
                dto.getEventId(),
                requesterUserId,
                Arrays.asList(RequestStatus.PENDING, RequestStatus.APPROVED)
            );
            
            if (exists) {
                throw new DuplicateRequestException(
                    "You already have an active request for this event");
            }
        }
        
        // Fetch event details
        EventDTO event = eventServiceClient.getEventById(dto.getEventId());
        
        // Check if event is full
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new IllegalStateException("Event is already full");
        }
        
        // Fetch requester details
        UserDTO requester = userServiceClient.getUserById(requesterUserId);
        
        // Get requester's rating
        RatingSummaryDTO ratingSummary = ratingServiceClient
            .getRatingSummary(requesterUserId);
        
        // Check eligibility
        EligibilityCheckResult eligibilityResult = eligibilityCheckService
            .checkEligibility(event, requester, ratingSummary);
        
        // Create join request
        JoinRequest request = JoinRequest.builder()
            .eventId(dto.getEventId())
            .requesterUserId(requesterUserId)
            .hostUserId(event.getHostUserId())
            .requestMessage(dto.getRequestMessage())
            .status(event.getAutoApprove() && eligibilityResult.getEligible() 
                ? RequestStatus.AUTO_APPROVED 
                : RequestStatus.PENDING)
            .requesterRating(ratingSummary.getAverageRating())
            .eventMinRating(event.getMinRating())
            .isEligible(eligibilityResult.getEligible())
            .ineligibilityReason(eligibilityResult.getEligible() 
                ? null 
                : String.join(", ", eligibilityResult.getReasons()))
            .requesterDeviceInfo(dto.getDeviceInfo())
            .expiresAt(LocalDateTime.now().plusDays(autoExpireDays))
            .retryCount(0)
            .build();
        
        JoinRequest savedRequest = joinRequestRepository.save(request);
        
        // If auto-approved, handle immediately
        if (savedRequest.getStatus() == RequestStatus.AUTO_APPROVED) {
            handleApproval(savedRequest);
        } else {
            // Send notification to host via Kafka
            kafkaProducer.sendRequestCreatedEvent(savedRequest, event, requester);
        }
        
        log.info("Join request created with ID: {} (Status: {})", 
            savedRequest.getId(), savedRequest.getStatus());
        
        return convertToDTO(savedRequest, event, requester);
    }
    
    /**
     * Approve a join request
     */
    @Transactional
    @CacheEvict(value = "requests", key = "#requestId")
    public JoinRequestDTO approveRequest(Long hostUserId, Long requestId, String responseMessage) {
        log.info("Approving request {} by host {}", requestId, hostUserId);
        
        JoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException("Request not found"));
        
        // Verify host ownership
        if (!request.getHostUserId().equals(hostUserId)) {
            throw new UnauthorizedException("You are not authorized to approve this request");
        }
        
        // Check if request is still pending
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }
        
        // Update request
        request.setStatus(RequestStatus.APPROVED);
        request.setResponseMessage(responseMessage);
        request.setRespondedAt(LocalDateTime.now());
        
        JoinRequest updatedRequest = joinRequestRepository.save(request);
        
        // Handle approval (add to event, create group chat)
        handleApproval(updatedRequest);
        
        log.info("Request {} approved successfully", requestId);
        
        return convertToDTO(updatedRequest);
    }
    
    /**
     * Decline a join request
     */
    @Transactional
    @CacheEvict(value = "requests", key = "#requestId")
    public JoinRequestDTO declineRequest(Long hostUserId, Long requestId, String responseMessage) {
        log.info("Declining request {} by host {}", requestId, hostUserId);
        
        JoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException("Request not found"));
        
        // Verify host ownership
        if (!request.getHostUserId().equals(hostUserId)) {
            throw new UnauthorizedException("You are not authorized to decline this request");
        }
        
        // Check if request is still pending
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }
        
        // Update request
        request.setStatus(RequestStatus.DECLINED);
        request.setResponseMessage(responseMessage);
        request.setRespondedAt(LocalDateTime.now());
        
        JoinRequest updatedRequest = joinRequestRepository.save(request);
        
        // Send notification to requester
        kafkaProducer.sendRequestDeclinedEvent(updatedRequest);
        
        log.info("Request {} declined successfully", requestId);
        
        return convertToDTO(updatedRequest);
    }
    
    /**
     * Cancel a join request (by requester)
     */
    @Transactional
    @CacheEvict(value = "requests", key = "#requestId")
    public JoinRequestDTO cancelRequest(Long requesterUserId, Long requestId) {
        log.info("Cancelling request {} by user {}", requestId, requesterUserId);
        
        JoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException("Request not found"));
        
        // Verify requester ownership
        if (!request.getRequesterUserId().equals(requesterUserId)) {
            throw new UnauthorizedException("You are not authorized to cancel this request");
        }
        
        // Check if request is still pending
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }
        
        // Update request
        request.setStatus(RequestStatus.CANCELLED);
        request.setRespondedAt(LocalDateTime.now());
        
        JoinRequest updatedRequest = joinRequestRepository.save(request);
        
        // Send notification to host
        kafkaProducer.sendRequestCancelledEvent(updatedRequest);
        
        log.info("Request {} cancelled successfully", requestId);
        
        return convertToDTO(updatedRequest);
    }
    
    /**
     * Get request by ID
     */
    @Cacheable(value = "requests", key = "#requestId")
    public JoinRequestDTO getRequestById(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException("Request not found"));
        
        return convertToDTO(request);
    }
    
    /**
     * Get sent requests (by requester)
     */
    public Page<JoinRequestDTO> getSentRequests(Long requesterUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JoinRequest> requests = joinRequestRepository
            .findByRequesterUserIdOrderByRequestedAtDesc(requesterUserId, pageable);
        
        return requests.map(this::convertToDTO);
    }
    
    /**
     * Get received requests (by host)
     */
    public Page<JoinRequestDTO> getReceivedRequests(Long hostUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JoinRequest> requests = joinRequestRepository
            .findByHostUserIdOrderByRequestedAtDesc(hostUserId, pageable);
        
        return requests.map(this::convertToDTO);
    }
    
    /**
     * Get pending requests for host
     */
    public List<JoinRequestDTO> getPendingRequestsForHost(Long hostUserId) {
        List<JoinRequest> requests = joinRequestRepository
            .findPendingRequestsByHost(hostUserId);
        
        return requests.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get requests for an event
     */
    public Page<JoinRequestDTO> getRequestsForEvent(Long eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JoinRequest> requests = joinRequestRepository
            .findByEventIdOrderByRequestedAtDesc(eventId, pageable);
        
        return requests.map(this::convertToDTO);
    }
    
    /**
     * Bulk approve/decline requests
     */
    @Transactional
    public void bulkRespondToRequests(Long hostUserId, BulkRespondDTO dto) {
        log.info("Bulk {} requests by host {}", 
            dto.getApproved() ? "approving" : "declining", hostUserId);
        
        List<JoinRequest> requests = joinRequestRepository.findAllById(dto.getRequestIds());
        
        // Verify all requests belong to host
        boolean allValid = requests.stream()
            .allMatch(r -> r.getHostUserId().equals(hostUserId) && 
                          r.getStatus() == RequestStatus.PENDING);
        
        if (!allValid) {
            throw new UnauthorizedException("Invalid requests or not authorized");
        }
        
        RequestStatus newStatus = dto.getApproved() 
            ? RequestStatus.APPROVED 
            : RequestStatus.DECLINED;
        
        // Bulk update
        joinRequestRepository.bulkUpdateRequestStatus(
            dto.getRequestIds(),
            newStatus,
            LocalDateTime.now(),
            dto.getResponseMessage()
        );
        
        // Handle approvals
        if (dto.getApproved()) {
            requests.forEach(this::handleApproval);
        }
        
        // Send Kafka events
        requests.forEach(request -> {
            if (dto.getApproved()) {
                kafkaProducer.sendRequestApprovedEvent(request);
            } else {
                kafkaProducer.sendRequestDeclinedEvent(request);
            }
        });
        
        log.info("Bulk operation completed for {} requests", dto.getRequestIds().size());
    }
    
    /**
     * Get request statistics
     */
    public RequestStatisticsDTO getRequestStatistics(Long userId, boolean asHost) {
        List<Object[]> stats = asHost
            ? joinRequestRepository.getRequestStatisticsByEvent(userId)
            : joinRequestRepository.getRequestStatisticsByUser(userId);
        
        Map<RequestStatus, Long> statusCounts = new HashMap<>();
        for (Object[] row : stats) {
            RequestStatus status = (RequestStatus) row[0];
            Long count = ((Number) row[1]).longValue();
            statusCounts.put(status, count);
        }
        
        Long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        Long approved = statusCounts.getOrDefault(RequestStatus.APPROVED, 0L);
        Double approvalRate = total > 0 ? (approved.doubleValue() / total * 100) : 0.0;
        
        Double avgResponseTime = asHost
            ? joinRequestRepository.calculateAverageResponseTimeMinutes(userId)
            : null;
        
        return RequestStatisticsDTO.builder()
            .totalRequests(total)
            .pendingRequests(statusCounts.getOrDefault(RequestStatus.PENDING, 0L))
            .approvedRequests(approved)
            .declinedRequests(statusCounts.getOrDefault(RequestStatus.DECLINED, 0L))
            .cancelledRequests(statusCounts.getOrDefault(RequestStatus.CANCELLED, 0L))
            .expiredRequests(statusCounts.getOrDefault(RequestStatus.EXPIRED, 0L))
            .approvalRate(approvalRate)
            .averageResponseTimeMinutes(avgResponseTime != null ? avgResponseTime.longValue() : null)
            .build();
    }
    
    /**
     * Process expired requests
     */
    @Transactional
    public void processExpiredRequests() {
        log.info("Processing expired requests");
        
        List<JoinRequest> expiredRequests = joinRequestRepository
            .findExpiredRequests(LocalDateTime.now());
        
        for (JoinRequest request : expiredRequests) {
            request.setStatus(RequestStatus.EXPIRED);
            request.setRespondedAt(LocalDateTime.now());
            joinRequestRepository.save(request);
            
            // Send notification
            kafkaProducer.sendRequestExpiredEvent(request);
        }
        
        log.info("Processed {} expired requests", expiredRequests.size());
    }
    
    /**
     * Cleanup old requests
     */
    @Transactional
    public void cleanupOldRequests(int daysToKeep) {
        log.info("Cleaning up requests older than {} days", daysToKeep);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        joinRequestRepository.deleteOldRequests(cutoffDate);
        
        log.info("Old requests cleaned up successfully");
    }
    
    /**
     * Handle approved request (add to event, create group chat)
     */
    private void handleApproval(JoinRequest request) {
        try {
            // Add participant to event
            eventServiceClient.addParticipant(request.getEventId(), request.getRequesterUserId());
            
            // Create or update group chat
            chatServiceClient.addParticipantToEventChat(
                request.getEventId(), 
                request.getRequesterUserId()
            );
            
            // Send approval notification
            kafkaProducer.sendRequestApprovedEvent(request);
            
        } catch (Exception e) {
            log.error("Error handling approval for request {}: {}", 
                request.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Convert entity to DTO
     */
    private JoinRequestDTO convertToDTO(JoinRequest request) {
        return convertToDTO(request, null, null);
    }
    
    private JoinRequestDTO convertToDTO(JoinRequest request, EventDTO event, UserDTO requester) {
        // Fetch data if not provided
        if (event == null) {
            try {
                event = eventServiceClient.getEventById(request.getEventId());
            } catch (Exception e) {
                log.error("Error fetching event: {}", e.getMessage());
            }
        }
        
        if (requester == null) {
            try {
                requester = userServiceClient.getUserById(request.getRequesterUserId());
            } catch (Exception e) {
                log.error("Error fetching requester: {}", e.getMessage());
            }
        }
        
        return JoinRequestDTO.builder()
            .id(request.getId())
            .eventId(request.getEventId())
            .eventTitle(event != null ? event.getTitle() : null)
            .eventHostName(event != null ? event.getHostUsername() : null)
            .requesterUserId(request.getRequesterUserId())
            .requesterUsername(requester != null ? requester.getUsername() : null)
            .requesterProfileImage(requester != null ? requester.getProfileImageUrl() : null)
            .hostUserId(request.getHostUserId())
            .requestMessage(request.getRequestMessage())
            .status(request.getStatus())
            .responseMessage(request.getResponseMessage())
            .requesterRating(request.getRequesterRating())
            .eventMinRating(request.getEventMinRating())
            .isEligible(request.getIsEligible())
            .ineligibilityReason(request.getIneligibilityReason())
            .requestedAt(request.getRequestedAt())
            .respondedAt(request.getRespondedAt())
            .expiresAt(request.getExpiresAt())
            .build();
    }
}