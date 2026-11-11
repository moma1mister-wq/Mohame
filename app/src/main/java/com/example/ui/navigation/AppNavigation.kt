package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.StudyApplication
import com.example.alarm.RingtonePlayerManager
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.ui.components.ProductivityDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ContentPlayerScreen
import com.example.ui.screens.EvaluationsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationLogScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.flow.first

sealed class Screen {
    object Home : Screen()
    object Subjects : Screen()
    object Schedule : Screen()
    object Evaluations : Screen()
    object Settings : Screen()
    data class SubjectDetail(val subjectId: String) : Screen()
    data class ContentPlayer(
        val id: String,
        val isVideo: Boolean,
        val title: String,
        val linkedId: String? = null,
        val linkedTitle: String? = null
    ) : Screen()
    data class Quiz(
        val subjectId: String,
        val subjectName: String,
        val chapterTitle: String,
        val pdfTitle: String,
        val productivityRating: Int
    ) : Screen()
    object NotificationLogs : Screen()
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    application: StudyApplication,
    intentTargetId: String? = null,
    intentIsVideo: Boolean = true,
    intentIsEvaluation: Boolean = false,
    intentSubjectId: String? = null,
    intentTopicTitle: String? = null
) {
    // Force Right-to-Left (RTL) for Arabic
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
        val username by viewModel.username.collectAsStateWithLifecycle()
        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
        val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
        val customRingtoneUri by viewModel.customRingtoneUri.collectAsStateWithLifecycle()
        val lastContent by viewModel.lastContent.collectAsStateWithLifecycle()
        val subjects by viewModel.subjects.collectAsStateWithLifecycle()
        val schedules by viewModel.schedules.collectAsStateWithLifecycle()
        val upcomingSchedule by viewModel.upcomingSchedule.collectAsStateWithLifecycle()
        val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
        val recentQuizzes by viewModel.recentQuizzes.collectAsStateWithLifecycle()
        val averageScore by viewModel.averageScore.collectAsStateWithLifecycle()
        val averageProductivity by viewModel.averageProductivity.collectAsStateWithLifecycle()
        val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsStateWithLifecycle()
        val notificationLogs by viewModel.notificationLogs.collectAsStateWithLifecycle()

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        var showDirectInAppProductivity by remember { mutableStateOf(false) }

        // Handle notification deep links
        LaunchedEffect(intentTargetId, intentIsEvaluation) {
            if (intentIsEvaluation) {
                val subj = subjects.find { it.id == intentSubjectId } ?: subjects.firstOrNull()
                if (subj != null) {
                    currentScreen = Screen.Quiz(
                        subjectId = subj.id,
                        subjectName = subj.title,
                        chapterTitle = intentTopicTitle ?: subj.title,
                        pdfTitle = subj.title,
                        productivityRating = 5
                    )
                }
            } else if (!intentTargetId.isNullOrEmpty()) {
                currentScreen = Screen.ContentPlayer(
                    id = intentTargetId,
                    isVideo = intentIsVideo,
                    title = intentTopicTitle ?: "المحتوى"
                )
            }
        }

        if (!isLoggedIn) {
            AuthScreen(
                onLoginSuccess = { user ->
                    viewModel.login(user)
                }
            )
        } else {
            val isMainTab = currentScreen in listOf(
                Screen.Home,
                Screen.Subjects,
                Screen.Schedule,
                Screen.Evaluations,
                Screen.Settings
            )

            Scaffold(
                bottomBar = {
                    if (isMainTab) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            val items = listOf(
                                Triple(Screen.Home, "الرئيسية", Icons.Default.Home to Icons.Outlined.Home),
                                Triple(Screen.Subjects, "المواد", Icons.Default.MenuBook to Icons.Outlined.MenuBook),
                                Triple(Screen.Schedule, "الجدول", Icons.Default.CalendarMonth to Icons.Outlined.CalendarMonth),
                                Triple(Screen.Evaluations, "التقييمات", Icons.Default.BarChart to Icons.Outlined.BarChart),
                                Triple(Screen.Settings, "الإعدادات", Icons.Default.Settings to Icons.Outlined.Settings)
                            )

                            items.forEach { (screen, label, icons) ->
                                val selected = currentScreen == screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) icons.first else icons.second,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = VioletPrimary,
                                        selectedTextColor = VioletPrimary,
                                        indicatorColor = VioletPrimary.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                username = username,
                                upcomingSchedule = upcomingSchedule,
                                lastContent = lastContent,
                                recentQuizzes = recentQuizzes,
                                onOpenSchedule = { currentScreen = Screen.Schedule },
                                onOpenContent = { id, isVid ->
                                    currentScreen = Screen.ContentPlayer(
                                        id = id,
                                        isVideo = isVid,
                                        title = lastContent?.second ?: "المحاضرة"
                                    )
                                },
                                onOpenSubjects = { currentScreen = Screen.Subjects },
                                onOpenEvaluations = { currentScreen = Screen.Evaluations }
                            )
                        }

                        is Screen.Subjects -> {
                            SubjectsScreen(
                                subjects = subjects,
                                onSubjectClick = { subjId ->
                                    currentScreen = Screen.SubjectDetail(subjId)
                                },
                                onAddSubject = { title, sem, icon ->
                                    viewModel.addSubject(title, sem, icon)
                                },
                                onEditSubject = { subj ->
                                    viewModel.editSubject(subj)
                                },
                                onDeleteSubject = { subj ->
                                    viewModel.deleteSubject(subj)
                                }
                            )
                        }

                        is Screen.SubjectDetail -> {
                            BackHandler { currentScreen = Screen.Subjects }
                            val currentSubj = subjects.find { it.id == screen.subjectId }
                            if (currentSubj != null) {
                                val videosFlow = remember(screen.subjectId) {
                                    application.repository.getVideosForSubject(screen.subjectId)
                                }
                                val pdfsFlow = remember(screen.subjectId) {
                                    application.repository.getPdfsForSubject(screen.subjectId)
                                }
                                val subVideos by videosFlow.collectAsStateWithLifecycle(emptyList())
                                val subPdfs by pdfsFlow.collectAsStateWithLifecycle(emptyList())

                                SubjectDetailScreen(
                                    subject = currentSubj,
                                    videos = subVideos,
                                    pdfs = subPdfs,
                                    onBack = { currentScreen = Screen.Subjects },
                                    onOpenContent = { id, isVid, title ->
                                        viewModel.setLastContent(id, title, isVid, currentSubj.title)
                                        val linked = if (isVid) {
                                            subVideos.find { it.id == id }?.linkedPdfId
                                        } else {
                                            subVideos.find { it.linkedPdfId == id }?.id
                                        }
                                        currentScreen = Screen.ContentPlayer(
                                            id = id,
                                            isVideo = isVid,
                                            title = title,
                                            linkedId = linked
                                        )
                                    },
                                    onToggleWatched = { video ->
                                        viewModel.toggleVideoWatched(video)
                                    },
                                    onUpdateLinkedPdf = { videoId, pdfId ->
                                        viewModel.updateVideoLinkedPdf(videoId, pdfId)
                                    },
                                    onAddVideo = { title, url, linkedId ->
                                        // Custom video added
                                    },
                                    onAddPdf = { title, url ->
                                        // Custom pdf added
                                    },
                                    onStartQuiz = { chTitle, pdfTitle ->
                                        currentScreen = Screen.Quiz(
                                            subjectId = currentSubj.id,
                                            subjectName = currentSubj.title,
                                            chapterTitle = chTitle,
                                            pdfTitle = pdfTitle,
                                            productivityRating = 5
                                        )
                                    }
                                )
                            }
                        }

                        is Screen.ContentPlayer -> {
                            BackHandler { currentScreen = Screen.Subjects }
                            ContentPlayerScreen(
                                contentId = screen.id,
                                title = screen.title,
                                isVideo = screen.isVideo,
                                linkedContentId = screen.linkedId,
                                linkedContentTitle = screen.linkedTitle,
                                isWatched = false,
                                onBack = { currentScreen = Screen.Subjects },
                                onSwitchToLinked = { nextId, nextIsVid ->
                                    currentScreen = Screen.ContentPlayer(
                                        id = nextId,
                                        isVideo = nextIsVid,
                                        title = "المحتوى المرتبط"
                                    )
                                },
                                onToggleWatched = {
                                    viewModel.toggleVideoWatchedById(screen.id)
                                },
                                onFinishChapter = { rating ->
                                    val subj = subjects.firstOrNull()
                                    currentScreen = Screen.Quiz(
                                        subjectId = subj?.id ?: "computing_fundamental",
                                        subjectName = subj?.title ?: "المادة",
                                        chapterTitle = screen.title,
                                        pdfTitle = screen.title,
                                        productivityRating = rating
                                    )
                                }
                            )
                        }

                        is Screen.Quiz -> {
                            BackHandler { currentScreen = Screen.Evaluations }
                            QuizScreen(
                                subjectId = screen.subjectId,
                                subjectName = screen.subjectName,
                                chapterTitle = screen.chapterTitle,
                                pdfTitle = screen.pdfTitle,
                                productivityRating = screen.productivityRating,
                                onBack = { currentScreen = Screen.Evaluations },
                                onSaveAttempt = { attempt ->
                                    viewModel.saveQuizAttempt(attempt)
                                }
                            )
                        }

                        is Screen.Schedule -> {
                            ScheduleScreen(
                                schedules = schedules,
                                subjects = subjects,
                                onAddSchedule = { sch -> viewModel.addSchedule(sch) },
                                onEditSchedule = { sch -> viewModel.editSchedule(sch) },
                                onDeleteSchedule = { sch -> viewModel.deleteSchedule(sch) },
                                onOpenContent = { targetId, isVid ->
                                    currentScreen = Screen.ContentPlayer(
                                        id = targetId,
                                        isVideo = isVid,
                                        title = "مذاكرة المحتوى"
                                    )
                                }
                            )
                        }

                        is Screen.Evaluations -> {
                            EvaluationsScreen(
                                quizAttempts = quizAttempts,
                                averageScore = averageScore,
                                averageProductivity = averageProductivity,
                                totalStudyMinutes = totalStudyMinutes
                            )
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                currentTheme = themeMode,
                                notificationsEnabled = notificationsEnabled,
                                customRingtoneUri = customRingtoneUri,
                                onThemeChange = { mode: String -> viewModel.setTheme(mode) },
                                onNotificationsToggle = { en: Boolean -> viewModel.setNotificationsEnabled(en) },
                                onRingtoneUriChange = { uri: String? -> viewModel.setCustomRingtone(uri) },
                                onOpenLogs = { currentScreen = Screen.NotificationLogs },
                                onBackupData = {
                                    // Backup action
                                },
                                onRestoreData = {
                                    // Restore action
                                }
                            )
                        }

                        is Screen.NotificationLogs -> {
                            BackHandler { currentScreen = Screen.Settings }
                            NotificationLogScreen(
                                logs = notificationLogs,
                                onBack = { currentScreen = Screen.Settings },
                                onClearLogs = { viewModel.clearNotificationLogs() }
                            )
                        }
                    }
                }
            }
        }
    }
}
