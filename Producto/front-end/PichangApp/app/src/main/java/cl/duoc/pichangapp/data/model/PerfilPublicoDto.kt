package cl.duoc.pichangapp.data.model

import androidx.compose.runtime.Stable

/**
 * Perfil público de otro usuario (lo que devuelve el backend en
 * GET /api/v1/users/buscar y /api/v1/users/perfil-publico/{correo}).
 * No incluye correo ni id por privacidad.
 */
@Stable
data class PerfilPublicoDto(
    val correo: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val karmaScore: Int = 0,
    val categoriaKarma: String? = null,
    val historialVisible: Boolean = true
)
