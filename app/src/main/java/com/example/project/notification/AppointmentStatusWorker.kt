package com.example.project.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Appointment
import kotlinx.coroutines.tasks.await

/**
 * Периодически проверяет статус записей клиента в Firestore (Spark / без Cloud Functions).
 * При смене active → cancelled показывает локальное уведомление.
 */
class AppointmentStatusWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val clientId = inputData.getString(KEY_CLIENT_ID) ?: return Result.failure()

        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("appointments")
                .whereEqualTo("clientId", clientId)
                .get()
                .await()

            val appointments = snapshot.documents.mapNotNull { it.toObject(Appointment::class.java) }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val initialized = prefs.getBoolean(KEY_INITIALIZED, false)
            val previous = parseStatuses(prefs.getString(KEY_STATUSES, "") ?: "")

            if (initialized) {
                appointments.forEach { appointment ->
                    if (previous[appointment.id] == "active" && appointment.status == "cancelled") {
                        showCancellationNotification(appointment)
                        ReminderScheduler.cancel(
                            context,
                            "${appointment.date}_${appointment.time}_${appointment.serviceId}"
                        )
                    }
                }
            }

            val current = appointments.associate { it.id to it.status }
            prefs.edit()
                .putString(KEY_STATUSES, serializeStatuses(current))
                .putBoolean(KEY_INITIALIZED, true)
                .apply()

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun showCancellationNotification(appointment: Appointment) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Запись отменена")
            .setContentText("Запись на ${appointment.date} в ${appointment.time} (${appointment.serviceName}) отменена")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(appointment.id.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Статус записей",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления об отмене записи администратором"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_CLIENT_ID = "client_id"
        const val CHANNEL_ID = "appointment_status"
        private const val PREFS_NAME = "appointment_status_cache"
        private const val KEY_STATUSES = "statuses"
        private const val KEY_INITIALIZED = "initialized"

        private fun serializeStatuses(statuses: Map<String, String>): String =
            statuses.entries.joinToString("|") { "${it.key}:${it.value}" }

        private fun parseStatuses(raw: String): Map<String, String> {
            if (raw.isBlank()) return emptyMap()
            return raw.split("|").mapNotNull { part ->
                val idx = part.indexOf(':')
                if (idx <= 0) null else part.substring(0, idx) to part.substring(idx + 1)
            }.toMap()
        }
    }
}
