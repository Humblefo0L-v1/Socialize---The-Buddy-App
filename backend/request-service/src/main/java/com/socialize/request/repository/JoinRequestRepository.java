package com.socialize.request.repository;

import com.socialize.request.model.entity.JoinRequest;
import com.socialize.request.model.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    
    /**
     * Find request by event and requester
     */
    Optional<JoinRequest> findByEventIdAndRequesterUserId(Long eventId, Long requesterUserId);
    
    /**
     * Check if user already has a request for event
     */
    boolean existsByEventIdAndRequesterUserIdAndStatusIn(
        Long eventId, 
        Long requesterUserId, 
        List<RequestStatus> statuses
    );
    
    /**
     * Find all requests for an event
     */
    Page<JoinRequest> findByEventIdOrderByRequestedAtDesc(Long eventId, Pageable pageable);
    
    /**
     * Find pending requests for an event
     */
    List<JoinRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);
    
    /**
     * Find all requests sent by a user
     */
    Page<JoinRequest> findByRequesterUserIdOrderByRequestedAtDesc(
        Long requesterUserId, Pageable pageable);
    
    /**
     * Find requests received by host (for a specific event)
     */
    Page<JoinRequest> findByHostUserIdAndEventIdOrderByRequestedAtDesc(
        Long hostUserId, Long eventId, Pageable pageable);
    
    /**
     * Find all requests received by host
     */
    Page<JoinRequest> findByHostUserIdOrderByRequestedAtDesc(
        Long hostUserId, Pageable pageable);
    
    /**
     * Find pending requests received by host
     */
    @Query("SELECT jr FROM JoinRequest jr WHERE jr.hostUserId = :hostUserId " +
           "AND jr.status = 'PENDING' ORDER BY jr.requestedAt DESC")
    List<JoinRequest> findPendingRequestsByHost(@Param("hostUserId") Long hostUserId);
    
    /**
     * Find requests by status
     */
    List<JoinRequest> findByStatusOrderByRequestedAtDesc(RequestStatus status);
    
    /**
     * Count pending requests for a user
     */
    @Query("SELECT COUNT(jr) FROM JoinRequest jr WHERE jr.requesterUserId = :userId " +
           "AND jr.status = 'PENDING'")
    Long countPendingRequestsByUser(@Param("userId") Long userId);
    
    /**
     * Count pending requests for an event
     */
    Long countByEventIdAndStatus(Long eventId, RequestStatus status);
    
    /**
     * Find expired requests that need to be auto-declined
     */
    @Query("SELECT jr FROM JoinRequest jr WHERE jr.status = 'PENDING' " +
           "AND jr.expiresAt IS NOT NULL AND jr.expiresAt < :now")
    List<JoinRequest> findExpiredRequests(@Param("now") LocalDateTime now);
    
    /**
     * Update request status
     */
    @Modifying
    @Query("UPDATE JoinRequest jr SET jr.status = :status, jr.respondedAt = :respondedAt " +
           "WHERE jr.id = :requestId")
    void updateRequestStatus(
        @Param("requestId") Long requestId,
        @Param("status") RequestStatus status,
        @Param("respondedAt") LocalDateTime respondedAt
    );
    
    /**
     * Bulk approve/decline requests
     */
    @Modifying
    @Query("UPDATE JoinRequest jr SET jr.status = :status, jr.respondedAt = :respondedAt, " +
           "jr.responseMessage = :message WHERE jr.id IN :requestIds")
    void bulkUpdateRequestStatus(
        @Param("requestIds") List<Long> requestIds,
        @Param("status") RequestStatus status,
        @Param("respondedAt") LocalDateTime respondedAt,
        @Param("message") String message
    );
    
    /**
     * Get request statistics for user
     */
    @Query("SELECT jr.status, COUNT(jr) FROM JoinRequest jr " +
           "WHERE jr.requesterUserId = :userId GROUP BY jr.status")
    List<Object[]> getRequestStatisticsByUser(@Param("userId") Long userId);
    
    /**
     * Get request statistics for event
     */
    @Query("SELECT jr.status, COUNT(jr) FROM JoinRequest jr " +
           "WHERE jr.eventId = :eventId GROUP BY jr.status")
    List<Object[]> getRequestStatisticsByEvent(@Param("eventId") Long eventId);
    
    /**
     * Find requests by multiple statuses
     */
    @Query("SELECT jr FROM JoinRequest jr WHERE jr.hostUserId = :hostUserId " +
           "AND jr.status IN :statuses ORDER BY jr.requestedAt DESC")
    List<JoinRequest> findByHostUserIdAndStatusIn(
        @Param("hostUserId") Long hostUserId,
        @Param("statuses") List<RequestStatus> statuses
    );
    
    /**
     * Calculate average response time for host
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, jr.requestedAt, jr.respondedAt)) " +
           "FROM JoinRequest jr WHERE jr.hostUserId = :hostUserId " +
           "AND jr.respondedAt IS NOT NULL")
    Double calculateAverageResponseTimeMinutes(@Param("hostUserId") Long hostUserId);
    
    /**
     * Find requests that need attention (pending for too long)
     */
    @Query("SELECT jr FROM JoinRequest jr WHERE jr.status = 'PENDING' " +
           "AND jr.requestedAt < :cutoffTime ORDER BY jr.requestedAt ASC")
    List<JoinRequest> findStaleRequests(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Delete old requests (cleanup)
     */
    @Modifying
    @Query("DELETE FROM JoinRequest jr WHERE jr.requestedAt < :cutoffDate " +
           "AND jr.status NOT IN ('PENDING', 'APPROVED')")
    void deleteOldRequests(@Param("cutoffDate") LocalDateTime cutoffDate);
}