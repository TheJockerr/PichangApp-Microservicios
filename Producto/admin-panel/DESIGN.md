# PichangApp — Brand Guidelines & Sistema de Diseño

> Guía de marca del **Panel de Administración** de PichangApp.
> Inspiración visual: **Duolingo** (gamificado) + **Strava** (deportivo) + colores vibrantes.

---

## 1. Identidad de marca

| | |
|---|---|
| **Nombre** | PichangApp |
| **Concepto** | Plataforma deportiva recreativa chilena para gestionar "pichangas" (partidos amateur) |
| **Personalidad** | Energética · Inclusiva · Divertida · Confiable |
| **Tono visual** | Gamificado, redondeado, colorido, con profundidad táctil |

La marca debe sentirse como una **cancha en movimiento**: viva, amistosa y motivadora.
Cada pantalla premia la acción y celebra la actividad de la comunidad.

---

## 2. Paleta de colores

### Principales

| Token | Hex | Uso |
|-------|-----|-----|
| Primary | `#2E7D32` | Verde marca — confianza, deporte, acciones primarias |
| Primary light | `#66BB6A` | Verde energía — gradientes, hovers |
| Primary dark | `#1B5E20` | Borde inferior 3D, estados activos |
| Accent | `#FF6F00` | Naranja — dinamismo, llamadas a la acción, destacados |
| Accent light | `#FFB300` | Ámbar — gradientes de acento |
| Blue | `#1565C0` | Azul — estadísticas y datos |
| Purple | `#6A1B9A` | Morado — karma del sistema |
| Danger | `#C62828` | Rojo — alertas, eliminar |

### Superficies y texto

| Token | Hex | Uso |
|-------|-----|-----|
| Background | `#F8FBF8` | Fondo general (blanco verdoso) |
| Surface | `#FFFFFF` | Tarjetas, paneles |
| Dark | `#1A1A2E` | Sidebar, texto sobre claro fuerte |
| Text | `#2C3E50` | Texto principal |
| Text muted | `#78909C` | Texto secundario |
| Border | `#E6EDE6` | Bordes sutiles |

> **Regla:** colores dominantes con acentos nítidos. Nunca paletas tímidas y planas.
> El verde manda; naranja, azul y morado son acentos puntuales con significado.

---

## 3. Tipografía

| Rol | Fuente | Pesos |
|-----|--------|-------|
| Títulos / Headings | **Nunito** (redondeada, amigable, deportiva) | 700 · 800 · 900 |
| Cuerpo / Body | **Inter** (limpia, legible) | 300 · 400 · 500 · 600 |
| Números / KPIs | **Nunito** 800–900 | grandes y llamativos |

Cargadas desde Google Fonts en `index.html`. Los KPIs y métricas usan Nunito en
peso extra-bold para sentirse como un marcador deportivo.

---

## 4. Principios de diseño

1. **Esquinas muy redondeadas** — `16px` en cards, `12px` en botones, `9999px` en chips/avatares.
2. **Sombras suaves y coloridas** — la sombra toma el color del elemento (`--shadow-accent`, `--shadow-blue`…).
3. **Íconos con fondo circular de color** — cada categoría tiene su color propio.
4. **Gradientes** en headers, botones primarios y stat cards.
5. **Botones táctiles (firma Duolingo)** — borde inferior más oscuro (`box-shadow: 0 4px 0`) que se comprime al presionar (`:active`).
6. **Micro-animaciones** — reveals escalonados al cargar (`fadeInUp` + `animation-delay`), `scale` y sombra en hover.
7. **Stats con números enormes** — los KPIs son los protagonistas.
8. **Gamificación** — avatares con color por usuario, badges de karma por categoría, barras de progreso.

---

## 5. Tokens (ver `src/styles/variables.css`)

- **Espaciado:** `4 · 8 · 16 · 24 · 32 · 48 px`
- **Radios:** `sm 8 · md 12 · lg 16 · xl 24 · full`
- **Sombras:** `sm · md · lg` + variantes por color (`accent · blue · purple · danger`)
- **Transición base:** `all .2s cubic-bezier(.4,0,.2,1)`

---

## 6. Componentes clave

| Componente | Descripción |
|-----------|-------------|
| `StatCard` | Tarjeta KPI con gradiente, número enorme e ícono circular |
| `UserAvatar` | Avatar circular con iniciales y color derivado del nombre (hash) |
| `KarmaBadge` | Badge de karma coloreado por categoría (Excelente/Bueno/Regular/Bajo) |
| `SportBadge` | Badge de deporte con emoji (⚽ 🏀 🎾 🏐 …) |
| `StatusBadge` | Estado de usuario/evento (Activo · Finalizado · Cancelado · Inactivo) |
| `ConfirmModal` | Modal de confirmación con ícono y animación |
| `EmptyState` | Estado vacío con emoji ilustrativo y mensaje |
| `LoadingSpinner` | Spinner (balón que rebota) con colores de marca |

---

## 7. Reglas de producto

- **Todos los textos en español.**
- **Nunca mostrar IDs numéricos** al usuario (se usan solo internamente para navegación/API).
- Pensado para pantallas de **1280px+**, con degradación razonable a tablet/móvil.
- **Sin librerías CSS pesadas** (Material UI, Ant Design): solo CSS puro + variables.
- No se altera la lógica de negocio ni las llamadas a la API: este rediseño es **solo visual**.
