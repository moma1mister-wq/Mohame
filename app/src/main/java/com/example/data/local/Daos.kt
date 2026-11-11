package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NotificationLogEntity
import com.example.data.model.PdfEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.ScheduleEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE semester = :semester ORDER BY orderIndex ASC")
    fun getSubjectsBySemester(semester: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getVideosForSubject(subjectId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE isWatched = 1")
    fun getWatchedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY lastPositionSeconds DESC LIMIT 1")
    fun getLastWatchedVideo(): Flow<VideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET isWatched = :watched WHERE id = :id")
    suspend fun setWatched(id: String, watched: Boolean)

    @Query("UPDATE videos SET lastPositionSeconds = :seconds WHERE id = :id")
    suspend fun setLastPosition(id: String, seconds: Long)

    @Query("UPDATE videos SET linkedPdfId = :pdfId WHERE id = :videoId")
    suspend fun linkPdf(videoId: String, pdfId: String?)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)
}

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdfs WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getPdfsForSubject(subjectId: String): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdfs WHERE id = :id LIMIT 1")
    suspend fun getPdfById(id: String): PdfEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfs(pdfs: List<PdfEntity>)

    @Update
    suspend fun updatePdf(pdf: PdfEntity)

    @Query("UPDATE pdfs SET lastPage = :page WHERE id = :id")
    suspend fun setLastPage(id: String, page: Int)

    @Delete
    suspend fun deletePdf(pdf: PdfEntity)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY hour ASC, minute ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE isActive = 1 ORDER BY hour ASC, minute ASC")
    fun getActiveSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getQuizAttemptsForSubject(subjectId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC LIMIT 5")
    fun getRecentAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT AVG(score) FROM quiz_attempts")
    fun getAverageScore(): Flow<Float?>

    @Query("SELECT AVG(productivityRating) FROM quiz_attempts")
    fun getAverageProductivity(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity): Long
}

@Dao
interface StudyLogDao {
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<StudyLogEntity>>

    @Query("SELECT SUM(durationMinutes) FROM study_logs")
    fun getTotalStudyMinutes(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudyLogEntity)
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity)

    @Query("DELETE FROM notification_logs")
    suspend fun clearLogs()
}
