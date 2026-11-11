package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class StudySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val activeSchedules = db.scheduleDao().getActiveSchedules().first()
            for (schedule in activeSchedules) {
                AlarmScheduler.scheduleAlarm(applicationContext, schedule)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "StudySyncPeriodicWorker"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<StudySyncWorker>(6, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
