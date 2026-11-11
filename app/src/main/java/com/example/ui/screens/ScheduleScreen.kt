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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.ScheduleEntity
import com.example.data.model.SubjectEntity
import com.example.ui.components.PurpleButton
import com.example.ui.components.PurpleCard
import com.example.ui.components.PurpleOutlinedButton
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.components.TimeFormatHelper
import com.example.ui.components.TimePickerDialog12H
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    schedules: List<ScheduleEntity>,
    subjects: List<SubjectEntity>,
    onAddSchedule: (schedule: ScheduleEntity) -> Unit,
    onEditSchedule: (schedule: ScheduleEntity) -> Unit,
    onDeleteSchedule: (schedule: ScheduleEntity) -> Unit,
    onOpenContent: (targetId: String, isVideo: Boolean) -> Unit
) {
    var viewMode by remember { mutableIntStateOf(0) } // 0: Daily, 1: Weekly
    var selectedDayIndex by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }

    val daysOfWeek = listOf(
        Calendar.SUNDAY to "الأحد",
        Calendar.MONDAY to "الإثنين",
        Calendar.TUESDAY to "الثلاثاء",
        Calendar.WEDNESDAY to "الأربعاء",
        Calendar.THURSDAY to "الخميس",
        Calendar.FRIDAY to "الجمعة",
        Calendar.SATURDAY to "السبت"
    )

    val displayedSchedules = if (viewMode == 0) {
        // Daily: match selected day of week or daily repeating
        schedules.filter {
            it.repeatType == "daily" || it.dayOfWeek == selectedDayIndex
        }
    } else {
        // Weekly: all schedules sorted
        schedules
    }

    PurpleWaveBackground {
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
                    text = "جدول المذاكرة",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                PurpleButton(
                    onClick = { showAddDialog = true },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة موعد")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs: عرض يومي | عرض أسبوعي
            TabRow(
                selectedTabIndex = viewMode,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = VioletPrimary
            ) {
                Tab(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    text = { Text("عرض يومي", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    text = { Text("عرض أسبوعي", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day Selector (for Daily View)
            if (viewMode == 0) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysOfWeek) { (calDay, name) ->
                        val isSelected = calDay == selectedDayIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) VioletPrimary else MaterialTheme.colorScheme.surface)
                                .clickable { selectedDayIndex = calDay }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Schedules List
            if (displayedSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد مواعيد مذاكرة مسجلة",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedSchedules, key = { it.id }) { item ->
                        PurpleCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
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
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(VioletPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = VioletPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = item.subjectName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.topicTitle,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = TimeFormatHelper.format12H(item.hour, item.minute),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VioletPrimary
                                        )
                                        val repLabel = when (item.repeatType) {
                                            "daily" -> "يومي"
                                            "weekly" -> "أسبوعي"
                                            else -> "مرة واحدة"
                                        }
                                        Text(
                                            text = repLabel,
                                            fontSize = 12.sp,
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PurpleButton(
                                        onClick = {
                                            if (!item.targetId.isNullOrEmpty()) {
                                                onOpenContent(item.targetId, item.targetType == "video")
                                            }
                                        },
                                        isGreen = true,
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فتح المحتوى")
                                    }

                                    Row {
                                        IconButton(onClick = { editingSchedule = item }) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = VioletPrimary)
                                        }
                                        IconButton(onClick = { onDeleteSchedule(item) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
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
            }
        }
    }

    if (showAddDialog) {
        ScheduleDialog(
            schedule = null,
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onSave = { schedule ->
                onAddSchedule(schedule)
                showAddDialog = false
            }
        )
    }

    editingSchedule?.let { sch ->
        ScheduleDialog(
            schedule = sch,
            subjects = subjects,
            onDismiss = { editingSchedule = null },
            onSave = { updated ->
                onEditSchedule(updated)
                editingSchedule = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    schedule: ScheduleEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (ScheduleEntity) -> Unit
) {
    var selectedSubject by remember {
        mutableStateOf(subjects.find { it.id == schedule?.subjectId } ?: subjects.firstOrNull())
    }
    var topicTitle by remember { mutableStateOf(schedule?.topicTitle ?: "") }
    var selectedDay by remember { mutableIntStateOf(schedule?.dayOfWeek ?: Calendar.SUNDAY) }
    var hour by remember { mutableIntStateOf(schedule?.hour ?: 8) }
    var minute by remember { mutableIntStateOf(schedule?.minute ?: 0) }
    var repeatType by remember { mutableStateOf(schedule?.repeatType ?: "daily") }
    var showTimePicker by remember { mutableStateOf(false) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        Calendar.SUNDAY to "الأحد",
        Calendar.MONDAY to "الإثنين",
        Calendar.TUESDAY to "الثلاثاء",
        Calendar.WEDNESDAY to "الأربعاء",
        Calendar.THURSDAY to "الخميس",
        Calendar.FRIDAY to "الجمعة",
        Calendar.SATURDAY to "السبت"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (schedule == null) "إضافة موعد مذاكرة" else "تعديل الموعد",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = !subjectMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.title ?: "اختر المادة",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المادة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text("${subj.icon} ${subj.title}") },
                                onClick = {
                                    selectedSubject = subj
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Topic Title
                OutlinedTextField(
                    value = topicTitle,
                    onValueChange = { topicTitle = it },
                    label = { Text("الشابتر أو المحاضرة") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time 12H Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showTimePicker = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الوقت (12 ساعة)", fontWeight = FontWeight.Medium)
                    Text(
                        text = TimeFormatHelper.format12H(hour, minute),
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary,
                        fontSize = 16.sp
                    )
                }

                // Day Selection
                Text("اليوم:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(daysOfWeek) { (calDay, name) ->
                        val isSel = selectedDay == calDay
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedDay = calDay }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = name,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Repeat Type
                Text("التكرار:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("none" to "مرة واحدة", "daily" to "يومي", "weekly" to "أسبوعي").forEach { (code, label) ->
                        val isSel = repeatType == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { repeatType = code }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
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
            }
        },
        confirmButton = {
            PurpleButton(
                onClick = {
                    val subj = selectedSubject
                    if (subj != null) {
                        val newSchedule = ScheduleEntity(
                            id = schedule?.id ?: 0L,
                            subjectId = subj.id,
                            subjectName = subj.title,
                            topicTitle = topicTitle.trim().ifEmpty { "مذاكرة ${subj.title}" },
                            dayOfWeek = selectedDay,
                            hour = hour,
                            minute = minute,
                            repeatType = repeatType,
                            isActive = true
                        )
                        onSave(newSchedule)
                    }
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

    if (showTimePicker) {
        TimePickerDialog12H(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                hour = h
                minute = m
                showTimePicker = false
            }
        )
    }
}
