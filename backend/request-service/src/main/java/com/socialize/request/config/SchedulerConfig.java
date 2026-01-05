package com.socialize.request.config;

import com.socialize.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
    
    private final RequestService requestService;
    
    /**
     * Process expired requests every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processExpiredRequests() {
        log.info("Starting scheduled task: process expired requests");
        requestService.processExpiredRequests();
        log.info("Completed scheduled task: process expired requests");
    }
    
    /**
     * Cleanup old requests daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldRequests() {
        log.info("Starting scheduled task: cleanup old requests");
        requestService.cleanupOldRequests(90); // Keep last 90 days
        log.info("Completed scheduled task: cleanup old requests");
    }
}