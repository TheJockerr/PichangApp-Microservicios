package com.pichangapp.core

/**
 * Wrapper para resultados de repositorio (capa domain/data).
 * Distinto de UiState: este vive en data/domain, no en UI.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T)              : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    data object Loading                              : NetworkResult<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: retrofit2.HttpException) {
        NetworkResult.Error("HTTP ${e.code()}: ${e.message()}", e.code())
    } catch (e: java.io.IOException) {
        NetworkResult.Error("Sin conexión a internet")
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Error desconocido")
    }
}
