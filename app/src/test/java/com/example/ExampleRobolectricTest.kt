package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuizGenerator
import com.example.ui.components.TimeFormatHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مذاكرتي", appName)
    }

    @Test
    fun test12HourTimeFormatting() {
        assertEquals("12:00 ص", TimeFormatHelper.format12H(0, 0))
        assertEquals("12:00 م", TimeFormatHelper.format12H(12, 0))
        assertEquals("08:30 ص", TimeFormatHelper.format12H(8, 30))
        assertEquals("03:45 م", TimeFormatHelper.format12H(15, 45))
        assertEquals("11:59 م", TimeFormatHelper.format12H(23, 59))

        assertEquals(0, TimeFormatHelper.to24Hour(12, false))
        assertEquals(12, TimeFormatHelper.to24Hour(12, true))
        assertEquals(8, TimeFormatHelper.to24Hour(8, false))
        assertEquals(20, TimeFormatHelper.to24Hour(8, true))
    }

    @Test
    fun testQuizGeneration() {
        val questions = QuizGenerator.generateQuiz("discrete_mathematics", "Ch01", "Logic")
        assertEquals(22, questions.size)
        val mcqs = questions.filter { !it.isEssay }
        val essays = questions.filter { it.isEssay }
        assertEquals(20, mcqs.size)
        assertEquals(2, essays.size)
        assertTrue(mcqs.all { it.options.size == 4 })
    }
}
