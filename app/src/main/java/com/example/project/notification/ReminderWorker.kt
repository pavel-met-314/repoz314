package com.example.project.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "appointment_reminders"
        const val KEY_SERVICE_NAME = "service_name"
        const val KEY_DATE = "date"
        const val KEY_TIME = "time"
    }

    override fun doWork(): Result {
        val serviceName = inputData.getString(KEY_SERVICE_NAME) ?: "Запись"
        val date = inputData.getString(KEY_DATE) ?: ""
        val time = inputData.getString(KEY_TIME) ?: ""

        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Напоминание о записи")
            .setContentText("Завтра в $time — $serviceName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Напоминаем: завтра ($date) в $time у вас запись на услугу «$serviceName»")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о записях",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления за день до визита"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

