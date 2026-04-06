package com.example.project.notification

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    /**
     * Планирует уведомление за 1 день до записи (в то же время).
     * Если до записи меньше суток — не планирует.
     *
     * @param date   дата в формате "yyyy-MM-dd"
     * @param time   время в формате "HH:mm"
     */
    fun schedule(
        context: Context,
        appointmentId: String,
        serviceName: String,
        date: String,
        time: String
    ) {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val appointmentDateTime = formatter.parse("$date $time") ?: return

            // Напоминание — за 24 часа до записи
            val reminderTime = appointmentDateTime.time - TimeUnit.HOURS.toMillis(24)
            val delay = reminderTime - System.currentTimeMillis()

            if (delay <= 0) return  // До напоминания меньше суток — пропускаем

            // Форматируем дату для отображения
            val displayFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val displayDate = displayFormatter.format(appointmentDateTime)

            val inputData = Data.Builder()
                .putString(ReminderWorker.KEY_SERVICE_NAME, serviceName)
                .putString(ReminderWorker.KEY_DATE, displayDate)
                .putString(ReminderWorker.KEY_TIME, time)
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_$appointmentId")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        } catch (_: Exception) {}
    }

    /**
     * Отменяет запланированное уведомление (при отмене записи).
     */
    fun cancel(context: Context, appointmentId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$appointmentId")
    }
}

