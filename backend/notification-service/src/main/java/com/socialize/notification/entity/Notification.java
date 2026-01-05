package com.socialize.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification Entity - Stores in-app notifications
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_is_read", columnList = "is_read")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "reference_id")
    private Long referenceId; // Event ID, Request ID, etc.

    @Column(name = "reference_type", length = 50)
    private String referenceType; // EVENT, REQUEST, RATING, CHAT

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "data", columnDefinition = "JSON")
    private String data; // Additional JSON data

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Notification Types
     */
    public enum NotificationType {
        REQUEST_RECEIVED,      // Join request received
        REQUEST_APPROVED,      // Your request approved
        REQUEST_DECLINED,      // Your request declined
        EVENT_CREATED,         // New event nearby
        EVENT_UPDATED,         // Event you joined updated
        EVENT_CANCELLED,       // Event cancelled
        CHAT_MESSAGE,          // New chat message
        RATING_RECEIVED,       // New rating received
        BUDDY_JOINED,          // New buddy joined event
        EVENT_REMINDER,        // Event starting soon
        SYSTEM                 // System notifications
    }
}