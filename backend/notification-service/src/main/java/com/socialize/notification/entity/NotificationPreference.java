package com.socialize.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationPreference Entity - User notification preferences
 */
@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Email Preferences
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "email_request_received", nullable = false)
    private Boolean emailRequestReceived = true;

    @Column(name = "email_request_approved", nullable = false)
    private Boolean emailRequestApproved = true;

    @Column(name = "email_event_updates", nullable = false)
    private Boolean emailEventUpdates = true;

    // Push Preferences
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "push_request_received", nullable = false)
    private Boolean pushRequestReceived = true;

    @Column(name = "push_request_approved", nullable = false)
    private Boolean pushRequestApproved = true;

    @Column(name = "push_chat_messages", nullable = false)
    private Boolean pushChatMessages = true;

    @Column(name = "push_event_updates", nullable = false)
    private Boolean pushEventUpdates = true;

    // In-app Preferences
    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    // Quiet Hours
    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start")
    private Integer quietHoursStart; // 0-23 (e.g., 22 for 10 PM)

    @Column(name = "quiet_hours_end")
    private Integer quietHoursEnd; // 0-23 (e.g., 8 for 8 AM)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        setDefaults();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Set default values
     */
    private void setDefaults() {
        if (emailEnabled == null) emailEnabled = true;
        if (pushEnabled == null) pushEnabled = true;
        if (inAppEnabled == null) inAppEnabled = true;
        if (quietHoursEnabled == null) quietHoursEnabled = false;
    }

    /**
     * Check if in quiet hours
     */
    public boolean isInQuietHours() {
        if (!quietHoursEnabled || quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        int currentHour = LocalDateTime.now().getHour();
        
        if (quietHoursStart < quietHoursEnd) {
            return currentHour >= quietHoursStart && currentHour < quietHoursEnd;
        } else {
            // Crosses midnight
            return currentHour >= quietHoursStart || currentHour < quietHoursEnd;
        }
    }
}