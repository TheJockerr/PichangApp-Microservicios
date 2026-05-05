package cl.duoc.pichangapp.notification_service.controller;

import cl.duoc.pichangapp.notification_service.dto.BulkNotificationRequestDTO;
import cl.duoc.pichangapp.notification_service.dto.DeviceTokenRequestDTO;
import cl.duoc.pichangapp.notification_service.dto.NotificationRequestDTO;
import cl.duoc.pichangapp.notification_service.dto.NotificationResponseDTO;
import cl.duoc.pichangapp.notification_service.model.DeviceToken;
import cl.duoc.pichangapp.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Registrar o actualizar un token de dispositivo FCM.
     * POST /api/v1/notifications/device-token
     */
    @PostMapping("/device-token")
    public ResponseEntity<DeviceToken> registerDeviceToken(@RequestBody DeviceTokenRequestDTO dto) {
        DeviceToken token = notificationService.registerDeviceToken(dto);
        return ResponseEntity.ok(token);
    }

    /**
     * Eliminar un token de dispositivo FCM.
     * DELETE /api/v1/notifications/device-token/{token}
     */
    @DeleteMapping("/device-token/{token}")
    public ResponseEntity<Void> removeDeviceToken(@PathVariable String token) {
        notificationService.removeDeviceToken(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enviar una notificación a un usuario específico.
     * POST /api/v1/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDTO> sendNotification(@RequestBody NotificationRequestDTO dto) {
        NotificationResponseDTO response = notificationService.sendNotification(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Enviar notificaciones masivas a múltiples usuarios.
     * POST /api/v1/notifications/send-bulk
     */
    @PostMapping("/send-bulk")
    public ResponseEntity<List<NotificationResponseDTO>> sendBulkNotification(@RequestBody BulkNotificationRequestDTO dto) {
        List<NotificationResponseDTO> responses = notificationService.sendBulkNotification(dto);
        return ResponseEntity.ok(responses);
    }

    /**
     * Consultar historial de notificaciones de un usuario (paginado).
     * GET /api/v1/notifications/{userId}?page=0&size=20
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> history = notificationService.getNotificationHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }
}
