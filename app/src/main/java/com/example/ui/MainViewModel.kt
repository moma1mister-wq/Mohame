package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.alarm.AlarmScheduler
import com.example.data.model.NotificationLogEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.ScheduleEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as StudyApplication
    private val repo = app.repository
    private val prefs = app.preferencesManager

    val isLoggedIn: StateFlow<Boolean> = prefs.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val username: StateFlow<String> = prefs.usernameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "طالب الأكاديمية")

    val themeMode: StateFlow<String> = prefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "light")

    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val customRingtoneUri: StateFlow<String?> = prefs.ringtoneUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastContent: StateFlow<Triple<String, String, Boolean>?> = prefs.lastContentFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<SubjectEntity>> = repo.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleEntity>> = repo.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingSchedule: StateFlow<ScheduleEntity?> = repo.getActiveSchedules()
        .map { list ->
            val nowCal = Calendar.getInstance()
            val currentDay = nowCal.get(Calendar.DAY_OF_WEEK)
            val currentHour = nowCal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = nowCal.get(Calendar.MINUTE)

            list.firstOrNull { sch ->
                (sch.repeatType == "daily" || sch.dayOfWeek == currentDay) &&
                        (sch.hour > currentHour || (sch.hour == currentHour && sch.minute >= currentMinute))
            } ?: list.firstOrNull()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repo.getAllQuizAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentQuizzes: StateFlow<List<QuizAttemptEntity>> = repo.getRecentQuizAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val averageScore: StateFlow<Float?> = repo.getAverageScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val averageProductivity: StateFlow<Float?> = repo.getAverageProductivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalStudyMinutes: StateFlow<Int?> = repo.getTotalStudyMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = repo.getAllNotificationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(user: String) {
        viewModelScope.launch {
            prefs.setLogin(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.logout()
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            prefs.setTheme(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setNotificationsEnabled(enabled)
        }
    }

    fun setCustomRingtone(uri: String?) {
        viewModelScope.launch {
            prefs.setRingtoneUri(uri)
        }
    }

    fun setLastContent(id: String, title: String, isVideo: Boolean, subjTitle: String = "") {
        viewModelScope.launch {
            prefs.setLastContent(id, title, isVideo, subjTitle)
        }
    }

    fun toggleVideoWatched(video: VideoEntity) {
        viewModelScope.launch {
            repo.setVideoWatched(video.id, !video.isWatched)
        }
    }

    fun updateVideoLinkedPdf(videoId: String, pdfId: String?) {
        viewModelScope.launch {
            repo.linkVideoPdf(videoId, pdfId)
        }
    }

    fun addSubject(title: String, sem: String, icon: String) {
        viewModelScope.launch {
            val newId = "custom_${System.currentTimeMillis()}"
            val newSubject = SubjectEntity(
                id = newId,
                title = title,
                semester = sem,
                icon = icon,
                colorHex = "#7C3AED",
                orderIndex = 99
            )
            repo.insertSubject(newSubject)
        }
    }

    fun editSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repo.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repo.deleteSubject(subject)
        }
    }

    fun addSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            val id = repo.insertSchedule(schedule)
            val inserted = schedule.copy(id = id)
            AlarmScheduler.scheduleAlarm(getApplication(), inserted)
        }
    }

    fun editSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repo.updateSchedule(schedule)
            AlarmScheduler.cancelAlarm(getApplication(), schedule.id)
            if (schedule.isActive) {
                AlarmScheduler.scheduleAlarm(getApplication(), schedule)
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), schedule.id)
            repo.deleteSchedule(schedule)
        }
    }

    fun saveQuizAttempt(attempt: QuizAttemptEntity) {
        viewModelScope.launch {
            repo.insertQuizAttempt(attempt)
            repo.insertStudyLog(30, attempt.subjectId)
        }
    }

    fun clearNotificationLogs() {
        viewModelScope.launch {
            repo.clearNotificationLogs()
        }
    }
}
