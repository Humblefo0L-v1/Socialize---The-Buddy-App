package com.socialize.geolocation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "geofences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Geofence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point centerPoint;
    
    @Column(nullable = false)
    private Double radius; // in meters
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private GeofenceType type;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastTriggeredAt;
}

enum GeofenceType {
    EVENT,
    HOME,
    WORK,
    CUSTOM
}