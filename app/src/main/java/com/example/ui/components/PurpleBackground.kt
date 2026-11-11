package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.Purple800
import com.example.ui.theme.Purple900
import com.example.ui.theme.VioletDark
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletPrimary

@Composable
fun PurpleWaveBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (isDark) {
                // Top subtle purple wave
                val topPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(width, 0f)
                    lineTo(width, height * 0.28f)
                    cubicTo(
                        width * 0.75f, height * 0.35f,
                        width * 0.25f, height * 0.20f,
                        0f, height * 0.28f
                    )
                    close()
                }
                drawPath(
                    path = topPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Purple900.copy(alpha = 0.7f), Purple800.copy(alpha = 0.3f))
                    )
                )

                // Bottom soft wave
                val bottomPath = Path().apply {
                    moveTo(0f, height)
                    lineTo(width, height)
                    lineTo(width, height * 0.82f)
                    cubicTo(
                        width * 0.7f, height * 0.75f,
                        width * 0.3f, height * 0.88f,
                        0f, height * 0.80f
                    )
                    close()
                }
                drawPath(
                    path = bottomPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(VioletDark.copy(alpha = 0.15f), Purple900.copy(alpha = 0.4f))
                    )
                )
            } else {
                // Light mode: Vibrant purple top header wave with gradient
                val topWave = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(width, 0f)
                    lineTo(width, height * 0.24f)
                    cubicTo(
                        width * 0.7f, height * 0.29f,
                        width * 0.3f, height * 0.18f,
                        0f, height * 0.25f
                    )
                    close()
                }
                drawPath(
                    path = topWave,
                    brush = Brush.verticalGradient(
                        colors = listOf(VioletPrimary.copy(alpha = 0.18f), VioletLight.copy(alpha = 0.05f))
                    )
                )

                val bottomWave = Path().apply {
                    moveTo(0f, height)
                    lineTo(width, height)
                    lineTo(width, height * 0.88f)
                    cubicTo(
                        width * 0.65f, height * 0.82f,
                        width * 0.35f, height * 0.94f,
                        0f, height * 0.88f
                    )
                    close()
                }
                drawPath(
                    path = bottomWave,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEDE7F6).copy(alpha = 0.3f), VioletPrimary.copy(alpha = 0.12f))
                    )
                )
            }
        }
        content()
    }
}
