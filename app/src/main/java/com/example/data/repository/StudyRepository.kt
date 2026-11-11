package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.InitialDataLoader
import com.example.data.model.NotificationLogEntity
import com.example.data.model.PdfEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.ScheduleEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val db: AppDatabase) {

    private val loader = InitialDataLoader(db)

    suspend fun ensureDataLoaded() {
        loader.loadInitialDataIfNeeded()
    }

    // Subjects
    fun getAllSubjects(): Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()
    fun getSubjectsBySemester(sem: String): Flow<List<SubjectEntity>> = db.subjectDao().getSubjectsBySemester(sem)
    suspend fun getSubjectById(id: String): SubjectEntity? = db.subjectDao().getSubjectById(id)
    suspend fun insertSubject(subject: SubjectEntity) = db.subjectDao().insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = db.subjectDao().updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = db.subjectDao().deleteSubject(subject)

    // Videos
    fun getVideosForSubject(subjectId: String): Flow<List<VideoEntity>> = db.videoDao().getVideosForSubject(subjectId)
    suspend fun getVideoById(id: String): VideoEntity? = db.videoDao().getVideoById(id)
    suspend fun setVideoWatched(id: String, watched: Boolean) = db.videoDao().setWatched(id, watched)
    suspend fun setVideoLastPosition(id: String, seconds: Long) = db.videoDao().setLastPosition(id, seconds)
    suspend fun linkVideoPdf(videoId: String, pdfId: String?) = db.videoDao().linkPdf(videoId, pdfId)
    suspend fun insertVideo(video: VideoEntity) = db.videoDao().insertVideo(video)
    suspend fun updateVideo(video: VideoEntity) = db.videoDao().updateVideo(video)
    suspend fun deleteVideo(video: VideoEntity) = db.videoDao().deleteVideo(video)

    // PDFs
    fun getPdfsForSubject(subjectId: String): Flow<List<PdfEntity>> = db.pdfDao().getPdfsForSubject(subjectId)
    suspend fun getPdfById(id: String): PdfEntity? = db.pdfDao().getPdfById(id)
    suspend fun setPdfLastPage(id: String, page: Int) = db.pdfDao().setLastPage(id, page)
    suspend fun insertPdf(pdf: PdfEntity) = db.pdfDao().insertPdf(pdf)
    suspend fun updatePdf(pdf: PdfEntity) = db.pdfDao().updatePdf(pdf)
    suspend fun deletePdf(pdf: PdfEntity) = db.pdfDao().deletePdf(pdf)

    // Schedules
    fun getAllSchedules(): Flow<List<ScheduleEntity>> = db.scheduleDao().getAllSchedules()
    fun getActiveSchedules(): Flow<List<ScheduleEntity>> = db.scheduleDao().getActiveSchedules()
    suspend fun getScheduleById(id: Long): ScheduleEntity? = db.scheduleDao().getScheduleById(id)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long = db.scheduleDao().insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = db.scheduleDao().updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = db.scheduleDao().deleteSchedule(schedule)

    // Quizzes & Ratings
    fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>> = db.quizDao().getAllQuizAttempts()
    fun getRecentQuizAttempts(): Flow<List<QuizAttemptEntity>> = db.quizDao().getRecentAttempts()
    fun getAverageScore(): Flow<Float?> = db.quizDao().getAverageScore()
    fun getAverageProductivity(): Flow<Float?> = db.quizDao().getAverageProductivity()
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity): Long = db.quizDao().insertQuizAttempt(attempt)

    // Study logs
    fun getAllStudyLogs(): Flow<List<StudyLogEntity>> = db.studyLogDao().getAllLogs()
    fun getTotalStudyMinutes(): Flow<Int?> = db.studyLogDao().getTotalStudyMinutes()
    suspend fun insertStudyLog(durationMinutes: Int, subjectId: String) =
        db.studyLogDao().insertLog(StudyLogEntity(subjectId = subjectId, durationMinutes = durationMinutes))

    // Notification logs
    fun getAllNotificationLogs(): Flow<List<NotificationLogEntity>> = db.notificationLogDao().getAllLogs()
    suspend fun addNotificationLog(title: String, type: String) =
        db.notificationLogDao().insertLog(NotificationLogEntity(title = title, type = type))
    suspend fun clearNotificationLogs() = db.notificationLogDao().clearLogs()
}
