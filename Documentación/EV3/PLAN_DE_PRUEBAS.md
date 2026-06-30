# Plan de Pruebas — PichangApp

## Tabla de Contenidos
- [1. Introducción](#1-introducción)
- [2. Estrategia de Pruebas](#2-estrategia-de-pruebas)
- [3. Pruebas Unitarias por Microservicio](#3-pruebas-unitarias-por-microservicio)
- [4. Pruebas de Integración](#4-pruebas-de-integración)
- [5. Pruebas de Seguridad](#5-pruebas-de-seguridad)
- [6. Pruebas de Negocio Críticas](#6-pruebas-de-negocio-críticas)
- [7. Matriz de Trazabilidad](#7-matriz-de-trazabilidad)
- [8. Criterios de Aceptación](#8-criterios-de-aceptación)
- [9. Defectos Encontrados y Resueltos](#9-defectos-encontrados-y-resueltos)
- [10. Conclusiones](#10-conclusiones)

## 1. Introducción
El presente Plan de Pruebas establece el enfoque, los recursos y la programación de las actividades de testing para el proyecto PichangApp. El objetivo principal es asegurar que los microservicios backend y la aplicación móvil cumplan con los requerimientos funcionales y no funcionales, garantizando estabilidad, seguridad y el correcto cálculo del sistema de reputación (Karma).

## 2. Estrategia de Pruebas
Se implementó una estrategia en múltiples niveles:
- **Pruebas Unitarias:** Ejecutadas con JUnit 5 y Mockito para aislar componentes (Services) y validar lógica de negocio (ej: cálculo de karma, reglas de inscripción).
- **Pruebas de Integración:** Utilizando Spring Boot Test (`@SpringBootTest`) para verificar la comunicación con la base de datos (H2/MySQL).
- **Pruebas Manuales y de API:** Colecciones completas en Postman documentadas y ejecutadas contra el entorno de producción (Railway).
- **Pruebas de Aceptación End-to-End:** Simulando flujos completos del usuario desde la aplicación móvil en dispositivos físicos y emuladores.

## 3. Pruebas Unitarias por Microservicio

### 3.1 users-service (`UserServiceImplTest.java`)

- **ID del test:** US-01
- **Descripción:** testExistsById_UserExists_ReturnsTrue
- **Precondiciones:** El repositorio mockeado tiene un usuario con ID 1.
- **Pasos:** Ejecutar `userService.existsById(1)`.
- **Resultado esperado:** Retorna `true`.
- **Resultado obtenido:** Retorna `true`.
- **Estado:** PASS

- **ID del test:** US-02
- **Descripción:** testFindByCorreo_UserExists_ReturnsDto
- **Precondiciones:** Repositorio mockeado devuelve usuario para `test@test.com`.
- **Pasos:** Ejecutar `userService.findByCorreo("test@test.com")`.
- **Resultado esperado:** Retorna Optional con el DTO correcto.
- **Resultado obtenido:** Coincide correo y ID.
- **Estado:** PASS

### 3.2 karma_service (`KarmaServiceTest.java`)

- **ID del test:** KS-01
- **Descripción:** testProcessCheckIn_Success
- **Precondiciones:** Usuario existe con 100 de karma.
- **Pasos:** Ejecutar `processCheckIn` con un `CheckInEventDTO`.
- **Resultado esperado:** Suma 10 de karma (Total 110). Se guarda historial.
- **Resultado obtenido:** 110 puntos retornados y métodos de guardado llamados.
- **Estado:** PASS

- **ID del test:** KS-02
- **Descripción:** testProcessAbsence_MinimumKarmaIsZero
- **Precondiciones:** Usuario con 10 de karma.
- **Pasos:** Ejecutar `processAbsence`.
- **Resultado esperado:** Resta 15 puntos, pero el límite inferior es 0. Retorna 0.
- **Resultado obtenido:** 0 puntos de karma.
- **Estado:** PASS

- **ID del test:** KS-03
- **Descripción:** testProcessOrganizerValidation_Negative
- **Precondiciones:** Usuario con 100 de karma.
- **Pasos:** Ejecutar `processOrganizerValidation` con `isValidated = false`.
- **Resultado esperado:** Resta 5 puntos (Total 95).
- **Resultado obtenido:** 95 puntos retornados.
- **Estado:** PASS

### 3.3 events-service (`EventServiceTest.java`)

- **ID del test:** EV-01
- **Descripción:** joinEvent_Full_ThrowsException
- **Precondiciones:** Evento con `currentPlayers` = `maxPlayers` (10).
- **Pasos:** Ejecutar `joinEvent(eventId, userId)`.
- **Resultado esperado:** Lanza `IllegalStateException` ("Event is full").
- **Resultado obtenido:** Excepción capturada correctamente.
- **Estado:** PASS

- **ID del test:** EV-02
- **Descripción:** deleteEvent_Success
- **Precondiciones:** Evento activo con 1 usuario inscrito.
- **Pasos:** Ejecutar `deleteEvent(eventId, organizerId)`.
- **Resultado esperado:** Estado pasa a "CANCELLED", se compensa el karma llamando al `karmaServiceClient` y se notifica vía `notificationServiceClient`.
- **Resultado obtenido:** Interacciones con clientes externos verificadas 1 vez.
- **Estado:** PASS

- **ID del test:** EV-03
- **Descripción:** processFinishedEvents_MarksAbsence
- **Precondiciones:** Evento con fecha expirada hace 5 horas y usuario inscrito sin check-in marcado.
- **Pasos:** El cron ejecuta `processFinishedEvents()`.
- **Resultado esperado:** Evento cambia a "FINISHED", el usuario pasa a estado "ABSENT", y se llama al `karmaServiceClient` para registrar inasistencia.
- **Resultado obtenido:** Modificaciones y llamadas verificadas.
- **Estado:** PASS

### 3.4 notification-service (`NotificationServiceTest.java`)

- **ID del test:** NS-01
- **Descripción:** testSendNotification_Success
- **Precondiciones:** Token de FCM válido registrado para el usuario.
- **Pasos:** Ejecutar `sendNotification` con tipo KARMA_INCREASE.
- **Resultado esperado:** Retorna estado "SENT", se llama a FCM y a WebSockets.
- **Resultado obtenido:** Llamadas verificadas correctamente.
- **Estado:** PASS

- **ID del test:** NS-02
- **Descripción:** testSendNotification_FcmFails_StatusIsFailed
- **Precondiciones:** Servicio FCM lanza falla silenciosa al enviar.
- **Pasos:** Ejecutar `sendNotification`.
- **Resultado esperado:** No lanza excepción a quien lo llama, guarda la notificación con estado "FAILED".
- **Resultado obtenido:** Notificación guardada con FAILED.
- **Estado:** PASS

## 4. Pruebas de Integración
- **Flujo Eventos-Karma:** Se probó usando `start-all.ps1`. Al finalizar un evento en el servicio `events-service`, se verificó que la petición HTTP a `karma_service` se efectúa exitosamente, actualizando la BD `pichangapp_karma` en tiempo real.
- **Flujo Eventos-Notificaciones:** Al cancelar un evento, `events-service` envía la petición REST a `notification-service`, el cual envía satisfactoriamente el push FCM a los dispositivos suscritos.

## 5. Pruebas de Seguridad
- **Acceso sin JWT → 401:** Intentos de llamar a `/api/v1/users/profile` sin header Authorization devuelven `401 Unauthorized`.
- **Token expirado → 401:** Empleando un JWT de prueba caducado, el gateway rechaza la petición.
- **Acceso a recurso de otro usuario → 403 / 400:** Intentar modificar datos pasando un ID distinto al contenido en el "sub" (subject) del JWT genera rechazo en la capa de control.

## 6. Pruebas de Negocio Críticas

| Flujo Probado | Verificación | Estado |
| :--- | :--- | :--- |
| **Registro → verificación email → login** | Se crea cuenta, código es verificado, genera JWT al loguear. | APROBADO |
| **Crear evento → unirse → finalizar → karma** | El organizador crea, un usuario se une, el organizador finaliza. El karma del usuario sube. | APROBADO |
| **Eliminar evento → karma compensado → notif.**| Organizador elimina evento. Usuarios inscritos ganan puntos de compensación y reciben PUSH. | APROBADO |
| **Karma inicial → inasistencia → categoría**| Usuario empieza en 100. Falla a evento (-15). Karma baja a 85 (Categoría Medio/Bajo). | APROBADO |

## 7. Matriz de Trazabilidad

| ID Requerimiento | Descripción | Caso de Prueba Asignado |
| :--- | :--- | :--- |
| RF-01 | Login con correo y clave | Postman (1.4), US-02 |
| RF-02 | Cálculo de Karma por asistencia | KS-01, EV-03 |
| RF-03 | Búsqueda de eventos cercanos | Postman (4.2) |
| RF-04 | Envío de notificaciones Push | NS-01, Postman (5.2) |

## 8. Criterios de Aceptación
1. **Cobertura de Código:** Todos los `Service` principales superan el 80% de cobertura en ramas críticas (verificado localmente con IntelliJ IDEA).
2. **0 Bugs Críticos:** Ningún endpoint debe devolver `500 Internal Server Error` ante ingresos de datos estándar o faltantes previstos.
3. **Flujo de Karma Impecable:** Las matemáticas del karma no deben arrojar nunca valores bajo 0, y los eventos finalizados no pueden re-evaluarse.

## 9. Defectos Encontrados y Resueltos

| ID | Descripción del Defecto | Severidad | Estado |
| :--- | :--- | :--- | :--- |
| BUG-01 | Bajar de 0 karma lanzaba excepción no controlada. | Alta | Resuelto (Lógica fijada en KarmaService.java) |
| BUG-02 | Eventos full permitían inscripciones paralelas. | Media | Resuelto (Validación currentPlayers) |
| BUG-03 | Error JSON al deserializar respuesta paginada de Notif. | Media | Resuelto (Se ajustó PageResponseDTO) |
| BUG-04 | Gateway ruteaba mal /auth a /users. | Crítica | Resuelto (Ajuste en application.yml) |

## 10. Conclusiones
El sistema ha alcanzado un alto nivel de madurez técnica. Las pruebas unitarias confirman la lógica central, las pruebas en Postman evidencian un comportamiento consistente del lado de la API expuesta, y el despliegue en Railway superó los controles de seguridad y comunicación inter-servicio. El ecosistema es considerado estable y listo para uso en producción por la aplicación móvil.
