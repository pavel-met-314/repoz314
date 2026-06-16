package com.example.project.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object AppointmentStatusScheduler {

    private const val WORK_NAME = "appointment_status_check"
    private const val PREFS_NAME = "appointment_status_cache"

    /** Минимальный интервал WorkManager — 15 минут. Бесплатная замена Cloud Functions push. */
    fun schedule(context: Context, clientId: String) {
        val request = PeriodicWorkRequestBuilder<AppointmentStatusWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf(AppointmentStatusWorker.KEY_CLIENT_ID to clientId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
