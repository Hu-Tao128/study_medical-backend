package com.studymedical.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp(
            @Value("${FIREBASE_SERVICE_ACCOUNT_PATH:}") String serviceAccountPath,
            @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}") String serviceAccountJson
    ) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(resolveCredentials(serviceAccountPath, serviceAccountJson))
                .build();

        return FirebaseApp.initializeApp(options);
    }

    private GoogleCredentials resolveCredentials(String path, String json) throws IOException {
        if (path != null && !path.isBlank()) {
            try (InputStream in = new FileInputStream(path)) {
                return GoogleCredentials.fromStream(in);
            }
        }
        if (json != null && !json.isBlank()) {
            try (InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(in);
            }
        }
        return GoogleCredentials.getApplicationDefault();
    }
}
