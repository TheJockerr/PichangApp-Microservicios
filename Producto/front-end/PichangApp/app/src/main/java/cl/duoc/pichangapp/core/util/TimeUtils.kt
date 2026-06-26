package cl.duoc.pichangapp.core.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Utilidades de formato de tiempo relativo en español ("hace X tiempo").
 * Tolera fechas ISO con zona (Instant) o locales (LocalDateTime).
 */
object TimeUtils {

    private fun parseToInstant(iso: String): Instant? {
        return try {
            Instant.parse(iso) // formato con Z (UTC)
        } catch (_: Exception) {
            try {
                // LocalDateTime sin zona → se asume zona del dispositivo
                LocalDateTime.parse(iso).atZone(ZoneId.systemDefault()).toInstant()
            } catch (_: Exception) {
                null
            }
        }
    }

    /** Devuelve un texto tipo "hace 5 minutos". Si no se puede parsear, devuelve "". */
    fun relativeTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val instant = parseToInstant(iso) ?: return iso.substringBefore("T")
        val d = Duration.between(instant, Instant.now())
        val secs = d.seconds
        return when {
            secs < 0 -> "recién"
            secs < 60 -> "hace un momento"
            secs < 3600 -> {
                val m = secs / 60
                if (m == 1L) "hace 1 minuto" else "hace $m minutos"
            }
            secs < 86400 -> {
                val h = secs / 3600
                if (h == 1L) "hace 1 hora" else "hace $h horas"
            }
            secs < 604800 -> {
                val days = secs / 86400
                if (days == 1L) "ayer" else "hace $days días"
            }
            else -> {
                val dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        }
    }
}
