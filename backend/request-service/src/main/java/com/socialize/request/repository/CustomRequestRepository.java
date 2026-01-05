package com.socialize.request.repository;

import com.socialize.request.model.entity.JoinRequest;
import com.socialize.request.model.entity.RequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Slf4j
public class CustomRequestRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Find requests with complex filters
     */
    @SuppressWarnings("unchecked")
    public List<JoinRequest> findRequestsWithFilters(
            Long userId,
            Long eventId,
            RequestStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Boolean asHost) {
        
        StringBuilder sql = new StringBuilder("SELECT jr FROM JoinRequest jr WHERE 1=1");
        
        if (asHost) {
            sql.append(" AND jr.hostUserId = :userId");
        } else {
            sql.append(" AND jr.requesterUserId = :userId");
        }
        
        if (eventId != null) {
            sql.append(" AND jr.eventId = :eventId");
        }
        
        if (status != null) {
            sql.append(" AND jr.status = :status");
        }
        
        if (fromDate != null) {
            sql.append(" AND jr.requestedAt >= :fromDate");
        }
        
        if (toDate != null) {
            sql.append(" AND jr.requestedAt <= :toDate");
        }
        
        sql.append(" ORDER BY jr.requestedAt DESC");
        
        Query query = entityManager.createQuery(sql.toString());
        query.setParameter("userId", userId);
        
        if (eventId != null) query.setParameter("eventId", eventId);
        if (status != null) query.setParameter("status", status);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        
        return query.getResultList();
    }
    
    /**
     * Get detailed request statistics
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getDetailedStatistics(Long userId, boolean asHost) {
        String sql = """
            SELECT 
                DATE(jr.requested_at) as date,
                jr.status,
                COUNT(*) as count,
                AVG(TIMESTAMPDIFF(MINUTE, jr.requested_at, jr.responded_at)) as avg_response_time
            FROM join_requests jr
            WHERE %s = :userId
            AND jr.requested_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY DATE(jr.requested_at), jr.status
            ORDER BY date DESC
            """.formatted(asHost ? "jr.host_user_id" : "jr.requester_user_id");
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        
        return query.getResultList();
    }
}