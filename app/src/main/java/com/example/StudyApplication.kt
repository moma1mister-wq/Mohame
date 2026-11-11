package com.example

import android.app.Application
import com.example.alarm.NotificationHelper
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.StudyRepository
import com.example.worker.StudySyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: StudyRepository
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = StudyRepository(database)
        preferencesManager = PreferencesManager(this)

        NotificationHelper.createNotificationChannel(this)
        try {
            StudySyncWorker.enqueue(this)
        } catch (_: Exception) {
        }

        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureDataLoaded()
        }
    }
}
