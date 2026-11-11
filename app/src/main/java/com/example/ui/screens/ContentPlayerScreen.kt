package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.ProductivityDialog
import com.example.ui.components.PurpleButton
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletPrimary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ContentPlayerScreen(
    contentId: String,
    title: String,
    isVideo: Boolean,
    linkedContentId: String?,
    linkedContentTitle: String?,
    isWatched: Boolean,
    onBack: () -> Unit,
    onSwitchToLinked: (linkedId: String, isVideo: Boolean) -> Unit,
    onToggleWatched: () -> Unit,
    onFinishChapter: (rating: Int) -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var showProductivityDialog by remember { mutableStateOf(false) }

    val previewUrl = "https://drive.google.com/file/d/$contentId/preview"
    val downloadUrl = "https://drive.google.com/uc?export=download&id=$contentId&confirm=t"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F081E)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B1131))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Zoom Out
                IconButton(onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f) }) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = Color.White)
                }

                // Zoom In
                IconButton(onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3.0f) }) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = Color.White)
                }

                // Rotate
                IconButton(onClick = { rotationAngle = (rotationAngle + 90f) % 360f }) {
                    Icon(Icons.Default.RotateRight, contentDescription = "تدوير", tint = Color.White)
                }

                // Direct Download Intent
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Download, contentDescription = "تحميل", tint = EmeraldGreen)
                }
            }

            // Secondary Action Strip (Linked counterpart & Finish Chapter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF261942))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Link button to counterpart
                if (!linkedContentId.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(VioletPrimary.copy(alpha = 0.25f))
                            .clickable {
                                onSwitchToLinked(linkedContentId, !isVideo)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVideo) Icons.Default.PictureAsPdf else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = VioletPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVideo) "فتح الـ PDF المرتبط" else "مشاهدة الفيديو المرتبط",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isWatched) EmeraldGreen else Color.White.copy(alpha = 0.15f))
                                .clickable { onToggleWatched() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isWatched) "تمت المشاهدة" else "تحديد كمشاهد",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Finish Chapter Button
                    PurpleButton(
                        onClick = { showProductivityDialog = true },
                        isGreen = true,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إنهاء الشابتر", fontSize = 12.sp)
                    }
                }
            }

            // Player Viewport Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                                allowFileAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }
                            }
                            webChromeClient = WebChromeClient()
                            loadUrl(previewUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(zoomScale)
                        .rotate(rotationAngle)
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = VioletPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }

    if (showProductivityDialog) {
        ProductivityDialog(
            onDismiss = { showProductivityDialog = false },
            onConfirm = { rating ->
                showProductivityDialog = false
                onFinishChapter(rating)
            }
        )
    }
}
