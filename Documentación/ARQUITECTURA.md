# Arquitectura del Proyecto PichangApp

## Tabla de Contenidos
- [1. Visión General](#1-visión-general)
- [2. Estructura del Repositorio](#2-estructura-del-repositorio)
- [3. Detalle de cada Microservicio](#3-detalle-de-cada-microservicio)
- [4. Admin Panel](#4-admin-panel)
- [5. Frontend Android](#5-frontend-android)
- [6. Patrones de Diseño Utilizados](#6-patrones-de-diseño-utilizados)
- [7. Base de Datos](#7-base-de-datos)
- [8. Seguridad](#8-seguridad)

## 1. Visión General

El sistema expone un único punto de entrada (API Gateway) hacia los microservicios backend. Existen dos clientes: la **aplicación móvil Android** (usuarios finales) y el **Admin Panel web** (gestión administrativa).

En producción (Railway) todos los microservicios comparten **una única instancia MySQL** por restricciones de presupuesto; cada servicio usa su propia base de datos lógica dentro de esa instancia.

```text
  +-------------------+          +-------------------+
  |    App Android    |          |   Admin Panel     |
  | (Kotlin/Compose)  |          |   (React/Vite)    |
  +---------+---------+          +---------+---------+
            |  HTTPS/REST                  |  HTTPS/REST
            +---------------+--------------+
                            |
                            v
                  +-------------------+
                  |    API Gateway    |
                  |  (Spring Cloud)   |
                  |     :8080         |
                  +---+---+---+---+---+
                      |   |   |   |
          +-----------+   |   |   +-----------+
          |               |   |               |
          v               v   v               v
  +-------------+  +----------+  +----------+  +-------------+
  |users-service|  |karma_    |  |events-   |  |notification-|
  |   :8083     |  |service   |  |service   |  |service      |
  |             |  |  :8081   |  |  :8084   |  |  :8082      |
  +------+------+  +----+-----+  +----+-----+  +------+------+
         |              |             |                |
         +--------------+-------------+----------------+
                                 |
                    +------------v-----------+
                    |   MySQL (Railway)      |
                    |  Una sola instancia    |
                    |  ┌──────────────────┐  |
                    |  │ pichangapp       │  |
                    |  │ pichangapp_karma │  |
                    |  │ pichangapp_events│  |
                    |  │ pichangapp_      │  |
                    |  │  notifications   │  |
                    |  └──────────────────┘  |
                    +------------------------+
```

## 2. Estructura del Repositorio

```
PichangApp-Microservicios/
├── Documentación/          # Informes, diagramas, carta Gantt, wireframes
├── Gestión/                # Gestión del proyecto
└── Producto/
    ├── Backend/
    │   ├── api-gateway/            # Enrutamiento JWT, CORS, rate limiting
    │   ├── users-service/          # Auth, JWT, perfiles, roles
    │   ├── karma_service/          # Sistema de reputación
    │   ├── events-service/         # Eventos deportivos y registros
    │   ├── notification-service/   # FCM push + WebSockets
    │   ├── start-local.ps1         # Arranque local de todos los servicios
    │   └── start-all.ps1
    ├── admin-panel/                # Panel administrativo React
    │   ├── src/
    │   │   ├── api/        # Clientes HTTP por dominio (authApi, usersApi, karmaApi, eventsApi)
    │   │   ├── components/ # Componentes reutilizables
    │   │   ├── hooks/      # AuthContext, useAuth
    │   │   ├── pages/      # Login, Dashboard, Users, UserDetail, Events
    │   │   └── styles/     # variables.css, animations.css
    │   └── DESIGN.md       # Guía de diseño y tokens visuales
    └── front-end/
        └── PichangApp/     # Aplicación móvil Android (Kotlin/Compose)
```

## 3. Detalle de cada Microservicio

### Users Service (`:8083`)
- **Responsabilidad principal:** Autenticación, generación de tokens JWT, gestión de perfiles y verificación de correo electrónico.

| Método | Ruta | Descripción | Requiere JWT |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/auth/register` | Registro de usuario | No |
| POST | `/api/v1/auth/login` | Iniciar sesión, retorna JWT | No |
| POST | `/api/v1/auth/verify-code` | Verifica código de email | No |
| POST | `/api/v1/auth/resend-code` | Reenvía código de verificación | No |
| POST | `/api/v1/auth/enable/{id}` | Habilita cuenta de usuario | No |
| GET | `/api/v1/users/{id}` | Obtiene el perfil del usuario | Sí |
| PUT | `/api/v1/users/{id}` | Actualiza el perfil del usuario | Sí |
| PUT | `/api/v1/users/{id}/password` | Cambia la contraseña | Sí |
| GET | `/api/v1/admin/users` | Lista todos los usuarios | ADMIN |
| GET | `/api/v1/admin/users/{userId}` | Detalle de un usuario | ADMIN |
| DELETE | `/api/v1/admin/users/{userId}` | Elimina un usuario | ADMIN |

- **Entidades/Tablas:** `users`, `roles`
- **Comunicaciones:** Es la única fuente de verdad de identidad. El API Gateway y otros servicios decodifican el JWT que este servicio emite para extraer el `userId` y el rol.

---

### Karma Service (`:8081`)
- **Responsabilidad principal:** Sistema de reputación: calcula, ajusta y registra historial de karma por comportamiento en eventos.

| Método | Ruta | Descripción | Requiere JWT |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/karma/{userId}` | Obtiene karma e historial del usuario | Sí |
| POST | `/api/v1/karma/check-in` | Registra asistencia y suma karma | Sí |
| POST | `/api/v1/karma/absence/{userId}/event/{eventId}` | Registra inasistencia y resta karma | Sí |
| POST | `/api/v1/karma/validation` | Valida asistencia por el organizador | Sí |
| PUT | `/api/v1/admin/karma/{userId}` | Ajuste manual de karma (admin) | ADMIN |

- **Entidades/Tablas:** `karma_scores`, `karma_history`
- **Comunicaciones:** Recibe peticiones desde `events-service` (vía RestTemplate) al finalizar eventos o registrar inasistencias.

---

### Events Service (`:8084`)
- **Responsabilidad principal:** Creación, búsqueda geolocalizada y gestión del ciclo de vida de eventos deportivos.

| Método | Ruta | Descripción | Requiere JWT |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/events` | Crear nuevo evento | Sí |
| GET | `/api/v1/events` | Buscar eventos cercanos (params: lat/lng) | Sí |
| GET | `/api/v1/events/{id}` | Detalle completo de un evento | Sí |
| POST | `/api/v1/events/{id}/join` | Inscribirse en un evento | Sí |
| DELETE | `/api/v1/events/{id}/leave` | Cancelar participación | Sí |
| GET | `/api/v1/events/{id}/registrations` | Ver inscritos (solo organizador) | Sí |
| POST | `/api/v1/events/{id}/attendance` | Marcar asistencia del usuario | Sí |
| POST | `/api/v1/events/{id}/finish` | Finaliza evento y evalúa karma | Sí |
| DELETE | `/api/v1/events/{id}` | Eliminar evento (organizador) | Sí |
| GET | `/api/v1/events/my-events` | Eventos donde el usuario está inscrito | Sí |
| GET | `/api/v1/events/organizing` | Eventos que el usuario organiza | Sí |
| GET | `/api/v1/admin/events` | Lista todos los eventos | ADMIN |
| DELETE | `/api/v1/admin/events/{eventId}` | Cancela un evento (admin) | ADMIN |

- **Entidades/Tablas:** `events`, `event_registrations`
- **Comunicaciones:** Llama a `karma_service` al finalizar un evento o registrar inasistencias. Llama a `notification-service` para alertar a los inscritos de cancelaciones y cambios.

---

### Notification Service (`:8082`)
- **Responsabilidad principal:** Centralización de notificaciones push (Firebase FCM) y comunicación en tiempo real (WebSocket).

| Método | Ruta | Descripción | Requiere JWT |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/notifications/token` | Registra el token FCM del dispositivo | Sí |
| POST | `/api/v1/notifications/send` | Envía notificación push asíncrona | Sí |
| GET | `/api/v1/notifications/history` | Historial paginado de notificaciones | Sí |
| WS | `/ws/**` | Canal WebSocket para notificaciones en tiempo real | Sí |

- **Entidades/Tablas:** `notifications`, `device_tokens`
- **Comunicaciones:** Recibe llamadas desde `events-service` y se conecta con las APIs de Firebase Cloud Messaging.

---

### API Gateway (`:8080`)
- **Responsabilidad principal:** Punto de entrada único. Valida JWT, enruta al microservicio correcto, gestiona CORS y rate limiting.
- **Sin base de datos propia.**
- **Enrutamiento:**

| Prefijo | Destino |
| :--- | :--- |
| `/api/v1/auth/**`, `/api/v1/users/**` | users-service |
| `/api/v1/karma/**` | karma_service |
| `/api/v1/events/**` | events-service |
| `/api/v1/notifications/**`, `/ws/**` | notification-service |
| `/api/v1/admin/users/**` | users-service (requiere rol ADMIN) |
| `/api/v1/admin/karma/**` | karma_service (requiere rol ADMIN) |
| `/api/v1/admin/events/**` | events-service (requiere rol ADMIN) |

## 4. Admin Panel

Panel de administración web construido con React 18 y Vite 5. Permite a los administradores gestionar usuarios, karma y eventos desde el navegador.

- **URL de producción:** https://pichangapp-admin.vercel.app
- **URL local:** http://localhost:5173
- **Apunta a:** API Gateway (`VITE_API_URL`)

**Páginas:**

| Página | Descripción |
| :--- | :--- |
| `LoginPage` | Autenticación con validación de rol ADMIN |
| `DashboardPage` | KPIs (usuarios, eventos activos/finalizados, karma promedio) + gráfico de eventos por día |
| `UsersPage` | Grid de usuarios con filtros por estado y rol, paginación, karma inline |
| `UserDetailPage` | Perfil de usuario, medidor circular de karma, historial, ajuste manual con slider, eliminar |
| `EventsPage` | Grid de eventos con filtros por estado, progreso de jugadores, eliminar evento |

**Stack:**
- React 18 + React Router v6
- TanStack React Query v5
- Recharts (gráficos)
- Lucide React (iconos)
- CSS puro con variables de diseño

## 5. Frontend Android

La aplicación móvil está diseñada bajo el patrón MVVM con Clean Architecture en Kotlin y Jetpack Compose:

- **`core/`** — Configuraciones globales, interceptor JWT, DataStore, tema Material 3.
- **`data/`** — DTOs, clientes Retrofit, implementaciones de repositorios.
- **`domain/`** — Modelos limpios y casos de uso (UseCases).
- **`ui/screens/`** — Composables de cada pantalla (Login, Dashboard, Events, Karma, Profile, Notifications), controlados por sus ViewModels.
- **`di/`** — Módulos Hilt (NetworkModule, RepositoryModule, AppModule).

**Stack:** Kotlin 2.1.0 · Jetpack Compose · Material 3 · Hilt · Retrofit 2 · OkHttp · Coil · DataStore · Navigation Compose

## 6. Patrones de Diseño Utilizados

- **MVVM (Model-View-ViewModel):** Usado en la app Android para mantener la capa visual (Jetpack Compose) completamente reactiva al estado del ViewModel.
- **Clean Architecture:** Separación en capas `data`, `domain` y `ui` en el cliente Android.
- **Arquitectura en capas (Backend):** `Controller` (exposición REST) → `Service` (lógica de negocio) → `Repository` (Spring Data JPA).
- **API Gateway Pattern:** Punto de acceso único que oculta la topología interna de los microservicios.
- **JWT Stateless:** El `users-service` emite el token; el `api-gateway` valida la firma. Los microservicios internos decodifican localmente el `userId` y el rol sin consultar un servidor de sesiones.
- **Comunicación Síncrona REST (RestTemplate):** Para orquestación directa entre microservicios cuando se requiere inmediatez (p. ej., `events-service` → `karma_service`).

## 7. Base de Datos

### Instancia de producción (Railway)

Por restricciones de presupuesto, los cuatro microservicios se conectan a **una única instancia MySQL en Railway**. Cada servicio utiliza su propia base de datos lógica dentro de esa instancia, manteniendo aislamiento a nivel de esquema:

| Servicio | Base de datos | Tablas principales |
| :--- | :--- | :--- |
| users-service | `pichangapp` | `users`, `roles` |
| karma_service | `pichangapp_karma` | `karma_scores`, `karma_history` |
| events-service | `pichangapp_events` | `events`, `event_registrations` |
| notification-service | `pichangapp_notifications` | `notifications`, `device_tokens` |

> No existen Foreign Keys entre bases de datos. Las relaciones cruzadas se gestionan por `userId` / `eventId` a nivel de aplicación.

### Detalle de esquemas

- **`pichangapp`** (`users-service`):
  - `users`: id, correo, contraseña (bcrypt), nombre, apellido, enabled, verification_code, verification_code_expiry.
  - `roles`: id, nombre (USER, ADMIN).
  - *Relación:* Muchos-a-Muchos entre usuarios y roles.

- **`pichangapp_karma`** (`karma_service`):
  - `karma_scores`: id, userId, puntaje (inicia en 100), categoría (Bajo <50, Medio 50–80, Alto >80).
  - `karma_history`: id, karma_score_id, amount, reason, created_at.
  - *Relación:* Uno-a-Muchos entre `karma_scores` y `karma_history`.

- **`pichangapp_events`** (`events-service`):
  - `events`: id, organizer_id, nombre, deporte, fecha, estado (ACTIVE/FINISHED/CANCELLED), max_players, current_players, lat, lng.
  - `event_registrations`: id, event_id, user_id, estado (REGISTERED/ATTENDED/ABSENT), registered_at, checked_in_at.
  - *Relación:* Uno-a-Muchos entre `events` y `event_registrations`.

- **`pichangapp_notifications`** (`notification-service`):
  - `notifications`: id, user_id, title, body, status, created_at.
  - `device_tokens`: id, user_id, token_fcm.
  - *Sin FK entre tablas; ambas referencian `user_id` externamente.*

Todos los servicios usan `spring.jpa.hibernate.ddl-auto=update` — el esquema se crea y actualiza automáticamente al arrancar.

## 8. Seguridad

El flujo de autenticación JWT:

1. El usuario envía credenciales a `POST /api/v1/auth/login`.
2. El `users-service` valida con Spring Security y emite un JWT firmado que contiene `sub` (userId), `role` y expiración (1 hora).
3. El cliente almacena el token (DataStore en Android, memoria/cookie en Admin Panel).
4. Cada petición incluye el header `Authorization: Bearer <token>`.
5. El `api-gateway` valida la firma con el `JWT_SECRET` compartido. Si el token es inválido o expirado, retorna `401 Unauthorized` sin llegar a los microservicios.
6. Los microservicios internos decodifican localmente el token para extraer el `userId` sin requerir parámetros adicionales por URL.
7. Las rutas `/api/v1/admin/**` requieren además que el claim `role` sea `ADMIN`; cualquier otro rol recibe `403 Forbidden`.
