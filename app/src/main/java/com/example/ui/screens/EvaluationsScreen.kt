package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizAttemptEntity
import com.example.ui.components.PurpleCard
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvaluationsScreen(
    quizAttempts: List<QuizAttemptEntity>,
    averageScore: Float?,
    averageProductivity: Float?,
    totalStudyMinutes: Int?
) {
    var chartFilterIndex by remember { mutableIntStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly

    val avgScoreText = String.format(Locale.US, "%.1f", averageScore ?: 0f)
    val avgProdText = String.format(Locale.US, "%.1f", averageProductivity ?: 5f)
    val totalHours = String.format(Locale.US, "%.1f", (totalStudyMinutes ?: 0) / 60f)

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.forLanguageTag("ar")) }

    PurpleWaveBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "التقييمات والإحصائيات",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // KPI Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avg Score
                    PurpleCard(modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("متوسط الدرجات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$avgScoreText / 10",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    // Productivity
                    PurpleCard(modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("درجة الإنتاجية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$avgProdText / 5",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                            }
                        }
                    }
                }
            }

            // Chart Section
            item {
                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("معدل الإنجاز والمذاكرة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = VioletPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Daily / Weekly / Monthly Switcher
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("يومي", "أسبوعي", "شهري").forEachIndexed { idx, label ->
                                val isSel = chartFilterIndex == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSel) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { chartFilterIndex = idx }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Native Bar Chart Canvas
                        val barData = when (chartFilterIndex) {
                            0 -> listOf(0.6f, 0.85f, 0.7f, 0.9f, 0.75f, 0.95f, 0.8f) // 7 days
                            1 -> listOf(0.7f, 0.82f, 0.88f, 0.92f) // 4 weeks
                            else -> listOf(0.65f, 0.75f, 0.8f, 0.85f, 0.9f, 0.94f) // 6 months
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val barCount = barData.size
                            val spacing = size.width / (barCount * 2)
                            val barWidth = spacing * 0.9f

                            barData.forEachIndexed { index, fraction ->
                                val barHeight = size.height * fraction
                                val x = spacing + (index * (barWidth + spacing))
                                val y = size.height - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(VioletPrimary, EmeraldGreen)
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                        }
                    }
                }
            }

            // Quiz Attempts History List
            item {
                Text(
                    text = "سجل الكويزات والدرجات (${quizAttempts.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (quizAttempts.isEmpty()) {
                item {
                    PurpleCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "لا توجد نتائج مسجلة حتى الآن",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(quizAttempts, key = { it.id }) { attempt ->
                    PurpleCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = attempt.subjectName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = attempt.chapterOrTopic,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${attempt.productivityRating}/5",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = dateFormat.format(Date(attempt.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${attempt.score} / 10",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
