package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.ScheduleEntity
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(context: Context, schedule: ScheduleEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerMillis = calculateNextTriggerMillis(schedule)
        if (triggerMillis <= System.currentTimeMillis()) return

        // 1. Start of study alarm
        val startTitle = "مذاكرة ${schedule.subjectName}"
        val startIntent = Intent(context, StudyAlarmReceiver::class.java).apply {
            action = NotificationHelper.ACTION_STUDY_ALARM
            putExtra("EXTRA_SCHEDULE_ID", schedule.id)
            putExtra("EXTRA_TITLE", startTitle)
            putExtra("EXTRA_SUBJECT_ID", schedule.subjectId)
            putExtra("EXTRA_TARGET_TYPE", schedule.targetType)
            putExtra("EXTRA_TARGET_ID", schedule.targetId)
            putExtra("EXTRA_RINGTONE_URI", schedule.customRingtoneUri)
            putExtra("EXTRA_IS_EVALUATION", false)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, (schedule.id * 10).toInt())
        }

        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            (schedule.id * 10).toInt(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(alarmManager, triggerMillis, startPendingIntent)

        // 2. End of study evaluation alarm
        val evalMillis = triggerMillis + (schedule.durationMinutes.coerceAtLeast(1) * 60_000L)
        val evalTitle = "تقييم: ${schedule.subjectName}"
        val evalIntent = Intent(context, StudyAlarmReceiver::class.java).apply {
            action = NotificationHelper.ACTION_STUDY_ALARM
            putExtra("EXTRA_SCHEDULE_ID", schedule.id)
            putExtra("EXTRA_TITLE", evalTitle)
            putExtra("EXTRA_SUBJECT_ID", schedule.subjectId)
            putExtra("EXTRA_TARGET_TYPE", schedule.targetType)
            putExtra("EXTRA_TARGET_ID", schedule.targetId)
            putExtra("EXTRA_RINGTONE_URI", schedule.customRingtoneUri)
            putExtra("EXTRA_IS_EVALUATION", true)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, (schedule.id * 10 + 1).toInt())
        }

        val evalPendingIntent = PendingIntent.getBroadcast(
            context,
            (schedule.id * 10 + 1).toInt(),
            evalIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(alarmManager, evalMillis, evalPendingIntent)
    }

    fun scheduleTestAlarm(context: Context, secondsFromNow: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerMillis = System.currentTimeMillis() + (secondsFromNow * 1000L)

        val testIntent = Intent(context, StudyAlarmReceiver::class.java).apply {
            action = NotificationHelper.ACTION_STUDY_ALARM
            putExtra("EXTRA_TITLE", "مذاكرة تجريبية")
            putExtra("EXTRA_SUBJECT_ID", "computing_fundamental")
            putExtra("EXTRA_IS_EVALUATION", false)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 99999)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            99999,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(alarmManager, triggerMillis, pendingIntent)
    }

    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startIntent = Intent(context, StudyAlarmReceiver::class.java).apply {
            action = NotificationHelper.ACTION_STUDY_ALARM
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId * 10).toInt(),
            startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (startPendingIntent != null) {
            alarmManager.cancel(startPendingIntent)
            startPendingIntent.cancel()
        }

        val evalPendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId * 10 + 1).toInt(),
            startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (evalPendingIntent != null) {
            alarmManager.cancel(evalPendingIntent)
            evalPendingIntent.cancel()
        }
    }

    private fun scheduleExact(alarmManager: AlarmManager, triggerMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun calculateNextTriggerMillis(schedule: ScheduleEntity): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (schedule.repeatType) {
            "daily" -> {
                if (target.before(now)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "weekly" -> {
                // Calendar.SUNDAY is 1, Calendar.SATURDAY is 7
                target.set(Calendar.DAY_OF_WEEK, schedule.dayOfWeek)
                if (target.before(now)) {
                    target.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            else -> {
                // none (single event)
                if (schedule.dateEpochDay > 0) {
                    val epochDaysToMillis = schedule.dateEpochDay * 86_400_000L
                    val dayCal = Calendar.getInstance().apply { timeInMillis = epochDaysToMillis }
                    target.set(Calendar.YEAR, dayCal.get(Calendar.YEAR))
                    target.set(Calendar.MONTH, dayCal.get(Calendar.MONTH))
                    target.set(Calendar.DAY_OF_MONTH, dayCal.get(Calendar.DAY_OF_MONTH))
                } else if (target.before(now)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return target.timeInMillis
    }
}
