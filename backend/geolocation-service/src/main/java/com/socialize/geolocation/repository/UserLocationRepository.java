package com.socialize.geolocation.repository;

import com.socialize.geolocation.model.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    
    /**
     * Find current location for a user
     */
    Optional<UserLocation> findByUserIdAndIsCurrentTrue(Long userId);
    
    /**
     * Find all current locations
     */
    List<UserLocation> findByIsCurrentTrue();
    
    /**
     * Find locations by user IDs
     */
    @Query("SELECT ul FROM UserLocation ul WHERE ul.userId IN :userIds AND ul.isCurrent = true")
    List<UserLocation> findCurrentLocationsByUserIds(@Param("userIds") List<Long> userIds);
    
    /**
     * Find nearby users within radius using spatial query
     * Uses ST_Distance_Sphere for accurate distance calculation
     */
    @Query(value = """
        SELECT ul.*, 
               ST_Distance_Sphere(ul.location_point, ST_GeomFromText(:point, 4326)) as distance
        FROM user_locations ul
        WHERE ul.is_current = true
        AND ul.user_id != :userId
        AND ST_Distance_Sphere(ul.location_point, ST_GeomFromText(:point, 4326)) <= :radius
        ORDER BY distance
        """, nativeQuery = true)
    List<Object[]> findNearbyUsers(
        @Param("point") String point,
        @Param("radius") Double radius,
        @Param("userId") Long userId
    );
    
    /**
     * Find users within bounding box (faster initial filter)
     */
    @Query(value = """
        SELECT ul.*
        FROM user_locations ul
        WHERE ul.is_current = true
        AND ul.user_id != :userId
        AND MBRContains(
            ST_GeomFromText(:boundingBox, 4326),
            ul.location_point
        )
        """, nativeQuery = true)
    List<UserLocation> findUsersInBoundingBox(
        @Param("boundingBox") String boundingBox,
        @Param("userId") Long userId
    );
    
    /**
     * Count nearby users
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM user_locations ul
        WHERE ul.is_current = true
        AND ul.user_id != :userId
        AND ST_Distance_Sphere(ul.location_point, ST_GeomFromText(:point, 4326)) <= :radius
        """, nativeQuery = true)
    Long countNearbyUsers(
        @Param("point") String point,
        @Param("radius") Double radius,
        @Param("userId") Long userId
    );
    
    /**
     * Set previous locations to not current
     */
    @Modifying
    @Query("UPDATE UserLocation ul SET ul.isCurrent = false WHERE ul.userId = :userId AND ul.isCurrent = true")
    void markPreviousLocationsAsNotCurrent(@Param("userId") Long userId);
    
    /**
     * Delete old locations
     */
    @Modifying
    @Query("DELETE FROM UserLocation ul WHERE ul.timestamp < :cutoffDate AND ul.isCurrent = false")
    void deleteOldLocations(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find locations within time range
     */
    @Query("SELECT ul FROM UserLocation ul WHERE ul.userId = :userId " +
           "AND ul.timestamp BETWEEN :startTime AND :endTime ORDER BY ul.timestamp DESC")
    List<UserLocation> findLocationsByUserAndTimeRange(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
