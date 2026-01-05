package com.socialize.notification.controller;

import com.socialize.notification.dto.DeviceTokenDTO;
import com.socialize.notification.dto.DeviceTokenRequest;
import com.socialize.notification.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Device Token Controller
 * Base Path: /api/notifications/devices
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/devices")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    /**
     * Register device token
     * POST /api/notifications/devices
     */
    @PostMapping
    public ResponseEntity<DeviceTokenDTO> registerDevice(
            @Valid @RequestBody DeviceTokenRequest request,
            Authentication authentication) {
        
        Long userId = Long.parseLong(authentication.getName());
        DeviceTokenDTO token = deviceTokenService.registerDevice(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    /**
     * Get user's device tokens
     * GET /api/notifications/devices
     */
    @GetMapping
    public ResponseEntity<List<DeviceTokenDTO>> getDevices(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<DeviceTokenDTO> tokens = deviceTokenService.getUserDevices(userId);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Deactivate device token
     * DELETE /api/notifications/devices/{tokenId}
     */
    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Void> deactivateDevice(
            @PathVariable Long tokenId,
            Authentication authentication) {
        
        Long userId = Long.parseLong(authentication.getName());
        deviceTokenService.deactivateDevice(tokenId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivate all user devices
     * DELETE /api/notifications/devices
     */
    @DeleteMapping
    public ResponseEntity<Void> deactivateAllDevices(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        deviceTokenService.deactivateAllUserDevices(userId);
        return ResponseEntity.noContent().build();
    }
}