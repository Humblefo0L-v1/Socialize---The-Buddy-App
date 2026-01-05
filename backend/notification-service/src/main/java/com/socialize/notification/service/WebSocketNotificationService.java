package com.socialize.notification.service;

import com.socialize.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * WebSocket Notification Service - Real-time notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${notification.websocket.enabled}")
    private boolean websocketEnabled;

    /**
     * Send notification to specific user via WebSocket
     * Destination: /user/{userId}/queue/notifications
     */
    @Async
    public void sendNotificationToUser(Long userId, NotificationDTO notification) {
        if (!websocketEnabled) {
            log.debug("WebSocket notifications disabled");
            return;
        }

        try {
            String destination = "/user/" + userId + "/queue/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            log.info("WebSocket notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send notification count update
     */
    @Async
    public void sendUnreadCountUpdate(Long userId, Long unreadCount) {
        if (!websocketEnabled) return;

        try {
            String destination = "/user/" + userId + "/queue/unread-count";
            messagingTemplate.convertAndSend(destination, unreadCount);
            log.debug("Unread count updated for user: {} - Count: {}", userId, unreadCount);
        } catch (Exception e) {
            log.error("Failed to send unread count update: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast to all connected users
     */
    @Async
    public void broadcastToAll(Object message) {
        if (!websocketEnabled) return;

        try {
            messagingTemplate.convertAndSend("/topic/notifications", message);
            log.info("Broadcast notification sent to all users");
        } catch (Exception e) {
            log.error("Failed to broadcast notification: {}", e.getMessage(), e);
        }
    }
}