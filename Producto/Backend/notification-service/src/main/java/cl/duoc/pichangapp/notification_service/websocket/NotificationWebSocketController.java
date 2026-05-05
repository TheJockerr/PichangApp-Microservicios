package cl.duoc.pichangapp.notification_service.websocket;

import cl.duoc.pichangapp.notification_service.dto.NotificationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controlador WebSocket para enviar notificaciones en tiempo real
 * a usuarios suscritos vía STOMP.
 */
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación en tiempo real al usuario suscrito
     * en /topic/notifications/{userId}
     */
    public void sendRealTimeNotification(String userId, NotificationResponseDTO notification) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}
