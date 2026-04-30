-- Script de inicialización para la base de datos de Karma
-- Creación de la base de datos si no existe
CREATE DATABASE IF NOT EXISTS pichangapp_karma;
USE pichangapp_karma;

-- Tabla principal de puntajes de karma
CREATE TABLE IF NOT EXISTS karma_scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    karma_score INT NOT NULL DEFAULT 100
);

-- Indice para búsquedas rápidas por usuario
CREATE INDEX idx_karma_user_id ON karma_scores(user_id);

-- Tabla para el historial de cambios de karma
CREATE TABLE IF NOT EXISTS karma_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    karma_score_id INT NOT NULL,
    amount INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (karma_score_id) REFERENCES karma_scores(id) ON DELETE CASCADE
);
