package com.socialize.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config-file}")
    private Resource firebaseConfigFile;

    @Value("${firebase.enabled}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.warn("Firebase push notifications are DISABLED. Set FIREBASE_ENABLED=true to enable.");
            return;
        }

        try {
            if (!firebaseConfigFile.exists()) {
                log.error("Firebase config file not found: {}", firebaseConfigFile.getFilename());
                log.warn("Push notifications will not work. Place firebase-service-account.json in src/main/resources/");
                return;
            }

            InputStream serviceAccount = firebaseConfigFile.getInputStream();
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK initialized successfully!");
            }
        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage(), e);
            log.warn("Push notifications will not work.");
        }
    }
}
