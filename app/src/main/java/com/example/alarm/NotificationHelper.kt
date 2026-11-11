package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "study_alarms_channel"
    const val CHANNEL_NAME = "تنبيهات المذاكرة"

    const val ACTION_STOP_ALARM = "com.example.ACTION_STOP_ALARM"
    const val ACTION_STUDY_ALARM = "com.example.ACTION_STUDY_ALARM"

    const val EXTRA_NOTIFICATION_ID = "extra_notif_id"
    const val EXTRA_TARGET_TYPE = "extra_target_type"
    const val EXTRA_TARGET_ID = "extra_target_id"
    const val EXTRA_SUBJECT_ID = "extra_subject_id"
    const val EXTRA_IS_EVALUATION = "extra_is_evaluation"
    const val EXTRA_TOPIC_TITLE = "extra_topic_title"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "قناة تنبيهات ومواعيد المذاكرة"
                enableVibration(true)
                setSound(null, null) // Audio is managed reliably by RingtonePlayerManager
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAlarmNotification(
        context: Context,
        notificationId: Int,
        title: String,
        targetType: String,
        targetId: String?,
        subjectId: String?,
        isEvaluation: Boolean
    ) {
        createNotificationChannel(context)

        // Intent for clicking the notification body (opens content & stops sound)
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TARGET_TYPE, targetType)
            putExtra(EXTRA_TARGET_ID, targetId)
            putExtra(EXTRA_SUBJECT_ID, subjectId)
            putExtra(EXTRA_IS_EVALUATION, isEvaluation)
            putExtra(EXTRA_TOPIC_TITLE, title)
            putExtra("STOP_ALARM_ON_CLICK", true)
        }

        val clickPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for the "إيقاف" action button (silences audio and dismisses)
        val stopIntent = Intent(context, StudyAlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 50000,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(clickPendingIntent)
            .setFullScreenIntent(clickPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(0, "إيقاف", stopPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS may not be granted yet
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
}
