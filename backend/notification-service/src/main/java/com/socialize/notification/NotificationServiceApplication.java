package com.socialize.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Notification Service - Handles all notification types
 * - Push Notifications (Firebase): Check
 * - Email Notifications: TODO 
 * - In-app Notifications: Check
 * - Real-time WebSocket notifications: check   
 * 
 * Kafka Topics Consumed:
 * - request-created (Join request sent)
 * - request-approved (Request approved)
 * - request-declined (Request declined)
 * - event-created (New event)
 * - event-updated (Event changes)
 * - chat-message (New chat messages)
 * - rating-received (New rating)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}