-- Script de inicialización para la base de datos de Notificaciones
-- Creación de la base de datos si no existe
CREATE DATABASE IF NOT EXISTS pichangapp_notifications;
USE pichangapp_notifications;

-- Tabla para el historial de notificaciones
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'SENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsquedas rápidas por usuario
CREATE INDEX idx_notification_user_id ON notifications(user_id);

-- Índice para consultas ordenadas por fecha
CREATE INDEX idx_notification_created_at ON notifications(created_at);

-- Tabla para tokens de dispositivos FCM
CREATE TABLE IF NOT EXISTS device_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

-- Índice para búsquedas por usuario
CREATE INDEX idx_device_token_user_id ON device_tokens(user_id);
