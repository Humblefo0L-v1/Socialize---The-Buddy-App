package com.socialize.notification.service;

import com.socialize.notification.dto.*;
import com.socialize.notification.entity.Notification;
import com.socialize.notification.entity.Notification.NotificationType;
import com.socialize.notification.entity.NotificationPreference;
import com.socialize.notification.repository.NotificationPreferenceRepository;
import com.socialize.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core Notification Service - Handles all notification logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    private final WebSocketNotificationService webSocketService;

    // ============================================
    // KAFKA EVENT HANDLERS
    // ============================================

    /**
     * Handle request-created event (Host receives join request)
     */
    @Async
    @Transactional
    public void handleRequestCreatedEvent(NotificationEvent event) {
        log.info("Processing REQUEST_CREATED notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        // Create in-app notification
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.REQUEST_RECEIVED,
                event.getReferenceId(),
                "REQUEST"
            );
            notificationRepository.save(notification);
            
            // Send via WebSocket
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
        
        // Send push notification
        if (pref.getPushEnabled() && pref.getPushRequestReceived() && !pref.isInQuietHours()) {
            pushNotificationService.sendPushNotification(event.getUserId(), event.getTitle(), event.getMessage(), event.getData());
        }
        
        // Send email
        if (pref.getEmailEnabled() && pref.getEmailRequestReceived()) {
            emailService.sendRequestReceivedEmail(event);
        }
    }

    /**
     * Handle request-approved event (Requester gets approval)
     */
    @Async
    @Transactional
    public void handleRequestApprovedEvent(NotificationEvent event) {
        log.info("Processing REQUEST_APPROVED notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.REQUEST_APPROVED,
                event.getReferenceId(),
                "REQUEST"
            );
            notificationRepository.save(notification);
            
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
        
        if (pref.getPushEnabled() && pref.getPushRequestApproved() && !pref.isInQuietHours()) {
            pushNotificationService.sendPushNotification(event.getUserId(), event.getTitle(), event.getMessage(), event.getData());
        }
        
        if (pref.getEmailEnabled() && pref.getEmailRequestApproved()) {
            emailService.sendRequestApprovedEmail(event);
        }
    }

    /**
     * Handle request-declined event
     */
    @Async
    @Transactional
    public void handleRequestDeclinedEvent(NotificationEvent event) {
        log.info("Processing REQUEST_DECLINED notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.REQUEST_DECLINED,
                event.getReferenceId(),
                "REQUEST"
            );
            notificationRepository.save(notification);
            
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
        
        if (pref.getPushEnabled() && !pref.isInQuietHours()) {
            pushNotificationService.sendPushNotification(event.getUserId(), event.getTitle(), event.getMessage(), event.getData());
        }
    }

    /**
     * Handle event-created (New event nearby)
     */
    @Async
    @Transactional
    public void handleEventCreatedEvent(NotificationEvent event) {
        log.info("Processing EVENT_CREATED notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.EVENT_CREATED,
                event.getReferenceId(),
                "EVENT"
            );
            notificationRepository.save(notification);
            
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
    }

    /**
     * Handle event-updated
     */
    @Async
    @Transactional
    public void handleEventUpdatedEvent(NotificationEvent event) {
        log.info("Processing EVENT_UPDATED notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.EVENT_UPDATED,
                event.getReferenceId(),
                "EVENT"
            );
            notificationRepository.save(notification);
            
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
        
        if (pref.getPushEnabled() && pref.getPushEventUpdates() && !pref.isInQuietHours()) {
            pushNotificationService.sendPushNotification(event.getUserId(), event.getTitle(), event.getMessage(), event.getData());
        }
        
        if (pref.getEmailEnabled() && pref.getEmailEventUpdates()) {
            emailService.sendEventUpdateEmail(event);
        }
    }

    /**
     * Handle chat-message
     */
    @Async
    @Transactional
    public void handleChatMessageEvent(NotificationEvent event) {
        log.info("Processing CHAT_MESSAGE notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.CHAT_MESSAGE,
                event.getReferenceId(),
                "CHAT"
            );
            notificationRepository.save(notification);
            
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
        
        if (pref.getPushEnabled() && pref.getPushChatMessages() && !pref.isInQuietHours()) {
            pushNotificationService.sendPushNotification(event.getUserId(), event.getTitle(), event.getMessage(), event.getData());
        }
    }

    /**
     * Handle rating-received
     */
    @Async
    @Transactional
    public void handleRatingReceivedEvent(NotificationEvent event) {
        log.info("Processing RATING_RECEIVED notification for user: {}", event.getUserId());
        
        NotificationPreference pref = getOrCreatePreference(event.getUserId());
        
        if (pref.getInAppEnabled()) {
            Notification notification = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage(),
                NotificationType.RATING_RECEIVED,
                event.getReferenceId(),
                "RATING"
            );
            notificationRepository.save(notification);
            
            if (!pref.isInQuietHours()) {
                webSocketService.sendNotificationToUser(event.getUserId(), convertToDTO(notification));
            }
        }
    }

    // ============================================
    // NOTIFICATION CRUD
    // ============================================

    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::convertToDTO);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.markAsRead();
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private Notification createNotification(Long userId, String title, String message, 
                                           NotificationType type, Long referenceId, String referenceType) {
        return Notification.builder()
            .userId(userId)
            .title(title)
            .message(message)
            .type(type)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .isRead(false)
            .build();
    }

    private NotificationPreference getOrCreatePreference(Long userId) {
        return preferenceRepository.findByUserId(userId)
            .orElseGet(() -> {
                NotificationPreference pref = NotificationPreference.builder()
                    .userId(userId)
                    .build();
                return preferenceRepository.save(pref);
            });
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .type(notification.getType().name())
            .referenceId(notification.getReferenceId())
            .referenceType(notification.getReferenceType())
            .isRead(notification.getIsRead())
            .readAt(notification.getReadAt())
            .createdAt(notification.getCreatedAt())
            .data(notification.getData())
            .build();
    }
}