package com.pichangapp.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel base con helpers para lanzar coroutines con manejo
 * automático de errores HTTP, red e inesperados.
 */
abstract class BaseViewModel : ViewModel() {

    // ── Error de snackbar / toast global ─────────────────────────────────────
    private val _globalError = MutableStateFlow<String?>(null)
    val globalError = _globalError.asStateFlow()

    fun clearGlobalError() { _globalError.value = null }

    // ── Launch helper ─────────────────────────────────────────────────────────
    /**
     * Lanza un bloque en el dispatcher indicado y actualiza [stateFlow] con
     * Loading → Success | Error automáticamente.
     *
     * @param stateFlow  MutableStateFlow a actualizar
     * @param dispatcher IO por defecto
     * @param block      Suspending block que retorna T
     */
    protected fun <T> launchWithState(
        stateFlow  : MutableStateFlow<UiState<T>>,
        dispatcher : CoroutineDispatcher = Dispatchers.IO,
        block      : suspend () -> T
    ) {
        viewModelScope.launch(dispatcher) {
            stateFlow.value = UiState.Loading
            try {
                val result = block()
                stateFlow.value = UiState.Success(result)
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesión expirada. Vuelve a iniciar sesión."
                    403  -> "No tienes permiso para realizar esta acción."
                    404  -> "Recurso no encontrado."
                    500  -> "Error en el servidor. Intenta más tarde."
                    else -> "Error HTTP ${e.code()}"
                }
                stateFlow.value = UiState.Error(msg, code = e.code(), throwable = e)
            } catch (e: IOException) {
                stateFlow.value = UiState.Error(
                    message   = "Sin conexión. Revisa tu internet.",
                    throwable = e
                )
            } catch (e: Exception) {
                stateFlow.value = UiState.Error(
                    message   = e.message ?: "Ocurrió un error inesperado.",
                    throwable = e
                )
            }
        }
    }

    /** Emite un error global (para Snackbar) sin cambiar el state flow principal. */
    protected fun emitGlobalError(message: String) {
        _globalError.value = message
    }
}