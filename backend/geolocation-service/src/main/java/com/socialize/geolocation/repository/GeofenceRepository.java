package com.socialize.geolocation.repository;

import com.socialize.geolocation.model.entity.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
    
    /**
     * Find active geofences for a user
     */
    List<Geofence> findByUserIdAndIsActiveTrue(Long userId);
    
    /**
     * Find geofences that contain a point
     */
    @Query(value = """
        SELECT * FROM geofences g
        WHERE g.is_active = true
        AND ST_Distance_Sphere(
            g.center_point,
            ST_GeomFromText(:point, 4326)
        ) <= g.radius
        """, nativeQuery = true)
    List<Geofence> findGeofencesContainingPoint(@Param("point") String point);
}