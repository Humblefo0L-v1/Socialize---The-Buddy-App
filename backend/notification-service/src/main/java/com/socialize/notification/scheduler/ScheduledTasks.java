package com.socialize.notification.scheduler;

import com.socialize.notification.repository.DeviceTokenRepository;
import com.socialize.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled Tasks for Notification Service
 * - Clean old notifications
 * - Clean inactive device tokens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${notification.in-app.retention-days}")
    private int retentionDays;

    /**
     * Clean old notifications
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanOldNotifications() {
        log.info("Starting cleanup of old notifications...");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            notificationRepository.deleteOldNotifications(cutoffDate);
            
            log.info("✅ Successfully cleaned notifications older than {} days", retentionDays);
        } catch (Exception e) {
            log.error("❌ Failed to clean old notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean inactive device tokens
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanInactiveDeviceTokens() {
        log.info("Starting cleanup of inactive device tokens...");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90); // 90 days inactive
            deviceTokenRepository.deleteInactiveTokens(cutoffDate);
            
            log.info("✅ Successfully cleaned device tokens inactive for 90+ days");
        } catch (Exception e) {
            log.error("❌ Failed to clean inactive tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Health check log
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void healthCheck() {
        log.debug("Notification Service is running... ✓");
    }
}