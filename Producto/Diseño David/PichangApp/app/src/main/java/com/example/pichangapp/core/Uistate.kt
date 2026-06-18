package com.pichangapp.core

/**
 * Estado genérico de UI para cualquier operación asíncrona.
 * Úsalo como tipo del StateFlow en cada ViewModel.
 *
 * Ejemplo:
 *   val uiState: StateFlow<UiState<List<Event>>> = ...
 */
sealed class UiState<out T> {

    /** Estado inicial antes de lanzar cualquier operación. */
    data object Idle : UiState<Nothing>()

    /** Operación en curso — muestra skeleton / shimmer. */
    data object Loading : UiState<Nothing>()

    /** Operación exitosa con datos. */
    data class Success<T>(val data: T) : UiState<T>()

    /** Error con mensaje y opción de retry. */
    data class Error(
        val message  : String,
        val code     : Int?    = null,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}

/** Extensiones de conveniencia */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

inline fun <T> UiState<T>.onError(action: (UiState.Error) -> Unit): UiState<T> {
    if (this is UiState.Error) action(this)
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}

val <T> UiState<T>.isLoading get() = this is UiState.Loading
val <T> UiState<T>.isSuccess get() = this is UiState.Success
val <T> UiState<T>.isError   get() = this is UiState.Error