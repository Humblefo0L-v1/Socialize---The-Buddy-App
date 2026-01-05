package com.socialize.notification.kafka;

import com.socialize.notification.dto.NotificationEvent;
import com.socialize.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka Listener for all notification events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaListener {

    private final NotificationService notificationService;

    /**
     * Listen to request-created topic (Join request sent)
     */
    @KafkaListener(
        topics = "request-created",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRequestCreated(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleRequestCreatedEvent(event);
        } catch (Exception e) {
            log.error("Error processing request-created event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to request-approved topic
     */
    @KafkaListener(
        topics = "request-approved",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRequestApproved(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleRequestApprovedEvent(event);
        } catch (Exception e) {
            log.error("Error processing request-approved event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to request-declined topic
     */
    @KafkaListener(
        topics = "request-declined",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRequestDeclined(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleRequestDeclinedEvent(event);
        } catch (Exception e) {
            log.error("Error processing request-declined event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to event-created topic
     */
    @KafkaListener(
        topics = "event-created",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEventCreated(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleEventCreatedEvent(event);
        } catch (Exception e) {
            log.error("Error processing event-created event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to event-updated topic
     */
    @KafkaListener(
        topics = "event-updated",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEventUpdated(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleEventUpdatedEvent(event);
        } catch (Exception e) {
            log.error("Error processing event-updated event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to chat-message topic
     */
    @KafkaListener(
        topics = "chat-message",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleChatMessage(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleChatMessageEvent(event);
        } catch (Exception e) {
            log.error("Error processing chat-message event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to rating-received topic
     */
    @KafkaListener(
        topics = "rating-received",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRatingReceived(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received event from topic: {} - Event Type: {}", topic, event.getEventType());
        try {
            notificationService.handleRatingReceivedEvent(event);
        } catch (Exception e) {
            log.error("Error processing rating-received event: {}", e.getMessage(), e);
        }
    }
}