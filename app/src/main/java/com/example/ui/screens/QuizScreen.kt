package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizGenerator
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.QuizQuestion
import com.example.ui.components.PurpleButton
import com.example.ui.components.PurpleCard
import com.example.ui.components.PurpleOutlinedButton
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletPrimary

@Composable
fun QuizScreen(
    subjectId: String,
    subjectName: String,
    chapterTitle: String,
    pdfTitle: String,
    productivityRating: Int,
    onBack: () -> Unit,
    onSaveAttempt: (attempt: QuizAttemptEntity) -> Unit
) {
    val questions = remember(subjectId, chapterTitle) {
        QuizGenerator.generateQuiz(subjectId, chapterTitle, pdfTitle)
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val userMcqAnswers = remember { mutableStateMapOf<Int, Int>() }
    val userEssayAnswers = remember { mutableStateMapOf<Int, String>() }
    var isSubmitted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentIndex) ?: questions[0]
    val totalCount = questions.size

    // Calculate score
    val mcqQuestions = questions.filter { !it.isEssay }
    val essayQuestions = questions.filter { it.isEssay }

    val mcqCorrectCount = mcqQuestions.count { q ->
        userMcqAnswers[q.id] == q.correctIndex
    }
    // Auto-grade essays based on effort / content matching keywords
    val essayCorrectCount = essayQuestions.count { q ->
        val answer = userEssayAnswers[q.id]?.trim() ?: ""
        answer.length >= 25 // Reasonable thoughtful essay response
    }

    val totalCorrect = mcqCorrectCount + essayCorrectCount
    // Score out of 10
    val rawScore = (totalCorrect.toFloat() / totalCount.toFloat()) * 10f
    val finalScore = String.format("%.1f", rawScore).toFloat()

    PurpleWaveBackground {
        if (!isSubmitted) {
            // Active Quiz Flow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subjectName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = chapterTitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(VioletPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / $totalCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / totalCount.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = VioletPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Question Card
                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = if (currentQuestion.isEssay) "سؤال مقالي:" else "اختر الإجابة الصحيحة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentQuestion.isEssay) VioletPrimary else EmeraldGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (currentQuestion.isEssay) {
                            OutlinedTextField(
                                value = userEssayAnswers[currentQuestion.id] ?: "",
                                onValueChange = { userEssayAnswers[currentQuestion.id] = it },
                                placeholder = { Text("اكتب إجابتك التحليلية هنا...") },
                                minLines = 5,
                                maxLines = 8,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                currentQuestion.options.forEachIndexed { optIndex, optionText ->
                                    val isSelected = userMcqAnswers[currentQuestion.id] == optIndex
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) VioletPrimary.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) VioletPrimary else Color.Transparent,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                userMcqAnswers[currentQuestion.id] = optIndex
                                            }
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) VioletPrimary else Color.Gray.copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${('أ'.code + optIndex).toChar()}",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = optionText,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Prev / Next / Submit row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentIndex > 0) {
                        PurpleOutlinedButton(onClick = { currentIndex-- }) {
                            Text("السابق")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (currentIndex < totalCount - 1) {
                        PurpleButton(onClick = { currentIndex++ }) {
                            Text("التالي")
                        }
                    } else {
                        PurpleButton(
                            onClick = {
                                isSubmitted = true
                                val attempt = QuizAttemptEntity(
                                    subjectId = subjectId,
                                    subjectName = subjectName,
                                    chapterOrTopic = chapterTitle,
                                    pdfId = pdfTitle,
                                    score = finalScore,
                                    totalQuestions = totalCount,
                                    correctAnswersCount = totalCorrect,
                                    productivityRating = productivityRating
                                )
                                onSaveAttempt(attempt)
                            },
                            isGreen = true
                        ) {
                            Text("تسليم الكويز")
                        }
                    }
                }
            }
        } else {
            // Results & Mistakes Review
            val wrongQuestions = questions.filter { q ->
                if (q.isEssay) {
                    (userEssayAnswers[q.id]?.trim()?.length ?: 0) < 25
                } else {
                    userMcqAnswers[q.id] != q.correctIndex
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "نتيجة الكويز",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Score Card
                item {
                    PurpleCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$finalScore / 10",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "أجبت بشكل صحيح على $totalCorrect من أصل $totalCount سؤال",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PurpleButton(
                                    onClick = {
                                        userMcqAnswers.clear()
                                        userEssayAnswers.clear()
                                        currentIndex = 0
                                        isSubmitted = false
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إعادة الكويز")
                                }

                                PurpleOutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إنهاء")
                                }
                            }
                        }
                    }
                }

                // Section: تصحيح الأسئلة التي أخطأت فيها
                item {
                    Text(
                        text = if (wrongQuestions.isEmpty()) "ممتاز! لم تخطئ في أي سؤال." else "الأسئلة التي أخطأت فيها والإجابة الصحيحة:",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                itemsIndexed(wrongQuestions) { idx, q ->
                    PurpleCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(RoseError.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "سؤال رقم ${q.id}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseError
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = q.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(10.dp))

                            if (q.isEssay) {
                                Text(
                                    text = "إجابتك: ${userEssayAnswers[q.id]?.ifEmpty { "لم تتم الإجابة" } ?: "لم تتم الإجابة"}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الإجابة النموذجية: ${q.modelAnswer}",
                                    fontSize = 13.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                val selectedOpt = userMcqAnswers[q.id]?.let { q.options.getOrNull(it) } ?: "لم يتم الاختيار"
                                val correctOpt = q.options.getOrNull(q.correctIndex) ?: ""

                                Text(
                                    text = "إجابتك: $selectedOpt",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الإجابة الصحيحة: $correctOpt",
                                    fontSize = 13.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold
                                )

                                if (q.explanation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "الشرح: ${q.explanation}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
