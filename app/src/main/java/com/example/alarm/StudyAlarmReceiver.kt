package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StudyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == NotificationHelper.ACTION_STOP_ALARM) {
            RingtonePlayerManager.stopAlarm()
            val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)
            if (notifId > 0) {
                NotificationHelper.cancelNotification(context, notifId)
            }
            return
        }

        if (action == NotificationHelper.ACTION_STUDY_ALARM) {
            val title = intent.getStringExtra("EXTRA_TITLE") ?: "مذاكرة"
            val subjectId = intent.getStringExtra("EXTRA_SUBJECT_ID")
            val targetType = intent.getStringExtra("EXTRA_TARGET_TYPE") ?: "general"
            val targetId = intent.getStringExtra("EXTRA_TARGET_ID")
            val isEvaluation = intent.getBooleanExtra("EXTRA_IS_EVALUATION", false)
            val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 1001)
            val scheduleId = intent.getLongExtra("EXTRA_SCHEDULE_ID", -1L)
            val itemRingtone = intent.getStringExtra("EXTRA_RINGTONE_URI")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = PreferencesManager(context)
                    val enabled = prefs.notificationsEnabledFlow.firstOrNull() ?: true
                    if (!enabled) return@launch

                    val globalRingtone = prefs.ringtoneUriFlow.firstOrNull()
                    val ringtoneToPlay = itemRingtone ?: globalRingtone

                    // 1. Play sound
                    RingtonePlayerManager.playAlarm(context, ringtoneToPlay)

                    // 2. Vibrate
                    RingtonePlayerManager.vibrate(context)

                    // 3. Show high priority notification
                    NotificationHelper.showAlarmNotification(
                        context = context,
                        notificationId = notifId,
                        title = title,
                        targetType = targetType,
                        targetId = targetId,
                        subjectId = subjectId,
                        isEvaluation = isEvaluation
                    )

                    // 4. Log in database
                    val db = AppDatabase.getDatabase(context)
                    db.notificationLogDao().insertLog(
                        com.example.data.model.NotificationLogEntity(
                            title = title,
                            type = if (isEvaluation) "تقييم" else "مذاكرة"
                        )
                    )

                    // 5. If repeating schedule, re-schedule for the next cycle
                    if (scheduleId > 0) {
                        val schedule = db.scheduleDao().getScheduleById(scheduleId)
                        if (schedule != null && schedule.isActive && schedule.repeatType != "none") {
                            AlarmScheduler.scheduleAlarm(context, schedule)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
