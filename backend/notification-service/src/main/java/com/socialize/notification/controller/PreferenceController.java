package com.socialize.notification.controller;

import com.socialize.notification.dto.NotificationPreferenceDTO;
import com.socialize.notification.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Notification Preference Controller
 * Base Path: /api/notifications/preferences
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    /**
     * Get user preferences
     * GET /api/notifications/preferences
     */
    @GetMapping
    public ResponseEntity<NotificationPreferenceDTO> getPreferences(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceDTO preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update user preferences
     * PUT /api/notifications/preferences
     */
    @PutMapping
    public ResponseEntity<NotificationPreferenceDTO> updatePreferences(
            @RequestBody NotificationPreferenceDTO request,
            Authentication authentication) {
        
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceDTO updated = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Reset to default preferences
     * POST /api/notifications/preferences/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<NotificationPreferenceDTO> resetPreferences(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceDTO reset = preferenceService.resetToDefault(userId);
        return ResponseEntity.ok(reset);
    }
}