package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.alarm.NotificationHelper
import com.example.alarm.RingtonePlayerManager
import com.example.ui.MainViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var intentTargetId by mutableStateOf<String?>(null)
    private var intentIsVideo by mutableStateOf(true)
    private var intentIsEvaluation by mutableStateOf(false)
    private var intentSubjectId by mutableStateOf<String?>(null)
    private var intentTopicTitle by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        val app = application as StudyApplication

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        viewModel = viewModel,
                        application = app,
                        intentTargetId = intentTargetId,
                        intentIsVideo = intentIsVideo,
                        intentIsEvaluation = intentIsEvaluation,
                        intentSubjectId = intentSubjectId,
                        intentTopicTitle = intentTopicTitle
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        if (intent.getBooleanExtra("STOP_ALARM_ON_CLICK", false)) {
            RingtonePlayerManager.stopAlarm()
            val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)
            if (notifId > 0) {
                NotificationHelper.cancelNotification(this, notifId)
            }
        }

        intentTargetId = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_ID)
        val type = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_TYPE)
        intentIsVideo = type != "pdf"
        intentIsEvaluation = intent.getBooleanExtra(NotificationHelper.EXTRA_IS_EVALUATION, false)
        intentSubjectId = intent.getStringExtra(NotificationHelper.EXTRA_SUBJECT_ID)
        intentTopicTitle = intent.getStringExtra(NotificationHelper.EXTRA_TOPIC_TITLE)
    }
}
