package com.socialize.geolocation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_locations", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_is_current", columnList = "is_current"),
    @Index(name = "idx_user_current", columnList = "user_id, is_current"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Spatial point for geographic queries
    @Column(name = "location_point", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point locationPoint;

    private Double accuracy; // in meters
    private Double altitude; // in meters
    private Double speed;    // in m/s
    private Double heading;  // in degrees (0-360)

    @Builder.Default
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 100)
    private String deviceId;

    @Column(length = 50)
    private String provider; // GPS, NETWORK, etc.
}