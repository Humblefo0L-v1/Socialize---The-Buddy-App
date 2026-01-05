package com.socialize.geolocation.repository;

import com.socialize.geolocation.model.entity.UserLocation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class CustomLocationRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final GeometryFactory geometryFactory = 
        new GeometryFactory(new PrecisionModel(), 4326);
    
    /**
     * Find nearby users with detailed information
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findNearbyUsersDetailed(
            Double latitude, 
            Double longitude, 
            Double radius, 
            Long currentUserId) {
        
        String sql = """
            SELECT 
                ul.user_id,
                ul.latitude,
                ul.longitude,
                ul.timestamp,
                ST_Distance_Sphere(
                    ul.location_point,
                    ST_GeomFromText(CONCAT('POINT(', :lng, ' ', :lat, ')'), 4326)
                ) as distance
            FROM user_locations ul
            WHERE ul.is_current = true
            AND ul.user_id != :currentUserId
            AND ST_Distance_Sphere(
                ul.location_point,
                ST_GeomFromText(CONCAT('POINT(', :lng, ' ', :lat, ')'), 4326)
            ) <= :radius
            ORDER BY distance
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("lat", latitude);
        query.setParameter("lng", longitude);
        query.setParameter("radius", radius);
        query.setParameter("currentUserId", currentUserId);
        
        return query.getResultList();
    }
    
    /**
     * Find users within polygon (for complex area searches)
     */
    @SuppressWarnings("unchecked")
    public List<UserLocation> findUsersInPolygon(List<Coordinate> coordinates) {
        // This is for future advanced features
        // For now, return empty list
        return List.of();
    }
    
    /**
     * Create Point from coordinates
     */
    public Point createPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
    
    /**
     * Calculate bearing between two points (direction)
     */
    public Double calculateBearing(
            Double lat1, Double lon1, 
            Double lat2, Double lon2) {
        
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        
        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
    
    /**
     * Get spatial statistics for a user
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getUserLocationStatistics(Long userId, int days) {
        String sql = """
            SELECT 
                DATE(timestamp) as date,
                COUNT(*) as location_updates,
                AVG(accuracy) as avg_accuracy,
                MIN(accuracy) as min_accuracy,
                MAX(accuracy) as max_accuracy
            FROM user_locations
            WHERE user_id = :userId
            AND timestamp >= DATE_SUB(NOW(), INTERVAL :days DAY)
            GROUP BY DATE(timestamp)
            ORDER BY date DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("days", days);
        
        return query.getResultList();
    }
}