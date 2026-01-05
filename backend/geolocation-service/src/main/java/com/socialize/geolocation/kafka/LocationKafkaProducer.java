package com.socialize.geolocation.kafka;

import com.socialize.geolocation.model.entity.UserLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationKafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.location-updates}")
    private String locationUpdatesTopic;
    
    @Value("${kafka.topics.location-batch-updates}")
    private String batchUpdatesTopic;
    
    /**
     * Send location update event
     */
    public void sendLocationUpdate(Long userId, UserLocation location) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId);
        event.put("latitude", location.getLatitude());
        event.put("longitude", location.getLongitude());
        event.put("accuracy", location.getAccuracy());
        event.put("timestamp", location.getTimestamp());
        
        kafkaTemplate.send(locationUpdatesTopic, userId.toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Location update sent to Kafka for user: {}", userId);
                } else {
                    log.error("Failed to send location update to Kafka: {}", 
                        ex.getMessage());
                }
            });
    }
    
    /**
     * Send batch update event
     */
    public void sendBatchLocationUpdate(Long userId, int count) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId);
        event.put("locationCount", count);
        event.put("timestamp", System.currentTimeMillis());
        
        kafkaTemplate.send(batchUpdatesTopic, userId.toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Batch update event sent for user: {} ({} locations)", 
                        userId, count);
                } else {
                    log.error("Failed to send batch update event: {}", ex.getMessage());
                }
            });
    }
}
