# Configuración Railway — PichangApp

## Tabla de Contenidos
- [1. Descripción General](#1-descripción-general)
- [2. Servicios Desplegados](#2-servicios-desplegados)
- [3. Base de Datos MySQL](#3-base-de-datos-mysql)
- [4. Variables de Entorno por Servicio](#4-variables-de-entorno-por-servicio)
- [5. Cómo Conectarse a la BD desde Local](#5-cómo-conectarse-a-la-bd-desde-local)
- [6. Cómo Hacer Deploy](#6-cómo-hacer-deploy)
- [7. Cómo Ver Logs](#7-cómo-ver-logs)
- [8. Cómo Apagar/Encender Servicios](#8-cómo-apagarencender-servicios)
- [9. Monitoreo de Consumo](#9-monitoreo-de-consumo)

## 1. Descripción General

[Railway](https://railway.app/) es la plataforma Cloud tipo PaaS (Platform as a Service) elegida para alojar todo el ecosistema de backend de PichangApp. Permite despliegues directos desde GitHub y ofrece la creación y mantención de instancias de bases de datos como MySQL en el mismo clúster privado.

## 2. Servicios Desplegados

| Nombre del Servicio | URL Pública | Rama GitHub | Root Directory | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **api-gateway** | https://pichangapp-microservicios-production.up.railway.app | `main` | `/Producto/Backend/api-gateway` | ✅ Activo |
| **users-service** | https://usuarios-production-7e57.up.railway.app | `main` | `/Producto/Backend/users-service` | ✅ Activo |
| **karma_service** | https://karma-production-6cc0.up.railway.app | `main` | `/Producto/Backend/karma_service` | ✅ Activo |
| **events-service** | https://eventos-production-efba.up.railway.app | `main` | `/Producto/Backend/events-service` | ✅ Activo |
| **notification-service** | https://notificaciones-production-2962.up.railway.app | `main` | `/Producto/Backend/notification-service`| ✅ Activo |

## 3. Base de Datos MySQL

PichangApp sigue el patrón *Database per Service*, alojando las diferentes bases lógicas dentro del mismo servidor MySQL que provee Railway.

- **Host interno (para comunicación en Railway):** `mysql.railway.internal:3306`
- **Host público (para conexión local):** `turntable.proxy.rlwy.net:19715`
- **Bases de datos creadas:**
  - `pichangapp_users`
  - `pichangapp_karma`
  - `pichangapp_notifications`
  - `pichangapp_events`

## 4. Variables de Entorno por Servicio

Cada microservicio en Railway tiene configuradas sus propias variables de entorno:

### Todos los microservicios (Comunes)
| Variable | Valor / Placeholder |
| :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `PORT` | `8080` (Railway injecta este valor) |

### users-service
| Variable | Valor / Placeholder |
| :--- | :--- |
| `DB_URL` | `jdbc:mysql://mysql.railway.internal:3306/pichangapp_users` |
| `DB_USER` | `{MYSQL_USER}` |
| `DB_PASSWORD` | `{MYSQL_PASSWORD}` |
| `JWT_SECRET` | `{SECRETO_JWT_SUPER_SEGURO_Y_LARGO}` |

### karma_service
| Variable | Valor / Placeholder |
| :--- | :--- |
| `DB_URL` | `jdbc:mysql://mysql.railway.internal:3306/pichangapp_karma` |
| `DB_USER` | `{MYSQL_USER}` |
| `DB_PASSWORD` | `{MYSQL_PASSWORD}` |

### events-service
| Variable | Valor / Placeholder |
| :--- | :--- |
| `DB_URL` | `jdbc:mysql://mysql.railway.internal:3306/pichangapp_events` |
| `DB_USER` | `{MYSQL_USER}` |
| `DB_PASSWORD` | `{MYSQL_PASSWORD}` |
| `KARMA_SERVICE_URL` | `http://karma_service.railway.internal:8080` |
| `NOTIFICATION_SERVICE_URL`| `http://notification-service.railway.internal:8080` |

### notification-service
| Variable | Valor / Placeholder |
| :--- | :--- |
| `DB_URL` | `jdbc:mysql://mysql.railway.internal:3306/pichangapp_notifications` |
| `DB_USER` | `{MYSQL_USER}` |
| `DB_PASSWORD` | `{MYSQL_PASSWORD}` |
| `FCM_CREDENTIALS` | `{BASE64_FIREBASE_JSON}` |

### api-gateway
| Variable | Valor / Placeholder |
| :--- | :--- |
| `USERS_SERVICE_URL` | `http://users-service.railway.internal:8080` |
| `KARMA_SERVICE_URL` | `http://karma_service.railway.internal:8080` |
| `EVENTS_SERVICE_URL`| `http://events-service.railway.internal:8080` |
| `NOTIF_SERVICE_URL` | `http://notification-service.railway.internal:8080` |

## 5. Cómo Conectarse a la BD desde Local

Si necesitas revisar las tablas de producción desde tu computador local, abre una terminal y utiliza el CLI de MySQL:

```powershell
mysql -h turntable.proxy.rlwy.net -P 19715 -u {MYSQL_USER} -p
```
*(Se solicitará el password. Luego puedes ejecutar `USE pichangapp_events;`, etc.)*

## 6. Cómo Hacer Deploy

El flujo es completamente automático a través de GitHub Actions / Integración Nativa de Railway:
1. Realiza los cambios en tu código local.
2. Haz commit de los cambios.
3. Haz `git push origin main`.
4. Railway detectará el nuevo push en la rama `main`, analizará qué directorios (`Root Directory`) han cambiado, y lanzará un **redeploy automático** del servicio correspondiente usando Maven.

## 7. Cómo Ver Logs

1. Ingresa al Dashboard de Railway en [railway.app](https://railway.app).
2. Haz clic en el proyecto "PichangApp-Microservicios".
3. Selecciona el servicio que deseas inspeccionar (ej: `events-service`).
4. Ve a la pestaña **Deployments**.
5. Haz clic en el botón **View Logs** del último despliegue.

## 8. Cómo Apagar/Encender Servicios

Para ahorrar créditos cuando no se esté utilizando la app:
1. En el Dashboard del proyecto, selecciona un servicio.
2. Dirígete a la pestaña **Settings**.
3. Baja hasta la sección **Service State** o **Danger Zone**.
4. Haz clic en **Pause**. El servicio quedará inactivo. Para encenderlo, vuelve al mismo lugar y presiona **Resume**.

## 9. Monitoreo de Consumo

- En el proyecto, dirígete a la pestaña superior derecha **Usage**.
- Se mostrará una gráfica de consumo en USD de CPU, RAM y Red.
- Cada mes Railway otorga un crédito base. Si la curva de estimación mensual supera el crédito gratuito, se recomienda pausar los servicios.
