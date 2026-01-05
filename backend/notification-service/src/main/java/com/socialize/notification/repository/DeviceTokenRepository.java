package com.socialize.notification.repository;

import com.socialize.notification.entity.DeviceToken;
import com.socialize.notification.entity.DeviceToken.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    List<DeviceToken> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<DeviceToken> findByToken(String token);
    
    boolean existsByToken(String token);
    
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.userId = :userId AND dt.deviceId = :deviceId")
    void deactivateByUserIdAndDeviceId(Long userId, String deviceId);
    
    @Modifying
    @Query("DELETE FROM DeviceToken dt WHERE dt.lastUsedAt < :cutoffDate")
    void deleteInactiveTokens(LocalDateTime cutoffDate);
    
    Long countByUserIdAndIsActiveTrue(Long userId);
}