package cl.duoc.pichangapp.data.remote

import cl.duoc.pichangapp.data.model.HistorialVisibleRequest
import cl.duoc.pichangapp.data.model.HistorialVisibleResponse
import cl.duoc.pichangapp.data.model.PasswordUpdateRequest
import cl.duoc.pichangapp.data.model.PerfilPublicoDto
import cl.duoc.pichangapp.data.model.UserDto
import cl.duoc.pichangapp.data.model.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("api/v1/users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): Response<UserDto>

    @PUT("api/v1/users/{id}")
    suspend fun updateUserProfile(@Path("id") id: String, @Body request: UserUpdateRequest): Response<UserDto>

    // ── Endpoints nuevos (el userId se toma del JWT en el backend) ──────────────

    /** Cambia la contraseña del usuario autenticado. */
    @PUT("api/v1/users/change-password")
    suspend fun changePassword(@Body request: PasswordUpdateRequest): Response<Unit>

    /** Actualiza la visibilidad del historial de karma del usuario autenticado. */
    @PUT("api/v1/users/historial-visible")
    suspend fun setHistorialVisible(@Body request: HistorialVisibleRequest): Response<HistorialVisibleResponse>

    /** Busca usuarios por nombre o apellido. Devuelve perfiles públicos. */
    @GET("api/v1/users/buscar")
    suspend fun buscarUsuarios(@Query("nombre") nombre: String): List<PerfilPublicoDto>

    /** Perfil público por correo. */
    @GET("api/v1/users/perfil-publico/{correo}")
    suspend fun getPerfilPublico(@Path("correo") correo: String): PerfilPublicoDto

    /** Elimina la cuenta propia del usuario autenticado. */
    @DELETE("api/v1/users/cuenta")
    suspend fun eliminarCuenta(): Response<Unit>
}
