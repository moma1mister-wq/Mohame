package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val semester: String, // "sem1", "sem2", "tests"
    val icon: String,
    val colorHex: String = "#7C3AED",
    val customRingtoneUri: String? = null,
    val orderIndex: Int = 0
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val driveUrl: String,
    val linkedPdfId: String? = null,
    val isWatched: Boolean = false,
    val lastPositionSeconds: Long = 0L,
    val orderIndex: Int = 0
)

@Entity(tableName = "pdfs")
data class PdfEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val driveUrl: String,
    val lastPage: Int = 1,
    val orderIndex: Int = 0
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val subjectName: String,
    val topicTitle: String,
    val targetType: String = "general", // "video", "pdf", "general"
    val targetId: String? = null,
    val dayOfWeek: Int = 1, // 1: Sunday, ..., 7: Saturday
    val dateEpochDay: Long = 0L, // For single date
    val hour: Int = 8, // 0 - 23
    val minute: Int = 0, // 0 - 59
    val durationMinutes: Int = 60,
    val repeatType: String = "none", // "none", "daily", "weekly"
    val isActive: Boolean = true,
    val customRingtoneUri: String? = null
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val subjectName: String,
    val chapterOrTopic: String,
    val pdfId: String? = null,
    val score: Float, // Score out of 10.0
    val totalQuestions: Int = 22,
    val correctAnswersCount: Int = 0,
    val productivityRating: Int = 5, // 1 to 5 stars
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val durationMinutes: Int = 30,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "مذاكرة", "تقييم", "تجربة"
    val timestamp: Long = System.currentTimeMillis(),
    val handled: Boolean = true
)
