# PichangApp — Microservicios

## Proyecto
Aplicación de gestión de "pichangas" (partidos de fútbol amateur). Arquitectura de microservicios con un panel administrativo (`admin-panel`) como producto principal de diseño.

## Skills instaladas

Las siguientes skills están disponibles en `.claude/skills/` y se pueden activar en cualquier tarea de diseño/frontend:

| Skill | Directorio | Cuándo usarla |
|-------|-----------|---------------|
| **theme-factory** | `neversight-learn-skills.dev-theme-factory/` | Aplicar/generar temas visuales (colores + tipografía) a cualquier artefacto |
| **frontend-design** | `neversight-learn-skills.dev-frontend-design/` | Construir interfaces web de alta calidad, evitando estética genérica de IA |
| **canvas-design** | `neversight-learn-skills.dev-canvas-design/` | Crear arte visual original en `.png`/`.pdf` con filosofía de diseño |
| **brand-guidelines** | `skillcreatorai-ai-agent-skills-brand-guidelines/` | Aplicar colores y tipografía de marca a artefactos con consistencia |

### Cómo usar una skill
1. Leer `SKILL.md` dentro del directorio de la skill correspondiente
2. Seguir las instrucciones del archivo para completar la tarea
3. Las skills se complementan entre sí — usar `brand-guidelines` + `frontend-design` para interfaces con identidad de marca

## Stack tecnológico

- **Admin Panel**: React + TypeScript + Vite
- **Backend**: Microservicios (Java/Spring Boot)
- **Estilo**: CSS modular / componentes

## Convenciones

- Código en inglés, comentarios en español
- Commits en español siguiendo Conventional Commits
- Cada microservicio en su propia carpeta bajo `/Microservicios/`
