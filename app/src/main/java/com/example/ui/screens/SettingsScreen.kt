package com.example.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.alarm.RingtonePlayerManager
import com.example.ui.components.PurpleButton
import com.example.ui.components.PurpleCard
import com.example.ui.components.PurpleOutlinedButton
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletPrimary

@Composable
fun SettingsScreen(
    currentTheme: String,
    notificationsEnabled: Boolean,
    customRingtoneUri: String?,
    onThemeChange: (String) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onRingtoneUriChange: (String?) -> Unit,
    onOpenLogs: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit
) {
    val context = LocalContext.current
    var isPlayingPreview by remember { mutableStateOf(false) }
    var testScheduledMessage by remember { mutableStateOf<String?>(null) }

    // Audio file picker launcher
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onRingtoneUriChange(uri.toString())
        }
    }

    // Permission launcher for POST_NOTIFICATIONS
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onNotificationsToggle(isGranted)
    }

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
                    text = "الإعدادات والصلاحيات",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Section 1: نغمة الإشعارات والتنبيهات
            item {
                Text(
                    text = "نغمة الإشعارات",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(VioletPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = VioletPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!customRingtoneUri.isNullOrEmpty()) "نغمة مخصصة مرفوعة" else "نغمة النظام الافتراضية",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!customRingtoneUri.isNullOrEmpty()) "ملف صوتي MP3 محدد" else "نغمة مؤقتة",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PurpleButton(
                                onClick = { audioPicker.launch("audio/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفع MP3")
                            }

                            PurpleButton(
                                onClick = {
                                    if (isPlayingPreview) {
                                        RingtonePlayerManager.stopAlarm()
                                        isPlayingPreview = false
                                    } else {
                                        isPlayingPreview = true
                                        RingtonePlayerManager.playPreview(context, customRingtoneUri) {
                                            isPlayingPreview = false
                                        }
                                    }
                                },
                                isGreen = true,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPlayingPreview) "إيقاف" else "تجربة")
                            }

                            if (!customRingtoneUri.isNullOrEmpty()) {
                                PurpleOutlinedButton(
                                    onClick = { onRingtoneUriChange(null) }
                                ) {
                                    Text("افتراضية")
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: الصلاحيات
            item {
                Text(
                    text = "الصلاحيات",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = VioletPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("الإشعارات والتنبيهات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { checked ->
                                    if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onNotificationsToggle(checked)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary, checkedTrackColor = VioletPrimary.copy(alpha = 0.3f))
                            )
                        }

                        // Exact Alarm
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = EmeraldGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("المنبهات الدقيقة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            PurpleButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                isGreen = true,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("تحقق")
                            }
                        }

                        // Battery optimization
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = AmberWarning)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("تجاهل تحسين البطارية", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            PurpleOutlinedButton(
                                onClick = {
                                    val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("تعديل")
                            }
                        }
                    }
                }
            }

            // Section 3: المظهر والثيم
            item {
                Text(
                    text = "المظهر",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("light" to "فاتح", "dark" to "داكن", "system" to "تلقائي").forEach { (themeCode, label) ->
                            val isSel = currentTheme == themeCode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSel) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onThemeChange(themeCode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: اختبار وتشخيص
            item {
                Text(
                    text = "اختبار الإشعارات والتشخيص",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PurpleButton(
                                onClick = {
                                    AlarmScheduler.scheduleTestAlarm(context, 10)
                                    testScheduledMessage = "تم ضبط إشعار تجريبي بعد 10 ثواني!"
                                },
                                isGreen = true,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("اختبار الإشعارات (10 ثواني)")
                            }

                            PurpleButton(
                                onClick = onOpenLogs,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سجل الإشعارات")
                            }
                        }

                        if (testScheduledMessage != null) {
                            Text(
                                text = testScheduledMessage ?: "",
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Section 5: النسخ الاحتياطي
            item {
                Text(
                    text = "النسخ الاحتياطي",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                PurpleCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PurpleButton(
                            onClick = onBackupData,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("نسخ احتياطي")
                        }

                        PurpleOutlinedButton(
                            onClick = onRestoreData,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("استيراد")
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
