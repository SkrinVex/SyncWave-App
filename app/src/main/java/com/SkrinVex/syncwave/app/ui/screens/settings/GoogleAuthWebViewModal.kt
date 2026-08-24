package com.SkrinVex.syncwave.app.ui.screens.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioEmerald
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleAuthWebViewModal(
    onDismiss: () -> Unit,
    onCookiesSynced: () -> Unit
) {
    val container = SyncWaveApplication.instance.container
    val googleAuthManager = container.googleAuthManager
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf("https://accounts.google.com/ServiceLogin?service=youtube&continue=https%3A%2F%2Fmusic.youtube.com%2F") }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var hasDetectedCookies by remember { mutableStateOf(googleAuthManager.hasAuthCookies()) }
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }

    val userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioBg,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        dragHandle = null,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBg)
        ) {
            // Drag Handle Bar (Top subtle pull-down pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Zinc500.copy(alpha = 0.5f))
                )
            }

            // Compact Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Close + Title & URL
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Zinc300,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "SSL",
                                tint = StudioEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Вход в YouTube Music",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Zinc100
                            )
                        }
                        Text(
                            text = currentUrl.removePrefix("https://").take(38) + if (currentUrl.length > 38) "..." else "",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Reload + Sync Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = Zinc400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Compact Sync Pill Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (hasDetectedCookies) StudioEmerald else StudioAccent)
                            .clickable(enabled = !isSyncing) {
                                scope.launch {
                                    isSyncing = true
                                    syncErrorMessage = null
                                    when (val res = googleAuthManager.syncCookiesToServer()) {
                                        is Resource.Success -> {
                                            isSyncing = false
                                            onCookiesSynced()
                                            onDismiss()
                                        }
                                        is Resource.Error -> {
                                            syncErrorMessage = res.message
                                            isSyncing = false
                                        }
                                        is Resource.Loading -> {}
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Zinc100,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Zinc100,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isSyncing) "Синхронизация..." else "Синхронизировать",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                    }
                }
            }

            // Progress Bar
            if (isLoading && pageProgress < 1f) {
                LinearProgressIndicator(
                    progress = { pageProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = StudioAccent,
                    trackColor = StudioElevated
                )
            }

            // Status Alert Banner (if cookies detected)
            AnimatedVisibility(visible = hasDetectedCookies && syncErrorMessage == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StudioEmerald)
                    )
                    Text(
                        text = "Сессия YouTube обнаружена! Нажмите «Синхронизировать», чтобы применить куки.",
                        fontSize = 11.sp,
                        color = StudioEmerald,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (syncErrorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioRed.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = syncErrorMessage ?: "",
                        fontSize = 11.sp,
                        color = StudioRed
                    )
                }
            }

            // Main WebView (Full height taking ALL remaining space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(StudioBg)
                    .navigationBarsPadding()
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
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                this.userAgentString = userAgent
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    if (url != null) currentUrl = url
                                    if (googleAuthManager.hasAuthCookies()) {
                                        hasDetectedCookies = true
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    if (url != null) currentUrl = url
                                    val detected = googleAuthManager.hasAuthCookies()
                                    hasDetectedCookies = detected

                                    // Auto-sync if reached music.youtube.com logged in
                                    if (detected && url != null && url.contains("music.youtube.com")) {
                                        scope.launch {
                                            googleAuthManager.syncCookiesToServer()
                                            onCookiesSynced()
                                        }
                                    }
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress / 100f
                                }
                            }

                            loadUrl(currentUrl)
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}
