# Guía de Pruebas Postman — PichangApp

## Tabla de Contenidos
- [Configuración del Entorno](#configuración-del-entorno)
- [Módulo 1 — Autenticación](#módulo-1--autenticación)
- [Módulo 2 — Usuarios](#módulo-2--usuarios)
- [Módulo 3 — Karma](#módulo-3--karma)
- [Módulo 4 — Eventos](#módulo-4--eventos)
- [Módulo 5 — Notificaciones](#módulo-5--notificaciones)
- [Módulo 6 — Health Checks](#módulo-6--health-checks)
- [Flujo Completo de Prueba End-to-End](#flujo-completo-de-prueba-end-to-end)

## Configuración del Entorno

1. En Postman, ve a la sección "Environments" y crea uno nuevo llamado "PichangApp Prod".
2. Agrega las siguientes variables:
   - `baseUrl` = `https://pichangapp-microservicios-production.up.railway.app`
   - `token` = (Dejar vacío, se llenará automáticamente con el script de login)
3. En la pestaña de colección o en el endpoint de Login (Módulo 1.4), dirígete a la pestaña "Tests" y agrega el siguiente script para guardar el token:
   ```javascript
   var jsonData = pm.response.json();
   pm.environment.set("token", jsonData.token);
   ```

## Módulo 1 — Autenticación

### 1.1 Registro de usuario
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/auth/register`
**Requiere JWT:** No
**Headers:** `Content-Type: application/json`
**Body:**
```json
{
  "correo": "test@pichangapp.cl",
  "password": "{VALOR_SECRETO}",
  "nombre": "Esteban",
  "apellido": "Mora"
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** La correcta creación de un usuario inactivo en la base de datos hasta que se verifique el email.

### 1.2 Verificar código de email
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/auth/verify?correo=test@pichangapp.cl&codigo=123456`
**Requiere JWT:** No
**Headers:** None
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Cambia el estado del usuario a "enabled" validando que el correo electrónico le pertenece.

### 1.3 Reenviar código de verificación
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/auth/resend-code?correo=test@pichangapp.cl`
**Requiere JWT:** No
**Headers:** None
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Genera un nuevo código en BD (y teóricamente lo enviaría por correo) para un usuario no verificado.

### 1.4 Login
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/auth/login`
**Requiere JWT:** No
**Headers:** `Content-Type: application/json`
**Body:**
```json
{
  "correo": "test@pichangapp.cl",
  "password": "{VALOR_SECRETO}"
}
```
**Respuesta esperada:** 200 OK (Incluye el Bearer Token)
**Qué valida:** Autenticación de credenciales, usuario habilitado y generación del JWT.

### 1.5 Login con credenciales incorrectas (prueba negativa)
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/auth/login`
**Requiere JWT:** No
**Headers:** `Content-Type: application/json`
**Body:**
```json
{
  "correo": "test@pichangapp.cl",
  "password": "wrong_password"
}
```
**Respuesta esperada:** 401 Unauthorized o 403 Forbidden
**Qué valida:** Spring Security bloquea el acceso a usuarios con contraseña incorrecta.

### 1.6 Request sin JWT (prueba negativa → debe retornar 401)
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/users/profile`
**Requiere JWT:** Sí (pero se simula enviándolo sin token)
**Headers:** Ninguno
**Body:** Ninguno
**Respuesta esperada:** 401 Unauthorized
**Qué valida:** Protección estricta de rutas privadas por el Gateway o el microservicio.

---

## Módulo 2 — Usuarios
*(A partir de aquí, incluir Header: `Authorization: Bearer {{token}}` en todos los endpoints)*

### 2.1 Obtener perfil de usuario
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/users/profile`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Lectura de datos del usuario autenticado (extrae el ID desde el token JWT).

### 2.2 Actualizar perfil
**Método:** PUT
**URL:** `{{baseUrl}}/api/v1/users/profile`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "nombre": "Esteban Actualizado",
  "apellido": "Mora Actualizado"
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** Modificación exitosa de los datos de perfil propios en la base de datos.

### 2.3 Verificar existencia de usuario
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/users/exists/1`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK (Boolean true/false)
**Qué valida:** Que el endpoint que utilizan los otros microservicios responda correctamente.

---

## Módulo 3 — Karma

### 3.1 Consultar karma de usuario
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/karma/1`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK (KarmaResponseDTO)
**Qué valida:** Muestra puntaje actual y categoría del usuario.

### 3.2 Registrar check-in (suma karma)
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/karma/check-in`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "userId": "1",
  "eventId": "100",
  "locationContext": "Cancha 1"
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** Aumento de 10 puntos de Karma por asistir puntualmente.

### 3.3 Registrar inasistencia (resta karma)
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/karma/absence/1/100`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Disminución de 15 puntos de Karma.

### 3.4 Validación por organizador (positiva)
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/karma/organizer-validation`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "userId": "1",
  "eventId": "100",
  "organizerId": "2",
  "isValidated": true
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** Agrega 5 puntos extra cuando el organizador valida al jugador.

### 3.5 Validación por organizador (negativa)
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/karma/organizer-validation`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "userId": "1",
  "eventId": "100",
  "organizerId": "2",
  "isValidated": false
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** Resta 5 puntos si el organizador marca conducta indebida.

---

## Módulo 4 — Eventos

### 4.1 Crear evento
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/events`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "name": "Pichanga de Fin de Semana",
  "eventDate": "2026-10-15T18:00:00",
  "maxPlayers": 10,
  "latitude": -33.4569,
  "longitude": -70.6482
}
```
**Respuesta esperada:** 201 Created o 200 OK
**Qué valida:** Crea el evento asignando automáticamente el ID del creador al `organizerId`.

### 4.2 Buscar eventos por cercanía
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/events/nearby?lat=-33.4569&lng=-70.6482&radius=5.0`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Filtrado geolocalizado de eventos activos.

### 4.3 Obtener detalle de evento
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/events/1`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Trae toda la info del evento específico.

### 4.4 Unirse a evento
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/events/1/join`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Registra la participación del usuario en la tabla `event_registrations`.

### 4.5 Cancelar participación
**Método:** DELETE
**URL:** `{{baseUrl}}/api/v1/events/1/leave`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK o 204 No Content
**Qué valida:** El usuario sale del evento, aplicando reglas de negocio (ej. no permite si faltan < 2hrs).

### 4.6 Ver inscritos (solo organizador)
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/events/1/attendees`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Listado de los usuarios registrados para este evento.

### 4.7 Marcar asistencia — asistió
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/events/1/attendance?userId=2&attended=true`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** El organizador o sistema marca que el jugador SÍ fue.

### 4.8 Marcar asistencia — no asistió
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/events/1/attendance?userId=2&attended=false`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Marca la inasistencia.

### 4.9 Finalizar evento
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/events/1/finish`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Cambia estado a FINISHED y orquesta los llamados al `karma_service` para todos los involucrados.

### 4.10 Eliminar evento
**Método:** DELETE
**URL:** `{{baseUrl}}/api/v1/events/1`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK o 204 No Content
**Qué valida:** El organizador elimina, enviando compensación de karma a los inscritos.

### 4.11 Mis eventos
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/events/my-events`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Listado de eventos a los que el token de usuario actual asiste.

### 4.12 Eventos que organizo
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/events/organized`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Listado de eventos creados por el usuario actual.

---

## Módulo 5 — Notificaciones

### 5.1 Registrar token FCM
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/notifications/token`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "userId": "1",
  "token": "dG_x7ZkH... (Token Firebase de Android)"
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** Vincula un ID de dispositivo Android al usuario para Push.

### 5.2 Enviar notificación a usuario
**Método:** POST
**URL:** `{{baseUrl}}/api/v1/notifications/send`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
**Body:**
```json
{
  "userId": "1",
  "title": "Aviso",
  "body": "El evento ha sido cancelado",
  "type": "EVENT_CANCELLED"
}
```
**Respuesta esperada:** 200 OK
**Qué valida:** Petición manual o interna para emitir la notificación.

### 5.3 Consultar historial de notificaciones
**Método:** GET
**URL:** `{{baseUrl}}/api/v1/notifications/history?page=0&size=10`
**Requiere JWT:** Sí
**Headers:** `Authorization: Bearer {{token}}`
**Body:** Ninguno
**Respuesta esperada:** 200 OK
**Qué valida:** Lista paginada de notificaciones anteriores de un usuario.

---

## Módulo 6 — Health Checks

### 6.1 Health del API Gateway
**Método:** GET
**URL:** `{{baseUrl}}/actuator/health`
**Requiere JWT:** No
**Headers:** Ninguno
**Body:** Ninguno
**Respuesta esperada:** 200 OK (`{"status": "UP"}`)
**Qué valida:** Saber si el proxy principal está activo en Railway.

### 6.2 Health de cada microservicio
**Método:** GET
**URLs:** 
- `https://usuarios-production-7e57.up.railway.app/actuator/health`
- `https://karma-production-6cc0.up.railway.app/actuator/health`
- `https://eventos-production-efba.up.railway.app/actuator/health`
- `https://notificaciones-production-2962.up.railway.app/actuator/health`
**Requiere JWT:** No
**Respuesta esperada:** 200 OK (`{"status": "UP"}`)

---

## Flujo Completo de Prueba End-to-End

Sigue este orden exacto para simular el comportamiento de la app en la vida real:

1. **Registro (1.1):** Crear un usuario nuevo (A).
2. **Registro (1.1):** Crear un usuario nuevo (B).
3. **Login (1.4):** Autenticar Usuario (A). Capturar token automáticamente.
4. **Perfil (2.1):** Verificar perfil de (A).
5. **Crear Evento (4.1):** El Usuario (A) crea una pichanga. Anotar el Event ID.
6. **Token FCM (5.1):** El Usuario (A) simula registrar su dispositivo.
7. **Login (1.4):** Cambiar a las credenciales de Usuario (B) y autenticar.
8. **Buscar (4.2):** Buscar eventos cercanos; debe aparecer el creado por (A).
9. **Unirse (4.4):** El Usuario (B) se une al evento.
10. **Finalizar Evento (4.9):** Volver a hacer login con Usuario (A) y disparar el endpoint `finish`.
11. **Consultar Karma (3.1):** Revisar que el Usuario (B) haya modificado su karma según se haya pasado su check-in.
12. **Historial Notificaciones (5.3):** Verificar que la notificación del `finish` llegó a todos los inscritos.
