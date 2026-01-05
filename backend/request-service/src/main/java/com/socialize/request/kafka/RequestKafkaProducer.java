package com.socialize.request.kafka;

import com.socialize.request.model.dto.EventDTO;
import com.socialize.request.model.dto.UserDTO;
import com.socialize.request.model.entity.JoinRequest;
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
public class RequestKafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.request-created}")
    private String requestCreatedTopic;
    
    @Value("${kafka.topics.request-approved}")
    private String requestApprovedTopic;
    
    @Value("${kafka.topics.request-declined}")
    private String requestDeclinedTopic;
    
    @Value("${kafka.topics.request-cancelled}")
    private String requestCancelledTopic;
    
    /**
     * Send request created event
     */
    public void sendRequestCreatedEvent(JoinRequest request, EventDTO event, UserDTO requester) {
        Map<String, Object> payload = buildEventPayload(request, event, requester);
        
        kafkaTemplate.send(requestCreatedTopic, request.getId().toString(), payload)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Request created event sent: requestId={}", request.getId());
                } else {
                    log.error("Failed to send request created event: {}", ex.getMessage());
                }
            });
    }
    
    /**
     * Send request approved event
     */
    public void sendRequestApprovedEvent(JoinRequest request) {
        Map<String, Object> payload = buildBasicPayload(request);
        
        kafkaTemplate.send(requestApprovedTopic, request.getId().toString(), payload)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Request approved event sent: requestId={}", request.getId());
                } else {
                    log.error("Failed to send request approved event: {}", ex.getMessage());
                }
            });
    }
    
    /**
     * Send request declined event
     */
    public void sendRequestDeclinedEvent(JoinRequest request) {
        Map<String, Object> payload = buildBasicPayload(request);
        
        kafkaTemplate.send(requestDeclinedTopic, request.getId().toString(), payload)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Request declined event sent: requestId={}", request.getId());
                } else {
                    log.error("Failed to send request declined event: {}", ex.getMessage());
                }
            });
    }
    
    /**
     * Send request cancelled event
     */
    public void sendRequestCancelledEvent(JoinRequest request) {
        Map<String, Object> payload = buildBasicPayload(request);
        
        kafkaTemplate.send(requestCancelledTopic, request.getId().toString(), payload)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Request cancelled event sent: requestId={}", request.getId());
                } else {
                    log.error("Failed to send request cancelled event: {}", ex.getMessage());
                }
            });
    }
    
    /**
     * Send request expired event
     */
    public void sendRequestExpiredEvent(JoinRequest request) {
        Map<String, Object> payload = buildBasicPayload(request);
        payload.put("expired", true);
        
        kafkaTemplate.send(requestDeclinedTopic, request.getId().toString(), payload)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Request expired event sent: requestId={}", request.getId());
                } else {
                    log.error("Failed to send request expired event: {}", ex.getMessage());
                }
            });
    }
    
    private Map<String, Object> buildBasicPayload(JoinRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", request.getId());
        payload.put("eventId", request.getEventId());
        payload.put("requesterUserId", request.getRequesterUserId());
        payload.put("hostUserId", request.getHostUserId());
        payload.put("status", request.getStatus().toString());
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }
    
    private Map<String, Object> buildEventPayload(
            JoinRequest request, EventDTO event, UserDTO requester) {
        Map<String, Object> payload = buildBasicPayload(request);
        payload.put("eventTitle", event.getTitle());
        payload.put("eventHostName", event.getHostUsername());
        payload.put("requesterUsername", requester.getUsername());
        payload.put("requesterRating", request.getRequesterRating());
        payload.put("requestMessage", request.getRequestMessage());
        return payload;
    }
}