package com.socialize.notification.service;

import com.socialize.notification.entity.DeviceToken;
import com.socialize.notification.repository.DeviceTokenRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Push Notification Service - Firebase Cloud Messaging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${notification.push.enabled}")
    private boolean pushEnabled;

    @Value("${notification.push.batch-size}")
    private int batchSize;

    /**
     * Send push notification to a user (all devices)
     */
    @Async
    public void sendPushNotification(Long userId, String title, String body, Map<String, Object> data) {
        if (!pushEnabled) {
            log.debug("Push notifications disabled");
            return;
        }

        try {
            List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
            
            if (tokens.isEmpty()) {
                log.debug("No active device tokens for user: {}", userId);
                return;
            }

            List<String> tokenStrings = tokens.stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());

            sendToMultipleDevices(tokenStrings, title, body, data);
            
            log.info("Push notification sent to {} devices for user: {}", tokens.size(), userId);
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send push notification to specific device token
     */
    @Async
    public void sendToDevice(String token, String title, String body, Map<String, Object> data) {
        if (!pushEnabled) return;

        try {
            Message message = buildMessage(token, title, body, data);
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message to device: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification: {}", e.getMessage());
            handleFirebaseException(token, e);
        }
    }

    /**
     * Send to multiple devices (batch)
     */
    @Async
    public void sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, Object> data) {
        if (!pushEnabled || tokens.isEmpty()) return;

        try {
            MulticastMessage message = buildMulticastMessage(tokens, title, body, data);
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            
            log.info("Successfully sent {} messages, {} failures", 
                response.getSuccessCount(), response.getFailureCount());

            // Handle failed tokens
            if (response.getFailureCount() > 0) {
                handleBatchFailures(tokens, response);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send batch notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Build single message
     */
    private Message buildMessage(String token, String title, String body, Map<String, Object> data) {
        Message.Builder builder = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build());

        // Add data payload
        if (data != null && !data.isEmpty()) {
            Map<String, String> stringData = data.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> String.valueOf(e.getValue())
                ));
            builder.putAllData(stringData);
        }

        // Android specific config
        builder.setAndroidConfig(AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder()
                .setSound("default")
                .setColor("#4CAF50")
                .build())
            .build());

        // iOS specific config
        builder.setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder()
                .setSound("default")
                .setBadge(1)
                .build())
            .build());

        return builder.build();
    }

    /**
     * Build multicast message
     */
    private MulticastMessage buildMulticastMessage(List<String> tokens, String title, String body, Map<String, Object> data) {
        MulticastMessage.Builder builder = MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build());

        if (data != null && !data.isEmpty()) {
            Map<String, String> stringData = data.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> String.valueOf(e.getValue())
                ));
            builder.putAllData(stringData);
        }

        builder.setAndroidConfig(AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .build());

        builder.setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder()
                .setSound("default")
                .build())
            .build());

        return builder.build();
    }

    /**
     * Handle Firebase exceptions
     */
    private void handleFirebaseException(String token, FirebaseMessagingException e) {
        String errorCode = e.getMessagingErrorCode() != null ? 
            e.getMessagingErrorCode().name() : "UNKNOWN";
        
        log.error("Firebase error code: {} for token: {}", errorCode, token);

        // Deactivate invalid tokens
        if ("INVALID_ARGUMENT".equals(errorCode) || 
            "UNREGISTERED".equals(errorCode) ||
            "SENDER_ID_MISMATCH".equals(errorCode)) {
            
            deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
                deviceToken.deactivate();
                deviceTokenRepository.save(deviceToken);
                log.info("Deactivated invalid token: {}", token);
            });
        }
    }

    /**
     * Handle batch failures
     */
    private void handleBatchFailures(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                String token = tokens.get(i);
                Exception exception = responses.get(i).getException();
                
                if (exception instanceof FirebaseMessagingException) {
                    handleFirebaseException(token, (FirebaseMessagingException) exception);
                } else {
                    log.error("Failed to send to token {}: {}", token, exception.getMessage());
                }
            }
        }
    }
}