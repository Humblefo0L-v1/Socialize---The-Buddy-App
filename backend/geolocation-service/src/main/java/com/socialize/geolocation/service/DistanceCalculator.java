package com.socialize.geolocation.service;

import org.springframework.stereotype.Component;

@Component
public class DistanceCalculator {
    
    private static final int EARTH_RADIUS = 6371000; // meters
    
    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in meters
     */
    public Double calculateDistance(
            Double lat1, Double lon1, 
            Double lat2, Double lon2) {
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * 
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Check if point is within radius of center point
     */
    public boolean isWithinRadius(
            Double centerLat, Double centerLon,
            Double pointLat, Double pointLon,
            Double radius) {
        
        Double distance = calculateDistance(
            centerLat, centerLon, 
            pointLat, pointLon
        );
        
        return distance <= radius;
    }
    
    /**
     * Calculate bearing (direction) between two points
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
}