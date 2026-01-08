package com.datn.discover_service.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

// @Configuration
// public class FirebaseConfig {

//     // destroyMethod="" để Spring KHÔNG tự gọi close() cho Firestore
//     @Bean(destroyMethod = "")
//     public Firestore firestore() throws IOException {

//         // Devtools / restart nóng có thể để lại FirebaseApp cũ (Firestore đã bị close)
//         // => delete hết app cũ để init mới sạch sẽ
//         for (FirebaseApp app : FirebaseApp.getApps()) {
//             try {
//                 app.delete();
//             } catch (Exception ignored) {}
//         }

//         try (InputStream serviceAccount = new ClassPathResource("firebase-service.json").getInputStream()) {
//             FirebaseOptions options = FirebaseOptions.builder()
//                     .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                     .build();
//             FirebaseApp.initializeApp(options);
//         }

//         return FirestoreClient.getFirestore();
//     }
// }
// //Path=/api/discover/**,/api/search/**,/api/profile/**,/api/follow/**,/api/plans/**
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initFirebase() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                logger.info("Firebase already initialized");
                return;
            }

            String firebaseJson = System.getenv("firebase-service");

            if (firebaseJson == null || firebaseJson.isBlank()) {
                throw new IllegalStateException(
                        "FIREBASE_SERVICE_ACCOUNT env variable not found"
                );
            }

            ByteArrayInputStream serviceAccount =
                    new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("✓ Firebase initialized successfully");

        } catch (Exception e) {
            logger.error("✗ Firebase initialization failed", e);
        }
    }

    @Bean
    public Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new IllegalStateException(
                    "FirebaseApp is not initialized. Firestore unavailable."
            );
        }
        return FirestoreClient.getFirestore();
    }
}
