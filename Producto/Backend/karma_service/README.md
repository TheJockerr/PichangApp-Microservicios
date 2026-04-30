# Karma Service

Este microservicio forma parte del ecosistema de PichangApp. Se encarga de gestionar el **Karma** (puntaje de reputación) de los usuarios, premiando el cumplimiento y penalizando la inasistencia.

## Tecnologías

- Java 17
- Spring Boot 3.2.x
- Spring Data JPA
- Spring Security + JWT
- MySQL 8

## Configuración Local (Laragon)

1. Ejecutar Laragon e iniciar MySQL.
2. Ejecutar el script SQL proporcionado en `sql/init_karma.sql` para crear la base de datos y las tablas.
   ```bash
   mysql -u root < sql/init_karma.sql
   ```
3. Las credenciales por defecto en `application.properties` asumen el usuario `root` sin contraseña en `localhost:3306`.
4. Ejecutar el proyecto:
   ```bash
   ./mvnw spring-boot:run
   ```

## Endpoints

### 1. Obtener Karma de Usuario
`GET /api/v1/karma/{userId}`
Obtiene el puntaje actual de un usuario. Si no existe, se inicializa con 100 puntos.

### 2. Registrar Check-in Exitoso
`POST /api/v1/karma/check-in`
Suma 10 puntos al usuario.
```json
{
  "userId": "user123",
  "eventId": "event456",
  "location": "-33.45,-70.66"
}
```

### 3. Registrar Inasistencia
`POST /api/v1/karma/absence/{userId}/event/{eventId}`
Resta 15 puntos al usuario.

### 4. Validación de Organizador
`POST /api/v1/karma/validation`
Suma (+5) o resta (-5) puntos basado en el reporte del organizador.
```json
{
  "userId": "user123",
  "eventId": "event456",
  "organizerId": "org789",
  "isPositiveValidation": true
}
```

## Pruebas

Para ejecutar las pruebas unitarias:
```bash
./mvnw test
```
