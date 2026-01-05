package com.socialize.geolocation.health;

import com.socialize.geolocation.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationServiceHealthIndicator implements HealthIndicator {
    
    private final UserLocationRepository userLocationRepository;
    
    @Override
    public Health health() {
        try {
            long count = userLocationRepository.count();
            return Health.up()
                    .withDetail("total_locations", count)
                    .withDetail("status", "Location service is operational")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}