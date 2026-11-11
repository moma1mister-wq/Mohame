package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object RingtonePlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    @Synchronized
    fun playAlarm(context: Context, customUriString: String?) {
        stopAlarm()
        try {
            val audioUri = if (!customUriString.isNullOrEmpty()) {
                Uri.parse(customUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, audioUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error playing alarm sound", e)
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mediaPlayer = MediaPlayer.create(context, fallbackUri)?.apply {
                    isLooping = false
                    start()
                }
            } catch (fallbackEx: Exception) {
                Log.e("RingtonePlayer", "Error playing fallback sound", fallbackEx)
            }
        }
    }

    @Synchronized
    fun playPreview(context: Context, customUriString: String?, onComplete: () -> Unit = {}) {
        stopAlarm()
        try {
            val audioUri = if (!customUriString.isNullOrEmpty()) {
                Uri.parse(customUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, audioUri)
                isLooping = false
                setOnCompletionListener {
                    stopAlarm()
                    onComplete()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error playing preview", e)
            onComplete()
        }
    }

    @Synchronized
    fun stopAlarm() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error stopping alarm", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun vibrate(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 300, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 300, 500), -1)
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "Error vibrating", e)
        }
    }
}
