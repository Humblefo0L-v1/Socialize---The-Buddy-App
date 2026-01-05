package com.socialize.geolocation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_history", indexes = {
    @Index(name = "idx_history_user_id", columnList = "user_id"),
    @Index(name = "idx_history_timestamp", columnList = "timestamp"),
    @Index(name = "idx_history_user_time", columnList = "user_id, timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column(name = "location_point", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point locationPoint;
    
    @Column(nullable = false)
    private Double accuracy;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 50)
    private String activityType; // STATIONARY, WALKING, DRIVING, etc.
}
