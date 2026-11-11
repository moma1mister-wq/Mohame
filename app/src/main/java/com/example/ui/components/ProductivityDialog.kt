package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning

@Composable
fun ProductivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Int) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "ما قيمة إنتاجيتك؟",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (star in 1..5) {
                        val isSelected = star <= selectedStars
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "نجمة $star",
                            tint = if (isSelected) AmberWarning else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedStars = star }
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$selectedStars / 5",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning
                )
            }
        },
        confirmButton = {
            PurpleButton(
                onClick = { onConfirm(selectedStars) },
                isGreen = true
            ) {
                Text("بدء الكويز")
            }
        },
        dismissButton = {
            PurpleOutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
