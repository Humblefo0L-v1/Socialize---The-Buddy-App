package com.socialize.geolocation.service;

import com.socialize.geolocation.client.EventServiceClient;
import com.socialize.geolocation.client.UserServiceClient;
import com.socialize.geolocation.exception.LocationNotFoundException;
import com.socialize.geolocation.kafka.LocationKafkaProducer;
import com.socialize.geolocation.model.dto.*;
import com.socialize.geolocation.model.entity.LocationHistory;
import com.socialize.geolocation.model.entity.UserLocation;
import com.socialize.geolocation.repository.CustomLocationRepository;
import com.socialize.geolocation.repository.LocationHistoryRepository;
import com.socialize.geolocation.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    
    private final UserLocationRepository userLocationRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final CustomLocationRepository customLocationRepository;
    private final LocationKafkaProducer kafkaProducer;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final DistanceCalculator distanceCalculator;
    
    /**
     * Update user location
     */
    @Transactional
    @CacheEvict(value = "userLocations", key = "#userId")
    public LocationDTO updateLocation(Long userId, LocationUpdateRequest request) {
        log.info("Updating location for user: {}", userId);
        
        // Mark previous locations as not current
        userLocationRepository.markPreviousLocationsAsNotCurrent(userId);
        
        // Create Point geometry
        Point point = customLocationRepository.createPoint(
            request.getLatitude(), 
            request.getLongitude()
        );
        
        // Create new location
        UserLocation location = UserLocation.builder()
            .userId(userId)
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .locationPoint(point)
            .accuracy(request.getAccuracy())
            .altitude(request.getAltitude())
            .speed(request.getSpeed())
            .heading(request.getHeading())
            .isCurrent(true)
            .deviceId(request.getDeviceId())
            .provider(request.getProvider())
            .build();
        
        UserLocation savedLocation = userLocationRepository.save(location);
        
        // Save to history
        saveToHistory(savedLocation);
        
        // Send to Kafka
        kafkaProducer.sendLocationUpdate(userId, savedLocation);
        
        log.info("Location updated successfully for user: {}", userId);
        return convertToLocationDTO(savedLocation);
    }
    
    /**
     * Get current location for a user
     */
    @Cacheable(value = "userLocations", key = "#userId")
    public LocationDTO getCurrentLocation(Long userId) {
        log.info("Fetching current location for user: {}", userId);
        
        UserLocation location = userLocationRepository
            .findByUserIdAndIsCurrentTrue(userId)
            .orElseThrow(() -> new LocationNotFoundException(
                "No current location found for user: " + userId));
        
        return convertToLocationDTO(location);
    }
    
    /**
     * Find nearby buddies
     */
    public List<NearbyUserDTO> findNearbyBuddies(
            Long currentUserId,
            Double latitude,
            Double longitude,
            Double radius) {
        
        log.info("Finding nearby buddies for user {} within {} meters", 
            currentUserId, radius);
        
        List<Object[]> nearbyUsersData = customLocationRepository
            .findNearbyUsersDetailed(latitude, longitude, radius, currentUserId);
        
        List<NearbyUserDTO> nearbyUsers = new ArrayList<>();
        
        for (Object[] data : nearbyUsersData) {
            Long userId = ((Number) data[0]).longValue();
            Double lat = (Double) data[1];
            Double lon = (Double) data[2];
            LocalDateTime timestamp = (LocalDateTime) data[3];
            Double distance = (Double) data[4];
            
            // Fetch user details from User Service
            try {
                var userDetails = userServiceClient.getUserById(userId);
                
                // Check if user has active events
                boolean hasActiveEvents = eventServiceClient
                    .hasActiveEvents(userId);
                
                NearbyUserDTO nearbyUser = NearbyUserDTO.builder()
                    .userId(userId)
                    .username(userDetails.username())
                    .profileImageUrl(userDetails.profileImageUrl())
                    .latitude(lat)
                    .longitude(lon)
                    .distance(distance)
                    .lastUpdated(timestamp)
                    .hasActiveEvents(hasActiveEvents)
                    .build();
                
                nearbyUsers.add(nearbyUser);
            } catch (Exception e) {
                log.error("Error fetching user details for userId: {}", userId, e);
            }
        }
        
        return nearbyUsers;
    }
    
    /**
     * Find nearby events
     */
    public List<NearbyEventDTO> findNearbyEvents(
            Double latitude,
            Double longitude,
            Double radius) {
        
        log.info("Finding nearby events within {} meters", radius);
        
        try {
            // Call Event Service to get nearby events
            List<NearbyEventDTO> nearbyEvents = eventServiceClient
                .getNearbyEvents(latitude, longitude, radius);
            
            // Calculate distances
            for (NearbyEventDTO event : nearbyEvents) {
                Double distance = distanceCalculator.calculateDistance(
                    latitude, longitude,
                    event.getLatitude(), event.getLongitude()
                );
                event.setDistance(distance);
            }
            
            // Sort by distance
            nearbyEvents.sort((e1, e2) -> 
                Double.compare(e1.getDistance(), e2.getDistance()));
            
            return nearbyEvents;
            
        } catch (Exception e) {
            log.error("Error fetching nearby events", e);
            return List.of();
        }
    }
    
    /**
     * Get location history
     */
    public Page<LocationHistoryDTO> getLocationHistory(
            Long userId, int page, int size) {
        
        log.info("Fetching location history for user: {}", userId);
        
        Page<LocationHistory> historyPage = locationHistoryRepository
            .findByUserIdOrderByTimestampDesc(userId, PageRequest.of(page, size));
        
        return historyPage.map(this::convertToHistoryDTO);
    }
    
    /**
     * Calculate distance between two points
     */
    public DistanceCalculationResponse calculateDistance(
            DistanceCalculationRequest request) {
        
        Double distanceInMeters = distanceCalculator.calculateDistance(
            request.getFromLatitude(),
            request.getFromLongitude(),
            request.getToLatitude(),
            request.getToLongitude()
        );
        
        return DistanceCalculationResponse.builder()
            .distance(distanceInMeters)
            .distanceInKm(distanceInMeters / 1000.0)
            .distanceInMiles(distanceInMeters / 1609.34)
            .build();
    }
    
    /**
     * Batch update locations
     */
    @Transactional
    public void batchUpdateLocations(Long userId, List<LocationUpdateRequest> locations) {
        log.info("Batch updating {} locations for user: {}", locations.size(), userId);
        
        for (LocationUpdateRequest request : locations) {
            updateLocation(userId, request);
        }
        
        // Send batch update event to Kafka
        kafkaProducer.sendBatchLocationUpdate(userId, locations.size());
    }
    
    /**
     * Get user location summary
     */
    public UserLocationSummary getUserLocationSummary(Long userId) {
        log.info("Fetching location summary for user: {}", userId);
        
        LocationDTO currentLocation = getCurrentLocation(userId);
        
        // Count nearby buddies
        Long nearbyBuddiesCount = userLocationRepository.countNearbyUsers(
            String.format("POINT(%f %f)", 
                currentLocation.getLongitude(), 
                currentLocation.getLatitude()),
            10000.0, // 10km
            userId
        );
        
        // Get total distance traveled (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Double totalDistance = locationHistoryRepository
            .calculateTotalDistanceTraveled(userId, sevenDaysAgo, LocalDateTime.now());
        
        return UserLocationSummary.builder()
            .userId(userId)
            .currentLocation(currentLocation)
            .lastUpdated(currentLocation.getTimestamp())
            .nearbyBuddiesCount(nearbyBuddiesCount.intValue())
            .nearbyEventsCount(0) // TODO: Implement
            .totalDistanceTraveled(totalDistance != null ? totalDistance : 0.0)
            .build();
    }
    
    /**
     * Delete old locations (cleanup)
     */
    @Transactional
    public void cleanupOldLocations(int daysToKeep) {
        log.info("Cleaning up locations older than {} days", daysToKeep);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        userLocationRepository.deleteOldLocations(cutoffDate);
        locationHistoryRepository.deleteOldHistory(cutoffDate);
        
        log.info("Old locations cleaned up successfully");
    }
    
    /**
     * Save location to history
     */
    private void saveToHistory(UserLocation location) {
        LocationHistory history = LocationHistory.builder()
            .userId(location.getUserId())
            .latitude(location.getLatitude())
            .longitude(location.getLongitude())
            .locationPoint(location.getLocationPoint())
            .accuracy(location.getAccuracy())
            .timestamp(location.getTimestamp())
            .build();
        
        locationHistoryRepository.save(history);
    }
    
    /**
     * Convert entity to DTO
     */
    private LocationDTO convertToLocationDTO(UserLocation location) {
        return LocationDTO.builder()
            .id(location.getId())
            .userId(location.getUserId())
            .latitude(location.getLatitude())
            .longitude(location.getLongitude())
            .accuracy(location.getAccuracy())
            .altitude(location.getAltitude())
            .speed(location.getSpeed())
            .heading(location.getHeading())
            .isCurrent(location.getIsCurrent())
            .timestamp(location.getTimestamp())
            .deviceId(location.getDeviceId())
            .provider(location.getProvider())
            .build();
    }
    
    private LocationHistoryDTO convertToHistoryDTO(LocationHistory history) {
        return LocationHistoryDTO.builder()
            .id(history.getId())
            .userId(history.getUserId())
            .latitude(history.getLatitude())
            .longitude(history.getLongitude())
            .accuracy(history.getAccuracy())
            .timestamp(history.getTimestamp())
            .activityType(history.getActivityType())
            .build();
    }
}