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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.SubjectEntity
import com.example.ui.components.PurpleButton
import com.example.ui.components.PurpleCard
import com.example.ui.components.PurpleOutlinedButton
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletPrimary

@Composable
fun SubjectsScreen(
    subjects: List<SubjectEntity>,
    onSubjectClick: (subjectId: String) -> Unit,
    onAddSubject: (title: String, semester: String, icon: String) -> Unit,
    onEditSubject: (subject: SubjectEntity) -> Unit,
    onDeleteSubject: (subject: SubjectEntity) -> Unit
) {
    var selectedSemester by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }

    val filteredSubjects = subjects.filter { subj ->
        (selectedSemester == "all" || subj.semester == selectedSemester) &&
                (searchQuery.isEmpty() || subj.title.contains(searchQuery, ignoreCase = true))
    }

    PurpleWaveBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المواد الدراسية",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    PurpleButton(
                        onClick = { showAddDialog = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مادة")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في المواد...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VioletPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Semester Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        "all" to "الكل",
                        "sem1" to "الترم الأول",
                        "sem2" to "الترم الثاني",
                        "tests" to "الامتحانات"
                    )
                    items(filters) { (key, label) ->
                        val isSelected = selectedSemester == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) VioletPrimary else MaterialTheme.colorScheme.surface)
                                .clickable { selectedSemester = key }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subjects List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSubjects, key = { it.id }) { subject ->
                        PurpleCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSubjectClick(subject.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(VioletPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = subject.icon, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = subject.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val semText = when (subject.semester) {
                                            "sem1" -> "الترم الأول"
                                            "sem2" -> "الترم الثاني"
                                            "tests" -> "الامتحانات"
                                            else -> "عام"
                                        }
                                        Text(
                                            text = semText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { editingSubject = subject }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = VioletPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDeleteSubject(subject) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
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
    }

    // Add Subject Dialog
    if (showAddDialog) {
        SubjectDialog(
            subject = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, sem, icon ->
                onAddSubject(title, sem, icon)
                showAddDialog = false
            }
        )
    }

    // Edit Subject Dialog
    editingSubject?.let { subj ->
        SubjectDialog(
            subject = subj,
            onDismiss = { editingSubject = null },
            onSave = { title, sem, icon ->
                onEditSubject(subj.copy(title = title, semester = sem, icon = icon))
                editingSubject = null
            }
        )
    }
}

@Composable
fun SubjectDialog(
    subject: SubjectEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, semester: String, icon: String) -> Unit
) {
    var title by remember { mutableStateOf(subject?.title ?: "") }
    var semester by remember { mutableStateOf(subject?.semester ?: "sem1") }
    var icon by remember { mutableStateOf(subject?.icon ?: "📚") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (subject == null) "إضافة مادة جديدة" else "تعديل المادة",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم المادة") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("الأيقونة (إيموجي)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("الترم:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("sem1" to "الأول", "sem2" to "الثاني", "tests" to "امتحانات").forEach { (code, lbl) ->
                        val isSel = semester == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { semester = code }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = lbl,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PurpleButton(
                onClick = {
                    if (title.isNotBlank()) onSave(title.trim(), semester, icon.trim().ifEmpty { "📚" })
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            PurpleOutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
