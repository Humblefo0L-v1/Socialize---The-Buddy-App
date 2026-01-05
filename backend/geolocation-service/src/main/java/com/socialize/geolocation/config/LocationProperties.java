package com.socialize.geolocation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "location")
@Data
public class LocationProperties {
    private Integer defaultSearchRadius = 10000;
    private Integer maxSearchRadius = 50000;
    private Integer updateInterval = 30;
    private Integer historyRetentionDays = 30;
    private Integer accuracyThreshold = 100;
    private Integer batchSize = 100;
}