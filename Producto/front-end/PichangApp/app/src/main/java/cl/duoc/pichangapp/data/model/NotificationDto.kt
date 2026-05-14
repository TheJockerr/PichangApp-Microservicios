package cl.duoc.pichangapp.data.model

data class NotificationDto(
    val id: Int?,
    val userId: String? = null,
    val title: String,
    val body: String,
    val type: String, // KARMA_INCREASE, KARMA_DECREASE, EVENT_REMINDER, EVENT_CANCELLED, NEW_EVENT_NEARBY
    val status: String? = null,
    val createdAt: String? = null
)

/**
 * Wrapper para la respuesta paginada de Spring Data Page.
 * El backend retorna: {"content": [...], "totalPages": X, "totalElements": X, "number": X, "size": X, ...}
 */
data class NotificationPageResponse(
    val content: List<NotificationDto> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val number: Int = 0,
    val size: Int = 0
)

data class DeviceTokenRequest(
    val userId: String,
    val token: String
)

data class NotificationSendRequest(
    val userId: String,
    val title: String,
    val body: String,
    val type: String
)
