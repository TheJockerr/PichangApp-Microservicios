# Notification Service

Este microservicio forma parte del ecosistema de PichangApp. Se encarga de gestionar las **notificaciones** hacia los usuarios a través de dos canales:

- **Push Notifications** vía Firebase Cloud Messaging (FCM) para alertas en segundo plano.
- **Notificaciones en tiempo real** vía WebSockets + STOMP para alertas instantáneas dentro de la app.

## Tecnologías

- Java 17
- Spring Boot 3.2.6
- Spring Data JPA
- Spring Security + JWT
- Spring WebSocket + STOMP
- Firebase Admin SDK 9.2.0
- MySQL 8
- Swagger/OpenAPI 3

## Configuración Local (Laragon)

1. Ejecutar Laragon e iniciar MySQL.
2. Ejecutar el script SQL proporcionado en `sql/init_notification.sql` para crear la base de datos y las tablas.
   ```bash
   mysql -u root < sql/init_notification.sql
   ```
3. Las credenciales por defecto en `application.properties` asumen el usuario `root` sin contraseña en `localhost:3306`.
4. **(Opcional)** Para habilitar FCM push notifications:
   - Descarga el archivo `serviceAccountKey.json` desde la consola de Firebase.
   - Configura la variable de entorno:
     ```bash
     set GOOGLE_APPLICATION_CREDENTIALS=C:\ruta\al\serviceAccountKey.json
     ```
   - Si no se configura, el servicio funcionará sin push (solo WebSocket + historial).
5. Ejecutar el proyecto:
   ```bash
   ./mvnw spring-boot:run
   ```
6. El servicio estará disponible en `http://localhost:8082`.

## Variables de Entorno

| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8082` | Puerto del servidor |
| `DATABASE_URL` | `jdbc:mysql://localhost:3306/pichangapp_notifications?useSSL=false&serverTimezone=UTC` | URL de conexión MySQL |
| `DB_USERNAME` | `root` | Usuario de la base de datos |
| `DB_PASSWORD` | *(vacío)* | Contraseña de la base de datos |
| `JWT_SECRET` | *(dev default)* | Clave secreta para firmar/verificar JWT |
| `FCM_PROJECT_ID` | `pichangapp-fcm` | ID del proyecto Firebase |
| `GOOGLE_APPLICATION_CREDENTIALS` | *(vacío)* | Ruta al archivo serviceAccountKey.json |

## Endpoints REST

Todos los endpoints requieren autenticación JWT (header `Authorization: Bearer <token>`).

### 1. Registrar Token de Dispositivo FCM
`POST /api/v1/notifications/device-token`

Registra o actualiza un token FCM. Un usuario puede tener múltiples tokens (varios dispositivos).
```json
{
  "userId": "1",
  "token": "fcm-device-token-abc123..."
}
```

### 2. Eliminar Token de Dispositivo
`DELETE /api/v1/notifications/device-token/{token}`

Elimina un token FCM cuando el usuario cierra sesión o desinstala la app.

### 3. Enviar Notificación a un Usuario
`POST /api/v1/notifications/send`

Envía una notificación push + WebSocket a un usuario específico.
```json
{
  "userId": "1",
  "title": "¡Tu karma subió!",
  "body": "+10 puntos por asistir al evento",
  "type": "KARMA_INCREASE"
}
```

### 4. Enviar Notificación Masiva
`POST /api/v1/notifications/send-bulk`

Envía notificaciones a múltiples usuarios (ej: recordatorio de evento).
```json
{
  "userIds": ["1", "2", "3"],
  "title": "Recordatorio de evento",
  "body": "Recuerda que tienes un evento en 1 hora",
  "type": "EVENT_REMINDER"
}
```

### 5. Consultar Historial de Notificaciones
`GET /api/v1/notifications/{userId}?page=0&size=20`

Retorna el historial de notificaciones del usuario, paginado y ordenado por fecha descendente.

## Tipos de Notificación

| Tipo | Descripción |
|---|---|
| `KARMA_INCREASE` | El karma del usuario subió |
| `KARMA_DECREASE` | El karma del usuario bajó |
| `EVENT_REMINDER` | Recordatorio de evento próximo |
| `EVENT_CANCELLED` | Evento cancelado por el organizador |
| `NEW_EVENT_NEARBY` | Nuevo evento cerca del usuario |

## WebSocket (Tiempo Real)

### Conexión STOMP
- **Endpoint:** `ws://localhost:8082/ws`
- **Protocolo:** STOMP sobre WebSocket con SockJS fallback

### Suscripción
```
SUBSCRIBE /topic/notifications/{userId}
```

### Autenticación
Enviar el token JWT en el header STOMP durante el `CONNECT`:
```
CONNECT
Authorization: Bearer <jwt-token>
```

## Interacción con Otros Microservicios

El notification-service es un **consumidor pasivo**. Los otros microservicios lo invocan vía HTTP:

```
karma_service → POST /api/v1/notifications/send (con JWT)
events_service → POST /api/v1/notifications/send-bulk (con JWT)
```

El notification-service **nunca** llama a otros servicios por iniciativa propia.

## Swagger

Documentación interactiva disponible en:
```
http://localhost:8082/swagger-ui.html
```

## Pruebas

Para ejecutar las pruebas unitarias:
```bash
./mvnw test
```

Cobertura de tests:
- ✅ Envío exitoso de notificación (FCM + WebSocket + historial)
- ✅ Fallo de FCM controlado (status FAILED, sin excepción propagada)
- ✅ Envío sin tokens FCM registrados (status SENT, solo WebSocket)
- ✅ Tipo de notificación inválido (NotificationException)
- ✅ Registro de nuevo token de dispositivo
- ✅ Actualización de token existente
- ✅ Consulta de historial paginado
