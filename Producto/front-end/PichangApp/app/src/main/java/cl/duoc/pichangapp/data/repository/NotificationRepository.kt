package cl.duoc.pichangapp.data.repository

import cl.duoc.pichangapp.core.util.Result
import cl.duoc.pichangapp.data.model.NotificationDto
import cl.duoc.pichangapp.data.remote.NotificationApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi
) {
    fun getNotifications(userId: String): Flow<Result<List<NotificationDto>>> = flow {
        emit(Result.Loading)
        try {
            val response = notificationApi.getNotifications(userId = userId, page = 0, size = 20)
            if (response.isSuccessful && response.body() != null) {
                // Extraer la lista "content" del objeto paginado de Spring Data
                emit(Result.Success(response.body()!!.content))
            } else {
                emit(Result.Error("Error al obtener notificaciones: ${response.code()}"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Error de conexión"))
        } catch (e: HttpException) {
            emit(Result.Error("Error HTTP: ${e.code()}"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Error desconocido"))
        }
    }
}
