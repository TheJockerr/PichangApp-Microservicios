package cl.duoc.pichangapp.notification_service.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Servicio de integración con Firebase Cloud Messaging (FCM).
 * Maneja la inicialización del SDK y el envío de push notifications.
 * 
 * Resiliencia: Nunca lanza excepciones al caller. Retorna boolean
 * indicando éxito/fallo. Los errores se loggean internamente.
 */
@Service
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    @Value("${google.application.credentials:}")
    private String credentialsPath;

    @Value("${fcm.project-id:}")
    private String projectId;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void init() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("⚠️ GOOGLE_APPLICATION_CREDENTIALS no configurado. " +
                    "FCM push notifications estarán deshabilitadas. " +
                    "Para habilitar FCM, configura la variable de entorno con la ruta al archivo serviceAccountKey.json");
            return;
        }

        try {
            FileInputStream serviceAccount = new FileInputStream(credentialsPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                log.info("✅ Firebase Admin SDK inicializado correctamente para proyecto: {}", projectId);
            } else {
                firebaseInitialized = true;
                log.info("✅ Firebase Admin SDK ya estaba inicializado");
            }
        } catch (IOException e) {
            log.error("❌ Error al inicializar Firebase Admin SDK: {}", e.getMessage());
            log.warn("⚠️ FCM push notifications estarán deshabilitadas hasta reiniciar con credenciales válidas.");
        }
    }

    /**
     * Envía una push notification a un dispositivo específico vía FCM.
     *
     * @param deviceToken Token FCM del dispositivo destino
     * @param title       Título de la notificación
     * @param body        Cuerpo del mensaje
     * @return true si se envió exitosamente, false si falló
     */
    public boolean sendPushNotification(String deviceToken, String title, String body) {
        if (!firebaseInitialized) {
            log.warn("⚠️ Firebase no inicializado. No se puede enviar push notification a token: {}", 
                    deviceToken.substring(0, Math.min(10, deviceToken.length())) + "...");
            return false;
        }

        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ Push notification enviada exitosamente. Response: {}", response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("❌ Error al enviar push notification vía FCM: {} - {}", 
                    e.getMessagingErrorCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar push notification: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Indica si Firebase está inicializado y listo para enviar notificaciones.
     */
    public boolean isFirebaseInitialized() {
        return firebaseInitialized;
    }
}
