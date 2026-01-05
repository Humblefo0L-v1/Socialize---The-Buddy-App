package com.socialize.notification.controller;

import com.socialize.notification.dto.NotificationDTO;
import com.socialize.notification.dto.UnreadCountDTO;
import com.socialize.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Notification REST Controller
 * Base Path: /api/notifications
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get user notifications (paginated)
     * GET /api/notifications?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountDTO> getUnreadCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Long count = notificationService.getUnreadCount(userId);
        
        UnreadCountDTO dto = UnreadCountDTO.builder()
            .userId(userId)
            .unreadCount(count)
            .build();
        
        return ResponseEntity.ok(dto);
    }

    /**
     * Mark notification as read
     * PUT /api/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        Long userId = Long.parseLong(authentication.getName());
        notificationService.markAsRead(notificationId, userId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read
     * PUT /api/notifications/mark-all-read
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Health check
     * GET /api/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running!");
    }
}