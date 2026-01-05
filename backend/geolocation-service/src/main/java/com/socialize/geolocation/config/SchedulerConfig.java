package com.socialize.geolocation.config;

import com.socialize.geolocation.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
    
    private final LocationService locationService;
    
    @Value("${location.history-retention-days}")
    private int retentionDays;
    
    /**
     * Cleanup old locations daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLocations() {
        log.info("Starting scheduled cleanup of old locations");
        locationService.cleanupOldLocations(retentionDays);
        log.info("Scheduled cleanup completed");
    }
}