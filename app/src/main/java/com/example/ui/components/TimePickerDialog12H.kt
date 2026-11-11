package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VioletPrimary

object TimeFormatHelper {

    fun format12H(hour24: Int, minute: Int): String {
        val period = if (hour24 < 12) "ص" else "م"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minStr = minute.toString().padStart(2, '0')
        val hrStr = hour12.toString().padStart(2, '0')
        return "$hrStr:$minStr $period"
    }

    fun to24Hour(hour12: Int, isPm: Boolean): Int {
        return when {
            isPm && hour12 < 12 -> hour12 + 12
            !isPm && hour12 == 12 -> 0
            else -> hour12
        }
    }
}

@Composable
fun TimePickerDialog12H(
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (hour24: Int, minute: Int) -> Unit
) {
    val initialIsPm = initialHour >= 12
    val initialH12 = when {
        initialHour == 0 -> 12
        initialHour > 12 -> initialHour - 12
        else -> initialHour
    }

    var selectedHour by remember { mutableIntStateOf(initialH12) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    var isPm by remember { mutableStateOf(initialIsPm) }

    val hoursList = (1..12).toList()
    val minutesList = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "اختيار الوقت",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large preview of selected time in Arabic
                val hStr = selectedHour.toString().padStart(2, '0')
                val mStr = selectedMinute.toString().padStart(2, '0')
                val periodStr = if (isPm) "م" else "ص"

                Text(
                    text = "$hStr:$mStr $periodStr",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = VioletPrimary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // AM / PM Switcher
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PeriodButton(
                        text = "صباحاً (ص)",
                        isSelected = !isPm,
                        onClick = { isPm = false }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    PeriodButton(
                        text = "مساءً (م)",
                        isSelected = isPm,
                        onClick = { isPm = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("الساعة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(hoursList) { h ->
                        SelectablePill(
                            text = h.toString().padStart(2, '0'),
                            isSelected = h == selectedHour,
                            onClick = { selectedHour = h }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("الدقيقة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(minutesList) { m ->
                        SelectablePill(
                            text = m.toString().padStart(2, '0'),
                            isSelected = m == selectedMinute,
                            onClick = { selectedMinute = m }
                        )
                    }
                }
            }
        },
        confirmButton = {
            PurpleButton(
                onClick = {
                    val hour24 = TimeFormatHelper.to24Hour(selectedHour, isPm)
                    onConfirm(hour24, selectedMinute)
                }
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            PurpleOutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (isSelected) VioletPrimary else Color.Transparent,
                RoundedCornerShape(50)
            )
            .border(
                1.5.dp,
                if (isSelected) VioletPrimary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SelectablePill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (isSelected) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
