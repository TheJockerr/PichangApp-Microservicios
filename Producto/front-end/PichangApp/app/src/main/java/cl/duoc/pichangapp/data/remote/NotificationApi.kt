package cl.duoc.pichangapp.data.remote

import cl.duoc.pichangapp.data.model.DeviceTokenRequest
import cl.duoc.pichangapp.data.model.NotificationPageResponse
import cl.duoc.pichangapp.data.model.NotificationSendRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @POST("api/v1/notifications/device-token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<Void>

    @POST("api/v1/notifications/send")
    suspend fun sendNotification(@Body request: NotificationSendRequest): Response<Void>

    @GET("api/v1/notifications/{id}")
    suspend fun getNotifications(
        @Path("id") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<NotificationPageResponse>
}
