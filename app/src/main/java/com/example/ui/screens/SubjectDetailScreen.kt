package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.PdfEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.ProductivityDialog
import com.example.ui.components.PurpleButton
import com.example.ui.components.PurpleCard
import com.example.ui.components.PurpleOutlinedButton
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subject: SubjectEntity,
    videos: List<VideoEntity>,
    pdfs: List<PdfEntity>,
    onBack: () -> Unit,
    onOpenContent: (id: String, isVideo: Boolean, title: String) -> Unit,
    onToggleWatched: (video: VideoEntity) -> Unit,
    onUpdateLinkedPdf: (videoId: String, pdfId: String?) -> Unit,
    onAddVideo: (title: String, url: String, linkedPdfId: String?) -> Unit,
    onAddPdf: (title: String, url: String) -> Unit,
    onStartQuiz: (chapterTitle: String, pdfTitle: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Videos, 1: PDFs
    var showProductivityForQuiz by remember { mutableStateOf(false) }
    var linkingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val watchedCount = videos.count { it.isWatched }
    val progress = if (videos.isNotEmpty()) watchedCount.toFloat() / videos.size.toFloat() else 0f

    PurpleWaveBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${subject.icon} ${subject.title}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                PurpleButton(
                    onClick = { showProductivityForQuiz = true },
                    isGreen = true,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الكويز")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Card
            PurpleCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تقدّم المادة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% ($watchedCount / ${videos.size} فيديوهات)",
                            fontSize = 13.sp,
                            color = VioletPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs: الفيديوهات | ملفات PDF
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = VioletPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "الفيديوهات (${videos.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "ملفات PDF (${pdfs.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content List
            if (selectedTab == 0) {
                // Videos
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(videos, key = { _, v -> v.id }) { index, video ->
                        val linkedPdf = pdfs.find { it.id == video.linkedPdfId }

                        PurpleCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (video.isWatched) EmeraldGreen.copy(alpha = 0.15f)
                                                    else VioletPrimary.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (video.isWatched) EmeraldGreen else VioletPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = video.title,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Linked PDF indication
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { linkingVideo = video }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = VioletPrimary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = linkedPdf?.title?.let { "مرتبط بـ: $it" } ?: "ربط بملف PDF",
                                                    fontSize = 12.sp,
                                                    color = VioletPrimary,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    // Watched checkbox toggle
                                    IconButton(onClick = { onToggleWatched(video) }) {
                                        Icon(
                                            imageVector = if (video.isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                            contentDescription = "تمت المشاهدة",
                                            tint = if (video.isWatched) EmeraldGreen else Color.Gray.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PurpleButton(
                                        onClick = { onOpenContent(video.id, true, video.title) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("مشاهدة")
                                    }

                                    if (linkedPdf != null) {
                                        PurpleOutlinedButton(
                                            onClick = { onOpenContent(linkedPdf.id, false, linkedPdf.title) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PDF المرتبط")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // PDFs
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(pdfs, key = { _, p -> p.id }) { index, pdf ->
                        PurpleCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pdf.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (pdf.lastPage > 1) {
                                            Text(
                                                text = "آخر صفحة: ${pdf.lastPage}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PurpleButton(
                                        onClick = { onOpenContent(pdf.id, false, pdf.title) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("عرض الملف")
                                    }

                                    PurpleButton(
                                        onClick = { onStartQuiz(pdf.title, pdf.title) },
                                        isGreen = true,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("كويز الملف")
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
    }

    // Productivity Dialog before Quiz
    if (showProductivityForQuiz) {
        ProductivityDialog(
            onDismiss = { showProductivityForQuiz = false },
            onConfirm = { rating ->
                showProductivityForQuiz = false
                val topic = videos.firstOrNull()?.title ?: subject.title
                onStartQuiz(topic, pdfs.firstOrNull()?.title ?: subject.title)
            }
        )
    }

    // Manual PDF Link Dialog
    linkingVideo?.let { v ->
        var expanded by remember { mutableStateOf(false) }
        var currentSelectedPdfId by remember { mutableStateOf(v.linkedPdfId) }

        AlertDialog(
            onDismissRequest = { linkingVideo = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("ربط الفيديو بملف PDF", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = v.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        val currentPdf = pdfs.find { it.id == currentSelectedPdfId }
                        OutlinedTextField(
                            value = currentPdf?.title ?: "بدون ربط",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("اختر ملف PDF") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("بدون ربط") },
                                onClick = {
                                    currentSelectedPdfId = null
                                    expanded = false
                                }
                            )
                            pdfs.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.title) },
                                    onClick = {
                                        currentSelectedPdfId = p.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                PurpleButton(
                    onClick = {
                        onUpdateLinkedPdf(v.id, currentSelectedPdfId)
                        linkingVideo = null
                    }
                ) {
                    Text("حفظ الربط")
                }
            },
            dismissButton = {
                PurpleOutlinedButton(onClick = { linkingVideo = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
