# PichangApp — Gestión Deportiva Inteligente

![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-green)
![Java](https://img.shields.io/badge/Backend-Java%2022-orange)
![Spring Boot](https://img.shields.io/badge/Framework-Spring%20Boot%203.2-brightgreen)
![React](https://img.shields.io/badge/Admin%20Panel-React%2018-61DAFB)
![Kotlin](https://img.shields.io/badge/Android-Kotlin-blue)

**PichangApp** es una plataforma de gestión de partidos deportivos amateur ("pichangas") con un sistema de karma para incentivar la asistencia y el juego limpio. Conecta deportistas de fútbol, tenis y pádel en Chile mediante geolocalización.

---

## Propuesta de valor

El diferencial es el **Sistema de Karma**: algoritmo de reputación que penaliza las inasistencias y premia la puntualidad, reduciendo la deserción en partidos organizados.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 22, Spring Boot 3.2, Spring Security 6 (JWT) |
| Base de datos | MySQL 8 |
| API Gateway | Spring Cloud Gateway |
| Admin Panel | React 18, Vite 5, TanStack Query v5, Recharts, Lucide |
| App móvil | Kotlin, Android Studio |
| Infraestructura | Railway (backend), Vercel (admin panel) |
| Correo | SendGrid |

---

## Arquitectura de microservicios

```
┌─────────────────────────────────────────────┐
│          Admin Panel (React/Vite)           │
│       Vercel / localhost:5173               │
└────────────────────┬────────────────────────┘
                     │ HTTP (JWT Bearer)
┌────────────────────▼────────────────────────┐
│         API Gateway — puerto 8080           │
│   Validación JWT · Enrutamiento · CORS      │
└──┬──────────┬──────────┬──────────┬─────────┘
   │          │          │          │
   ▼          ▼          ▼          ▼
users-    karma-     events-  notification-
service   service    service    service
:8083      :8081      :8084      :8082
```

### Responsabilidades de cada servicio

| Servicio | Puerto | Base de datos | Función |
|---|---|---|---|
| `api-gateway` | 8080 | — | Validación JWT, enrutamiento, CORS, rate limiting |
| `users-service` | 8083 | `pichangapp` | Auth (JWT), perfiles, rol ADMIN |
| `karma_service` | 8081 | `pichangapp_karma` | Cálculo y ajuste de karma, historial |
| `events-service` | 8084 | `pichangapp_events` | Creación, búsqueda y gestión de eventos |
| `notification-service` | 8082 | `pichangapp_notifications` | Notificaciones push (FCM) y WebSockets |

---

## Admin Panel

Panel de administración web con diseño gamificado. Permite gestionar usuarios, karma y eventos desde el navegador.

**Tecnologías:** React 18 · Vite 5 · TanStack React Query v5 · Recharts · Lucide React · CSS puro con variables

**Funcionalidades:**
- Login con validación de rol ADMIN
- Dashboard con KPIs (usuarios, eventos activos/finalizados, karma promedio) y gráfico de eventos por día
- Gestión de usuarios: búsqueda, filtros por estado y rol, paginación, karma por usuario
- Detalle de usuario: medidor circular de karma, historial de movimientos, ajuste de karma con slider, eliminar usuario
- Gestión de eventos: grid de cards con filtros por estado, progreso de jugadores, eliminar evento

**URL de producción:** [https://pichangapp-admin.vercel.app](https://pichangapp-admin.vercel.app)

---

## URLs de producción

| Servicio | URL |
|---|---|
| API Gateway | https://pichangapp-microservicios-production.up.railway.app |
| Admin Panel | https://pichangapp-admin.vercel.app |

---

## Estructura del repositorio

```
PichangApp-Microservicios/
├── Documentación/          # Informes, diagramas UML, carta Gantt, wireframes
├── Producto/
│   ├── Backend/
│   │   ├── api-gateway/
│   │   ├── users-service/
│   │   ├── karma_service/
│   │   ├── events-service/
│   │   ├── notification-service/
│   │   ├── start-local.ps1     ← arranque local de todos los servicios
│   │   └── start-all.ps1
│   ├── admin-panel/            ← panel de administración React
│   │   ├── src/
│   │   │   ├── api/            # clientes HTTP por dominio
│   │   │   ├── components/     # componentes reutilizables
│   │   │   ├── hooks/          # AuthContext, useAuth
│   │   │   ├── pages/          # Login, Dashboard, Users, UserDetail, Events
│   │   │   └── styles/         # variables.css, animations.css
│   │   ├── public/             # logo.png
│   │   └── DESIGN.md           # guía de diseño y tokens visuales
│   └── front-end/
│       └── PichangApp/         # aplicación móvil Android (Kotlin)
```

---

## Prueba local

### Prerrequisitos

- **Laragon** con MySQL 8 corriendo
- **Java 22** en el PATH
- **Node.js 20+** en el PATH
- Las siguientes bases de datos en MySQL (se crean automáticamente con `ddl-auto=update` si se pasa el nombre):

```sql
CREATE DATABASE IF NOT EXISTS pichangapp;
CREATE DATABASE IF NOT EXISTS pichangapp_karma;
CREATE DATABASE IF NOT EXISTS pichangapp_events;
CREATE DATABASE IF NOT EXISTS pichangapp_notifications;
```

### 1. Arrancar los microservicios

El script `start-local.ps1` levanta los 5 servicios con las variables de entorno correctas y espera a que todos reporten `Started`:

```powershell
cd Producto/Backend
.\start-local.ps1
```

Resultado esperado:
```
OK   karma-service          http://localhost:8081
OK   notification-service   http://localhost:8082
OK   users-service          http://localhost:8083
OK   events-service         http://localhost:8084
OK   api-gateway            http://localhost:8080
```

> `notification-service` puede quedar en WAIT si no hay credenciales FCM configuradas — el resto del sistema funciona igual.

### 2. Arrancar el panel de administración

```bash
cd Producto/admin-panel
npm install        # solo la primera vez
npm run dev
```

Abre **http://localhost:5173**

### 3. Credenciales de administrador

| Campo | Valor |
|---|---|
| Correo | `admin@pichangapp.cl` |
| Contraseña | `Admin@2024!` |

El usuario administrador se crea automáticamente al primer arranque de `users-service` (ver `DataInitializer.java`).

### 4. Variables de entorno del panel

Crea `Producto/admin-panel/.env` apuntando al gateway local o de Railway:

```env
# Local
VITE_API_URL=http://localhost:8080

# Railway (producción)
# VITE_API_URL=https://pichangapp-microservicios-production.up.railway.app
```

---

## Despliegue

### Backend — Railway

Cada microservicio se despliega como un servicio independiente en Railway. Las variables de entorno necesarias son:

```
JWT_SECRET=<clave-segura>
DATABASE_URL=<url-mysql-railway>
DB_USERNAME=<usuario>
DB_PASSWORD=<contraseña>
SENDGRID_API_KEY=<clave-sendgrid>   # solo users-service
```

### Admin Panel — Vercel

```bash
cd Producto/admin-panel
npm run build       # genera dist/
```

En Vercel, apuntar el root a `Producto/admin-panel` y configurar la variable de entorno:
```
VITE_API_URL=https://pichangapp-microservicios-production.up.railway.app
```

---

## Equipo

| Nombre | Rol |
|---|---|
| Esteban Mora | Arquitecto · Desarrollador Backend · Admin Panel |
| David Salazar | Product Owner · Desarrollador |

*Proyecto de la asignatura Taller Aplicado de Programación (TPY1101) — Duoc UC.*
