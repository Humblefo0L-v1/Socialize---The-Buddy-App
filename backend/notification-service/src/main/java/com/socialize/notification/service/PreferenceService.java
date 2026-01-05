package com.socialize.notification.service;

import com.socialize.notification.dto.NotificationPreferenceDTO;
import com.socialize.notification.entity.NotificationPreference;
import com.socialize.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notification Preference Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * Get user preferences
     */
    public NotificationPreferenceDTO getUserPreferences(Long userId) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        return convertToDTO(pref);
    }

    /**
     * Update user preferences
     */
    @Transactional
    public NotificationPreferenceDTO updatePreferences(Long userId, NotificationPreferenceDTO dto) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));

        // Update fields
        if (dto.getEmailEnabled() != null) pref.setEmailEnabled(dto.getEmailEnabled());
        if (dto.getEmailRequestReceived() != null) pref.setEmailRequestReceived(dto.getEmailRequestReceived());
        if (dto.getEmailRequestApproved() != null) pref.setEmailRequestApproved(dto.getEmailRequestApproved());
        if (dto.getEmailEventUpdates() != null) pref.setEmailEventUpdates(dto.getEmailEventUpdates());
        
        if (dto.getPushEnabled() != null) pref.setPushEnabled(dto.getPushEnabled());
        if (dto.getPushRequestReceived() != null) pref.setPushRequestReceived(dto.getPushRequestReceived());
        if (dto.getPushRequestApproved() != null) pref.setPushRequestApproved(dto.getPushRequestApproved());
        if (dto.getPushChatMessages() != null) pref.setPushChatMessages(dto.getPushChatMessages());
        if (dto.getPushEventUpdates() != null) pref.setPushEventUpdates(dto.getPushEventUpdates());
        
        if (dto.getInAppEnabled() != null) pref.setInAppEnabled(dto.getInAppEnabled());
        
        if (dto.getQuietHoursEnabled() != null) pref.setQuietHoursEnabled(dto.getQuietHoursEnabled());
        if (dto.getQuietHoursStart() != null) pref.setQuietHoursStart(dto.getQuietHoursStart());
        if (dto.getQuietHoursEnd() != null) pref.setQuietHoursEnd(dto.getQuietHoursEnd());

        NotificationPreference saved = preferenceRepository.save(pref);
        log.info("Updated notification preferences for user: {}", userId);
        return convertToDTO(saved);
    }

    /**
     * Reset to default preferences
     */
    @Transactional
    public NotificationPreferenceDTO resetToDefault(Long userId) {
        preferenceRepository.findByUserId(userId).ifPresent(preferenceRepository::delete);
        NotificationPreference defaultPref = createDefaultPreference(userId);
        log.info("Reset notification preferences to default for user: {}", userId);
        return convertToDTO(defaultPref);
    }

    /**
     * Create default preference
     */
    private NotificationPreference createDefaultPreference(Long userId) {
        NotificationPreference pref = NotificationPreference.builder()
            .userId(userId)
            .emailEnabled(true)
            .emailRequestReceived(true)
            .emailRequestApproved(true)
            .emailEventUpdates(true)
            .pushEnabled(true)
            .pushRequestReceived(true)
            .pushRequestApproved(true)
            .pushChatMessages(true)
            .pushEventUpdates(true)
            .inAppEnabled(true)
            .quietHoursEnabled(false)
            .build();
        return preferenceRepository.save(pref);
    }

    /**
     * Convert to DTO
     */
    private NotificationPreferenceDTO convertToDTO(NotificationPreference pref) {
        return NotificationPreferenceDTO.builder()
            .userId(pref.getUserId())
            .emailEnabled(pref.getEmailEnabled())
            .emailRequestReceived(pref.getEmailRequestReceived())
            .emailRequestApproved(pref.getEmailRequestApproved())
            .emailEventUpdates(pref.getEmailEventUpdates())
            .pushEnabled(pref.getPushEnabled())
            .pushRequestReceived(pref.getPushRequestReceived())
            .pushRequestApproved(pref.getPushRequestApproved())
            .pushChatMessages(pref.getPushChatMessages())
            .pushEventUpdates(pref.getPushEventUpdates())
            .inAppEnabled(pref.getInAppEnabled())
            .quietHoursEnabled(pref.getQuietHoursEnabled())
            .quietHoursStart(pref.getQuietHoursStart())
            .quietHoursEnd(pref.getQuietHoursEnd())
            .build();
    }
}