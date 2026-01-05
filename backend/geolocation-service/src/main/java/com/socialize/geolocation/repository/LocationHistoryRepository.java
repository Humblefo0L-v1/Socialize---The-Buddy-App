package com.socialize.geolocation.repository;

import com.socialize.geolocation.model.entity.LocationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    
    /**
     * Find location history for a user with pagination
     */
    Page<LocationHistory> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    /**
     * Find location history within time range
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.userId = :userId " +
           "AND lh.timestamp BETWEEN :startTime AND :endTime ORDER BY lh.timestamp DESC")
    List<LocationHistory> findByUserIdAndTimestampBetween(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Calculate total distance traveled (simplified)
     */
    @Query(value = """
        SELECT SUM(
            ST_Distance_Sphere(
                lh1.location_point,
                lh2.location_point
            )
        ) / 1000 as total_distance_km
        FROM location_history lh1
        INNER JOIN location_history lh2 ON lh1.user_id = lh2.user_id
        WHERE lh1.user_id = :userId
        AND lh1.timestamp > lh2.timestamp
        AND lh1.id = (
            SELECT MIN(id) FROM location_history 
            WHERE user_id = :userId AND timestamp > lh2.timestamp
        )
        AND lh1.timestamp BETWEEN :startTime AND :endTime
        """, nativeQuery = true)
    Double calculateTotalDistanceTraveled(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Delete old history records
     */
    @Modifying
    @Query("DELETE FROM LocationHistory lh WHERE lh.timestamp < :cutoffDate")
    void deleteOldHistory(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get latest location from history
     */
    Optional<LocationHistory> findFirstByUserIdOrderByTimestampDesc(Long userId);
}