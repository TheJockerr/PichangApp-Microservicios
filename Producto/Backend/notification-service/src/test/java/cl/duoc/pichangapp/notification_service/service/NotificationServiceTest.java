package cl.duoc.pichangapp.notification_service.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private FcmService fcmService;

    @Mock
    private NotificationWebSocketController webSocketController;

    @InjectMocks
    private NotificationService notificationService;

    private final String USER_ID = "user123";
    private DeviceToken mockDeviceToken;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockDeviceToken = DeviceToken.builder()
                .id(1)
                .userId(USER_ID)
                .token("fcm-token-abc123")
                .createdAt(Instant.now())
                .build();

        mockNotification = Notification.builder()
                .id(1)
                .userId(USER_ID)
                .title("Test Title")
                .body("Test Body")
                .type(NotificationType.KARMA_INCREASE)
                .status(NotificationStatus.SENT)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testSendNotification_Success() {
        // Arrange
        NotificationRequestDTO dto = new NotificationRequestDTO(
                USER_ID, "¡Tu karma subió!", "+10 puntos por asistir al evento", "KARMA_INCREASE"
        );
        when(deviceTokenRepository.findByUserId(USER_ID)).thenReturn(List.of(mockDeviceToken));
        when(fcmService.sendPushNotification(anyString(), anyString(), anyString())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        // Act
        NotificationResponseDTO response = notificationService.sendNotification(dto);

        // Assert
        assertNotNull(response);
        assertEquals(USER_ID, response.userId());
        assertEquals("SENT", response.status());
        verify(fcmService, times(1)).sendPushNotification(
                eq("fcm-token-abc123"), anyString(), anyString()
        );
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketController, times(1)).sendRealTimeNotification(eq(USER_ID), any());
    }

    @Test
    void testSendNotification_FcmFails_StatusIsFailed() {
        // Arrange: FCM falla pero NO se lanza excepción al caller
        NotificationRequestDTO dto = new NotificationRequestDTO(
                USER_ID, "Tu karma bajó", "-15 por no asistir al evento", "KARMA_DECREASE"
        );
        when(deviceTokenRepository.findByUserId(USER_ID)).thenReturn(List.of(mockDeviceToken));
        when(fcmService.sendPushNotification(anyString(), anyString(), anyString())).thenReturn(false);
        
        Notification failedNotification = Notification.builder()
                .id(2)
                .userId(USER_ID)
                .title("Tu karma bajó")
                .body("-15 por no asistir al evento")
                .type(NotificationType.KARMA_DECREASE)
                .status(NotificationStatus.FAILED)
                .createdAt(Instant.now())
                .build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(failedNotification);

        // Act — NO debe lanzar excepción
        NotificationResponseDTO response = notificationService.sendNotification(dto);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.status());
        verify(fcmService, times(1)).sendPushNotification(anyString(), anyString(), anyString());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendNotification_NoDeviceTokens_StatusIsSent() {
        // Arrange: usuario sin tokens FCM registrados
        NotificationRequestDTO dto = new NotificationRequestDTO(
                USER_ID, "Evento cerca", "Hay un nuevo evento cerca de ti", "NEW_EVENT_NEARBY"
        );
        when(deviceTokenRepository.findByUserId(USER_ID)).thenReturn(List.of());
        
        Notification sentNotification = Notification.builder()
                .id(3)
                .userId(USER_ID)
                .title("Evento cerca")
                .body("Hay un nuevo evento cerca de ti")
                .type(NotificationType.NEW_EVENT_NEARBY)
                .status(NotificationStatus.SENT)
                .createdAt(Instant.now())
                .build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(sentNotification);

        // Act
        NotificationResponseDTO response = notificationService.sendNotification(dto);

        // Assert: SENT porque no hay tokens que fallen
        assertNotNull(response);
        assertEquals("SENT", response.status());
        verify(fcmService, never()).sendPushNotification(anyString(), anyString(), anyString());
    }

    @Test
    void testSendNotification_InvalidType_ThrowsException() {
        // Arrange
        NotificationRequestDTO dto = new NotificationRequestDTO(
                USER_ID, "Test", "Test body", "INVALID_TYPE"
        );

        // Act & Assert
        assertThrows(NotificationException.class, () -> {
            notificationService.sendNotification(dto);
        });
    }

    @Test
    void testRegisterDeviceToken_NewToken() {
        // Arrange
        DeviceTokenRequestDTO dto = new DeviceTokenRequestDTO(USER_ID, "new-fcm-token");
        when(deviceTokenRepository.findByToken("new-fcm-token")).thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken);

        // Act
        DeviceToken result = notificationService.registerDeviceToken(dto);

        // Assert
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(deviceTokenRepository, times(1)).save(any(DeviceToken.class));
    }

    @Test
    void testRegisterDeviceToken_ExistingToken_UpdatesUserId() {
        // Arrange
        DeviceTokenRequestDTO dto = new DeviceTokenRequestDTO("user456", "fcm-token-abc123");
        when(deviceTokenRepository.findByToken("fcm-token-abc123")).thenReturn(Optional.of(mockDeviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken);

        // Act
        DeviceToken result = notificationService.registerDeviceToken(dto);

        // Assert
        assertNotNull(result);
        verify(deviceTokenRepository, times(1)).save(any(DeviceToken.class));
    }

    @Test
    void testGetNotificationHistory_Paginated() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(List.of(mockNotification), pageable, 1);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID, pageable)).thenReturn(page);

        // Act
        Page<NotificationResponseDTO> result = notificationService.getNotificationHistory(USER_ID, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(USER_ID, result.getContent().get(0).userId());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(USER_ID, pageable);
    }
}
