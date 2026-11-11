package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    val activity = context as? Activity

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }
    var showProductivityDialog by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val previewUrl = "https://drive.google.com/file/d/$contentId/preview"
    val directViewUrl = "https://drive.google.com/file/d/$contentId/view"
    val downloadUrl = "https://drive.google.com/uc?export=download&id=$contentId&confirm=t"

    // Cleanup WebView and restore portrait orientation when leaving screen
    DisposableEffect(contentId) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            webViewInstance?.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
            webViewInstance = null
        }
    }

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
                IconButton(onClick = {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    onBack()
                }) {
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

                // Native WebView Zoom Out
                IconButton(onClick = {
                    webViewInstance?.zoomOut()
                }) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = Color.White)
                }

                // Native WebView Zoom In
                IconButton(onClick = {
                    webViewInstance?.zoomIn()
                }) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = Color.White)
                }

                // Native Screen Rotation Toggle (Landscape / Portrait)
                IconButton(onClick = {
                    isLandscape = !isLandscape
                    activity?.requestedOrientation = if (isLandscape) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "تدوير الشاشة",
                        tint = if (isLandscape) EmeraldGreen else Color.White
                    )
                }

                // Open in External Browser / App
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(directViewUrl))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "فتح خارجي", tint = Color.White)
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
                            // Enable hardware acceleration with fallback safety
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
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

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    // If main frame fails, show retry option
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        hasError = true
                                    }
                                }
                            }
                            webChromeClient = WebChromeClient()
                            loadUrl(previewUrl)
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = VioletPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                if (hasError) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F081E).copy(alpha = 0.95f))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "تعذر تحميل المعاينة المباشرة",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PurpleButton(onClick = {
                                hasError = false
                                isLoading = true
                                webViewInstance?.reload()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة المحاولة")
                            }

                            PurpleButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(directViewUrl))
                                    context.startActivity(intent)
                                },
                                isGreen = true
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فتح في المتصفح")
                            }
                        }
                    }
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
