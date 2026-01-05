package com.socialize.notification.service;

import com.socialize.notification.dto.DeviceTokenDTO;
import com.socialize.notification.dto.DeviceTokenRequest;
import com.socialize.notification.entity.DeviceToken;
import com.socialize.notification.entity.DeviceToken.DeviceType;
import com.socialize.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Device Token Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Register device token
     */
    @Transactional
    public DeviceTokenDTO registerDevice(Long userId, DeviceTokenRequest request) {
        // Check if token already exists
        DeviceToken token = deviceTokenRepository.findByToken(request.getToken())
            .orElse(null);

        if (token != null) {
            // Update existing token
            token.setUserId(userId);
            token.setDeviceId(request.getDeviceId());
            token.setDeviceName(request.getDeviceName());
            token.setIsActive(true);
            token.updateLastUsed();
        } else {
            // Create new token
            token = DeviceToken.builder()
                .userId(userId)
                .token(request.getToken())
                .deviceType(DeviceType.valueOf(request.getDeviceType().toUpperCase()))
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .isActive(true)
                .build();
        }

        DeviceToken saved = deviceTokenRepository.save(token);
        log.info("Registered device token for user: {} - Device: {}", userId, request.getDeviceType());
        return convertToDTO(saved);
    }

    /**
     * Get user's devices
     */
    public List<DeviceTokenDTO> getUserDevices(Long userId) {
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Deactivate device
     */
    @Transactional
    public void deactivateDevice(Long tokenId, Long userId) {
        deviceTokenRepository.findById(tokenId).ifPresent(token -> {
            if (token.getUserId().equals(userId)) {
                token.deactivate();
                deviceTokenRepository.save(token);
                log.info("Deactivated device token: {} for user: {}", tokenId, userId);
            }
        });
    }

    /**
     * Deactivate all user devices
     */
    @Transactional
    public void deactivateAllUserDevices(Long userId) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
        tokens.forEach(DeviceToken::deactivate);
        deviceTokenRepository.saveAll(tokens);
        log.info("Deactivated all devices for user: {}", userId);
    }

    /**
     * Convert to DTO
     */
    private DeviceTokenDTO convertToDTO(DeviceToken token) {
        return DeviceTokenDTO.builder()
            .id(token.getId())
            .userId(token.getUserId())
            .deviceType(token.getDeviceType().name())
            .deviceId(token.getDeviceId())
            .deviceName(token.getDeviceName())
            .isActive(token.getIsActive())
            .lastUsedAt(token.getLastUsedAt())
            .createdAt(token.getCreatedAt())
            .build();
    }
}