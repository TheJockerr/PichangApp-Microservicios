-- Script para insertar datos de prueba para PichangApp
-- Este script es idempotente

-- 1. Insertar usuario de prueba en users-service
USE pichangapp;

INSERT INTO users (id, nombre, apellido, correo, contrasena, enabled)
SELECT 1, 'Test', 'User', 'test@pichangapp.cl', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdqD1RPHXiuf8VKu', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE correo = 'test@pichangapp.cl');

-- 2. Insertar Karma inicial en karma_service
USE pichangapp_karma;

INSERT INTO karma_scores (user_id, karma_score)
SELECT '1', 100
WHERE NOT EXISTS (SELECT 1 FROM karma_scores WHERE user_id = '1');
