package com.example.data.local

import android.content.Context
import com.example.data.local.seed.SeedSem1
import com.example.data.local.seed.SeedSem2Part1
import com.example.data.local.seed.SeedSem2Part2
import com.example.data.local.seed.SeedSubjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InitialDataLoader(private val db: AppDatabase) {

    suspend fun loadInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = db.subjectDao().getSubjectCount()
        if (count == 0) {
            // Seed subjects
            db.subjectDao().insertSubjects(SeedSubjects.subjects)

            // Seed videos
            db.videoDao().insertVideos(SeedSem1.videos)
            db.videoDao().insertVideos(SeedSem2Part1.videos)
            db.videoDao().insertVideos(SeedSem2Part2.videos)

            // Seed PDFs
            db.pdfDao().insertPdfs(SeedSem1.pdfs)
            db.pdfDao().insertPdfs(SeedSem2Part1.pdfs)
            db.pdfDao().insertPdfs(SeedSem2Part2.pdfs)
        }
    }
}
