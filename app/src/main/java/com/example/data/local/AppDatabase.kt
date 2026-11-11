package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.NotificationLogEntity
import com.example.data.model.PdfEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.ScheduleEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.VideoEntity

@Database(
    entities = [
        SubjectEntity::class,
        VideoEntity::class,
        PdfEntity::class,
        ScheduleEntity::class,
        QuizAttemptEntity::class,
        StudyLogEntity::class,
        NotificationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun videoDao(): VideoDao
    abstract fun pdfDao(): PdfDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun quizDao(): QuizDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_organizer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
