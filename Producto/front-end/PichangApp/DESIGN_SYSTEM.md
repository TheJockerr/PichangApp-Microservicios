# PichangApp — Sistema de Diseño

> Red social deportiva. Energética pero **premium**: la sofisticación visual de una
> app comercial real (Spotify), con la calidez gamificada de Duolingo y la limpieza
> de feed de Instagram. No "infantil académico".

**Inspiración dominante:** Spotify (dark mode, tarjetas grandes, tipografía bold,
gradientes sutiles tipo carátula de álbum) · **acentos:** Duolingo (gamificación,
redondeado, color en acentos) · **layout de feed:** Instagram (tarjetas limpias,
scroll vertical, tabs con subrayado).

---

## 1. Soporte de tema dual (Dark / Light)

El usuario elige **Sistema / Claro / Oscuro** desde *Configuración → Apariencia*.
La preferencia se persiste en **DataStore** vía `ThemeManager` (expuesto como
`StateFlow<ThemeMode>`) y se aplica en `MainActivity` mediante `PichangAppTheme`.

- **El modo oscuro es el predeterminado** en la primera instalación.
- El cambio de tema se **anima** suavemente (`animateColorAsState` sobre el esquema),
  nunca es un corte abrupto.

```
enum class ThemeMode { SYSTEM, LIGHT, DARK }
```

---

## 2. Paletas

### Dark Mode (dominante — estilo Spotify)

| Token              | Hex        | Uso                                            |
| ------------------ | ---------- | ---------------------------------------------- |
| `Background`       | `#121212`  | Fondo base (negro Spotify)                     |
| `Surface`          | `#1E1E1E`  | Superficies                                    |
| `SurfaceElevated`  | `#282828`  | Cards / inputs                                 |
| `Primary`          | `#2ECC71`  | Verde vibrante de marca                        |
| `PrimaryVariant`   | `#1DB954`  | Verde Spotify-like (gradientes, énfasis)       |
| `Accent`           | `#FF6F00`  | Naranja energía deportiva                      |
| `OnBackground`     | `#FFFFFF`  | Texto principal                                |
| `OnSurfaceMuted`   | `#B3B3B3`  | Texto secundario (gris Spotify)                |
| `Divider`          | `#2A2A2A`  | Separadores muy sutiles                        |
| `Error`            | `#FF5252`  | Errores / acciones destructivas                |

### Light Mode

| Token              | Hex        | Uso                       |
| ------------------ | ---------- | ------------------------- |
| `Background`       | `#FAFAFA`  | Fondo base                |
| `Surface`          | `#FFFFFF`  | Superficies               |
| `SurfaceElevated`  | `#F2F2F2`  | Cards / inputs            |
| `Primary`          | `#1DB954`  | Verde de marca            |
| `Accent`           | `#FF6F00`  | Naranja energía           |
| `OnBackground`     | `#121212`  | Texto principal           |
| `OnSurfaceMuted`   | `#6B6B6B`  | Texto secundario          |
| `Divider`          | `#E0E0E0`  | Separadores               |

### Mapeo a Material 3 `ColorScheme`

`Primary→primary`, `Background→background`, `SurfaceElevated→surfaceVariant`
(usado para cards/inputs), `OnSurfaceMuted→onSurfaceVariant`, `Accent→tertiary`,
`Divider→outlineVariant`. Esto permite que los componentes de Material se vean
nativamente correctos mientras conservamos la identidad de marca.

### Colores por deporte (carátulas / chips)

Se conservan los acentos por deporte (fútbol, pádel, tenis, esports, básquet,
vóley) para generar gradientes tipo "carátula de álbum" en las tarjetas de evento.
La función `sportColor()` / `sportEmoji()` mapea texto libre del backend.

---

## 3. Tipografía

Fuente del sistema con **pesos marcados**. Números/estadísticas (karma, contadores)
en **ExtraBold** y tamaño grande — deben sentirse como métricas de *Spotify Wrapped*.

| Rol        | Tamaño | Peso       | Ejemplo                          |
| ---------- | ------ | ---------- | -------------------------------- |
| Display    | 32 sp  | ExtraBold  | "Hola, {nombre}" / saludos       |
| Headline   | 24 sp  | Bold       | Títulos de pantalla              |
| Title      | 18 sp  | SemiBold   | Títulos de sección / card        |
| Body       | 14 sp  | Regular    | Texto general                    |
| Caption    | 12 sp  | Medium     | Metadatos, timestamps            |
| **Stat**   | 48–64+ sp | ExtraBold | Karma / contadores animados      |

Mapeo M3: `displayLarge` (32, ExtraBold), `headlineLarge` (24, Bold),
`titleLarge` (18, SemiBold), `bodyMedium` (14), `bodySmall`/`labelMedium` (12).
Estilo extra `statDisplay` (64sp ExtraBold) definido en `Type.kt`.

---

## 4. Layout, forma y elevación

- **Tarjetas grandes**, esquinas muy redondeadas (**20–24 dp**), **sin bordes**,
  solo elevación/sombra sutil (`tonalElevation` + sombra suave).
- **Imágenes/avatares grandes** y prominentes.
- Mucho **espacio negativo**, jerarquía visual clara.
- Scroll vertical con **secciones tipo feed** (como Home de Spotify: secciones
  horizontales scrolleables "Hecho para ti").
- **Botones primarios pill-shaped** (totalmente redondeados), full-width en CTAs.
- **Gradientes sutiles** en headers (de Primary/color de deporte → transparente,
  estilo cabecera de álbum de Spotify).

**Escala de radios** (`Shape.kt`): small 12 · medium 16 · large 20 · xl 24 · pill 50%.
**Espaciado** base 4 dp; gutters de pantalla 20 dp; separación entre cards 16 dp.

---

## 5. Componentes base (`ui/components/`)

| Componente          | Descripción                                                        |
| ------------------- | ------------------------------------------------------------------ |
| `PichangCard`       | Card base: esquinas 20 dp, elevación sutil, color de superficie.   |
| `PichangButton`     | Pill-shaped. Variantes: Primary (relleno), Secondary (outline), Text. |
| `StatDisplay`       | Número grande ExtraBold + label (estilo Spotify Wrapped).          |
| `AnimatedCounter`   | Número que anima de 0 → valor real al aparecer.                    |
| `SectionHeader`     | Título de sección + "Ver todo" opcional.                           |
| `HorizontalCardRow` | `LazyRow` reutilizable para secciones tipo "Eventos cerca de ti".  |
| `Avatar`            | Circular; iniciales con color generado por hash del nombre, o foto.|
| `CategoryChip`      | Chip de categoría/deporte con color e ícono.                       |
| `PichangBottomSheet`| Sheet genérico para acciones rápidas (unirse, confirmar).          |

---

## 6. Bottom Navigation

- Iconos **outline** (no seleccionado) / **filled** (seleccionado).
- **Sin texto** en ítems inactivos (estilo Instagram).
- Indicador de selección **minimalista**: cambio de color del ícono + punto pequeño,
  **no** fondo de pill.
- Contenedor con color `surface`, sin elevación pesada.

---

## 7. Motion / Animación

- **Navegación:** fade + slide sutil entre pantallas.
- **Listas:** `AnimatedVisibility` con delay escalonado por índice (stagger).
- **Contadores:** `AnimatedCounter` en todos los números de karma y stats.
- **Cambio de tema:** transición de color animada, no corte abrupto.
- Curvas: `spring` (stiffness medium-low) para entradas; `tween` 300 ms para color.

---

## 8. Principios de marca (qué evitar)

- ❌ Verde institucional plano / azul corporativo del diseño anterior.
- ❌ Bordes duros, cards pequeñas, sombras grises pesadas.
- ❌ Texto en ítems de navegación inactivos.
- ❌ Mostrar **IDs numéricos** al usuario — siempre nombres/etiquetas.
- ✅ Negro Spotify, verde vibrante, tarjetas grandes, números enormes, gradientes sutiles.
