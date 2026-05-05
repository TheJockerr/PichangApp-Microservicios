package cl.duoc.pichangapp.notification_service.service;

import cl.duoc.pichangapp.notification_service.dto.BulkNotificationRequestDTO;
import cl.duoc.pichangapp.notification_service.dto.DeviceTokenRequestDTO;
import cl.duoc.pichangapp.notification_service.dto.NotificationRequestDTO;
import cl.duoc.pichangapp.notification_service.dto.NotificationResponseDTO;
import cl.duoc.pichangapp.notification_service.exception.NotificationException;
import cl.duoc.pichangapp.notification_service.model.DeviceToken;
import cl.duoc.pichangapp.notification_service.model.Notification;
import cl.duoc.pichangapp.notification_service.model.NotificationStatus;
import cl.duoc.pichangapp.notification_service.model.NotificationType;
import cl.duoc.pichangapp.notification_service.repository.DeviceTokenRepository;
import cl.duoc.pichangapp.notification_service.repository.NotificationRepository;
import cl.duoc.pichangapp.notification_service.websocket.NotificationWebSocketController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;
    private final NotificationWebSocketController webSocketController;

    /**
     * Envía una notificación a un usuario específico.
     * 1. Persiste la notificación en el historial.
     * 2. Envía push vía FCM a todos los dispositivos del usuario.
     * 3. Envía notificación en tiempo real vía WebSocket.
     */
    @Transactional
    public NotificationResponseDTO sendNotification(NotificationRequestDTO dto) {
        NotificationType type = parseNotificationType(dto.type());

        // Intentar envío FCM a todos los dispositivos del usuario
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(dto.userId());
        boolean anySent = false;

        for (DeviceToken deviceToken : tokens) {
            boolean sent = fcmService.sendPushNotification(deviceToken.getToken(), dto.title(), dto.body());
            if (sent) {
                anySent = true;
            }
        }

        // Si no hay tokens registrados, consideramos que el envío es "exitoso"
        // (la notificación queda en el historial y se envía por WebSocket)
        NotificationStatus status;
        if (tokens.isEmpty() || anySent) {
            status = NotificationStatus.SENT;
        } else {
            status = NotificationStatus.FAILED;
        }

        // Persistir en historial
        Notification notification = Notification.builder()
                .userId(dto.userId())
                .title(dto.title())
                .body(dto.body())
                .type(type)
                .status(status)
                .build();
        notification = notificationRepository.save(notification);

        NotificationResponseDTO responseDTO = buildResponseDTO(notification);

        // Enviar vía WebSocket en tiempo real
        try {
            webSocketController.sendRealTimeNotification(dto.userId(), responseDTO);
            log.info("✅ Notificación WebSocket enviada a userId: {}", dto.userId());
        } catch (Exception e) {
            log.warn("⚠️ Error al enviar notificación WebSocket a userId {}: {}", dto.userId(), e.getMessage());
        }

        return responseDTO;
    }

    /**
     * Envía notificaciones masivas a múltiples usuarios.
     * Retorna la lista de resultados por cada usuario.
     */
    @Transactional
    public List<NotificationResponseDTO> sendBulkNotification(BulkNotificationRequestDTO dto) {
        List<NotificationResponseDTO> results = new ArrayList<>();

        for (String userId : dto.userIds()) {
            NotificationRequestDTO singleDto = new NotificationRequestDTO(
                    userId, dto.title(), dto.body(), dto.type()
            );
            NotificationResponseDTO result = sendNotification(singleDto);
            results.add(result);
        }

        return results;
    }

    /**
     * Consulta el historial de notificaciones de un usuario (paginado).
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotificationHistory(String userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::buildResponseDTO);
    }

    /**
     * Registra o actualiza un token de dispositivo FCM para un usuario.
     * Si el token ya existe, actualiza el userId asociado.
     */
    @Transactional
    public DeviceToken registerDeviceToken(DeviceTokenRequestDTO dto) {
        // Verificar si el token ya existe
        return deviceTokenRepository.findByToken(dto.token())
                .map(existing -> {
                    // Si el token existe pero con otro usuario, actualizar
                    existing.setUserId(dto.userId());
                    return deviceTokenRepository.save(existing);
                })
                .orElseGet(() -> {
                    // Crear nuevo registro
                    DeviceToken newToken = DeviceToken.builder()
                            .userId(dto.userId())
                            .token(dto.token())
                            .build();
                    return deviceTokenRepository.save(newToken);
                });
    }

    /**
     * Elimina un token de dispositivo FCM.
     */
    @Transactional
    public void removeDeviceToken(String token) {
        deviceTokenRepository.findByToken(token)
                .ifPresent(deviceTokenRepository::delete);
    }

    private NotificationType parseNotificationType(String type) {
        try {
            return NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new NotificationException(
                    "Tipo de notificación inválido: " + type + 
                    ". Valores permitidos: KARMA_INCREASE, KARMA_DECREASE, EVENT_REMINDER, EVENT_CANCELLED, NEW_EVENT_NEARBY"
            );
        }
    }

    private NotificationResponseDTO buildResponseDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getType().name(),
                notification.getStatus().name(),
                notification.getCreatedAt()
        );
    }
}
