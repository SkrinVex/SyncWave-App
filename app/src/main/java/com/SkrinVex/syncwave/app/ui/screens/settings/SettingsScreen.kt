package com.SkrinVex.syncwave.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.ui.components.StudioBadge
import com.SkrinVex.syncwave.app.ui.components.StudioButton
import com.SkrinVex.syncwave.app.ui.components.StudioCard
import com.SkrinVex.syncwave.app.ui.components.StudioTextField
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioEmerald
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isOffline: Boolean = false,
    onNavigateToAuth: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteCookiesDialog by remember { mutableStateOf(false) }

    val cookiesFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadCookiesFile(context, uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Offline Warning Banner
        if (isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioEmerald.copy(alpha = 0.12f))
                    .border(1.dp, StudioEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = StudioEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Оффлайн-режим активен",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Zinc100
                        )
                        Text(
                            text = "Серверные настройки отключены для защиты от рассинхронизации. Управление оффлайн-треками и аудиофокусом доступно.",
                            fontSize = 11.sp,
                            color = Zinc400,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Header
        Column {
            Text(
                text = "Настройки",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Zinc100
            )
            Text(
                text = if (isOffline) "Локальные параметры плеера и оффлайн-хранилища" else "Параметры системы, YouTube Cookies, хранилище и плеер",
                fontSize = 12.sp,
                color = Zinc400,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Section 1: Library & Storage Statistics Card
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = StudioAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Статистика медиатеки",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100
                    )
                }

                // 4-Card Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Треков в библиотеке",
                        value = "${uiState.stats.totalTracks}",
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Длительность",
                        value = uiState.stats.formattedTotalDuration,
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Объем аудиофайлов",
                        value = uiState.settings.formattedUserStorage,
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "База SQLite (WAL)",
                        value = uiState.settings.formattedDbSize,
                        icon = Icons.Default.Tune,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Storage Quota Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Дисковая квота пользователя", fontSize = 11.sp, color = Zinc400)
                        Text(
                            text = if (uiState.settings.userStorageQuotaBytes > 0) {
                                "${uiState.settings.formattedUserStorage} / ${uiState.settings.formattedUserQuota} (${uiState.settings.quotaUsagePercent}%)"
                            } else {
                                "${uiState.settings.formattedUserStorage} (Безлимитно)"
                            },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc300
                        )
                    }
                    if (uiState.settings.userStorageQuotaBytes > 0) {
                        LinearProgressIndicator(
                            progress = { uiState.settings.quotaUsageFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = StudioAccent,
                            trackColor = StudioElevated
                        )
                    }
                }
            }
        }

        // Section 2: YouTube Music Cookies & Google Auth Sync Card
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = if (uiState.settings.isCookiesValid) StudioEmerald else StudioAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "YouTube Music Cookies",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                    }

                    // Status Badge
                    val (badgeText, badgeBg, badgeTextColor) = if (isOffline) {
                        Triple("ОФФЛАЙН", StudioElevated, Zinc400)
                    } else when (uiState.settings.cookiesStatus) {
                        "valid" -> Triple("АКТИВНЫ", StudioEmerald.copy(alpha = 0.15f), StudioEmerald)
                        "expiring_soon" -> Triple("ИСТЕКАЮТ", StudioAccent.copy(alpha = 0.2f), StudioAccent)
                        "expired" -> Triple("ИСТЕКЛИ", StudioRed.copy(alpha = 0.2f), StudioRed)
                        "invalid" -> Triple("НЕВАЛИДНЫ", StudioRed.copy(alpha = 0.2f), StudioRed)
                        else -> Triple("ОТСУТСТВУЮТ", StudioElevated, Zinc400)
                    }

                    StudioBadge(
                        text = badgeText,
                        backgroundColor = badgeBg,
                        textColor = badgeTextColor
                    )
                }

                // Description
                val desc = when {
                    isOffline -> "Управление cookies YouTube и авторизация Google недоступны в оффлайн-режиме."
                    uiState.settings.isCookiesValid -> "Куки авторизации активны. Синхронизация закрытых плейлистов и треков Liked Music работает штатно."
                    uiState.settings.isCookiesExpiringSoon -> "Срок действия cookies YouTube подходит к концу. Рекомендуется обновить их через WebView или загрузить свежий файл."
                    uiState.settings.isCookiesExpired -> "Сессия YouTube истекла. YouTube заблокировал доступ к трекам. Выполните вход через Google WebView для восстановления синхронизации."
                    else -> "Для синхронизации треков из закрытых плейлистов и Liked Music (LM) выполните вход в аккаунт Google или загрузите cookies.txt."
                }

                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = Zinc300,
                    lineHeight = 16.sp
                )

                // Detailed Expiration / Error Banner if any
                if (!isOffline && (uiState.settings.cookiesExpiresAt.isNotBlank() || uiState.settings.cookiesError.isNotBlank())) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (uiState.settings.cookiesExpiresAt.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Истекают:", fontSize = 11.sp, color = Zinc400)
                                Text(
                                    text = uiState.settings.cookiesExpiresAt.take(19).replace("T", " "),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Zinc300
                                )
                            }
                        }
                        if (uiState.settings.cookiesError.isNotBlank()) {
                            Text(
                                text = uiState.settings.cookiesError,
                                fontSize = 11.sp,
                                color = StudioRed
                            )
                        }
                    }
                }

                // Cookies Operation Feedback Message
                AnimatedVisibility(visible = !uiState.cookiesOperationMessage.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = uiState.cookiesOperationMessage ?: "",
                            fontSize = 11.sp,
                            color = Zinc300
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google WebView Login Button (Primary Action)
                    StudioButton(
                        text = "Войти через Google",
                        onClick = { viewModel.openGoogleAuthModal() },
                        backgroundColor = StudioAccent,
                        textColor = Zinc100,
                        icon = Icons.Default.Language,
                        enabled = !isOffline,
                        modifier = Modifier.weight(1f)
                    )

                    // Upload cookies.txt
                    StudioButton(
                        text = "Файл cookies.txt",
                        onClick = { cookiesFilePicker.launch("text/*") },
                        backgroundColor = StudioElevated,
                        textColor = Zinc300,
                        icon = Icons.Default.UploadFile,
                        isLoading = uiState.isUploadingCookies,
                        enabled = !isOffline,
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.settings.hasCookies) {
                        IconButton(
                            onClick = { showDeleteCookiesDialog = true },
                            enabled = !isOffline,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить cookies",
                                tint = if (isOffline) Zinc500 else StudioRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

            }
        }

        // Section 2: Audio Focus & Playback Settings
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = StudioAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Воспроизведение и Аудио",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Автопауза при аудиофокусе (Audio Focus)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Zinc100
                        )
                        Text(
                            text = "Приостанавливать музыку при голосовом вводе клавиатуры, микрофоне и звонках",
                            fontSize = 11.sp,
                            color = Zinc400,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Switch(
                        checked = uiState.isAudioFocusEnabled,
                        onCheckedChange = { viewModel.toggleAudioFocus(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Zinc100,
                            checkedTrackColor = StudioAccent,
                            uncheckedThumbColor = Zinc500,
                            uncheckedTrackColor = StudioElevated
                        )
                    )
                }
            }
        }

        // Section: Downloaded Tracks & Offline Storage Card
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownloadDone,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Скачанные треки и оффлайн",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                    }

                    StudioBadge(
                        text = "${uiState.downloadedTracks.size} ТРЕКОВ",
                        backgroundColor = StudioEmerald.copy(alpha = 0.15f),
                        textColor = StudioEmerald
                    )
                }

                // Switch 1: Auto-download all tracks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Автоматически скачивать все треки",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Zinc100
                        )
                        Text(
                            text = "Автоматически сохранять новые треки на устройство для прослушивания без интернета",
                            fontSize = 11.sp,
                            color = Zinc400,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Switch(
                        checked = uiState.isAutoDownloadEnabled,
                        onCheckedChange = { viewModel.toggleAutoDownload(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Zinc100,
                            checkedTrackColor = StudioAccent,
                            uncheckedThumbColor = Zinc500,
                            uncheckedTrackColor = StudioElevated
                        )
                    )
                }

                // Switch 2: Auto-delete orphaned downloads
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Автоматически удалять треки, если их нет на сервере",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Zinc100
                        )
                        Text(
                            text = "Удалять локальный файл с устройства, если трек был удален из библиотеки на сервере",
                            fontSize = 11.sp,
                            color = Zinc400,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Switch(
                        checked = uiState.isAutoDeleteOrphanedEnabled,
                        onCheckedChange = { viewModel.toggleAutoDeleteOrphaned(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Zinc100,
                            checkedTrackColor = StudioAccent,
                            uncheckedThumbColor = Zinc500,
                            uncheckedTrackColor = StudioElevated
                        )
                    )
                }

                // Manage Downloaded Tracks Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioElevated)
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.openDownloadsSheet() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Управление скачанными треками",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                        Text(
                            text = "Занято памяти: ${uiState.downloadedTotalStorageFormatted} (${uiState.downloadedTracks.size} треков)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StudioEmerald,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Открыть", color = StudioAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = StudioAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Section 3: Server Connection Card
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Подключение к серверу",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                    }

                    StudioBadge(
                        text = if (isOffline) "ОФФЛАЙН" else "ONLINE",
                        backgroundColor = if (isOffline) StudioElevated else StudioEmerald.copy(alpha = 0.15f),
                        textColor = if (isOffline) Zinc400 else StudioEmerald
                    )

                }

                // Server URL Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioElevated)
                        .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.serverUrl,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Zinc300,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.testConnection() },
                            enabled = !isOffline,
                            modifier = Modifier.size(28.dp)
                        ) {
                            if (uiState.isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = StudioAccent,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Проверить",
                                    tint = if (isOffline) Zinc500 else Zinc400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }


                        IconButton(
                            onClick = { viewModel.openEditServerUrlModal() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Изменить",
                                tint = StudioAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Connection Test Banner
                AnimatedVisibility(visible = !uiState.connectionTestResult.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = uiState.connectionTestResult ?: "",
                            fontSize = 11.sp,
                            color = Zinc300
                        )
                    }
                }
            }
        }

        // Section 4: System Information & Diagnostics
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = StudioAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Системные показатели",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100
                    )
                }

                SettingsMetricRow("Аудиокодек сервера", uiState.settings.audioFormat.uppercase())
                SettingsMetricRow("Статус Cookies YouTube", uiState.settings.cookiesStatus.uppercase())
                SettingsMetricRow("Версия yt-dlp", uiState.settings.ytdlpVersion.ifBlank { "Ready" })
                SettingsMetricRow("Версия FFmpeg", uiState.settings.ffmpegVersion.ifBlank { "Ready" })
                if (uiState.settings.hostDiskFreeBytes > 0) {
                    SettingsMetricRow("Свободно на сервере", uiState.settings.formattedHostDiskFree)
                }
            }
        }

        // Section 5: User Profile & Logout
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioAccent.copy(alpha = 0.15f))
                                .border(1.dp, StudioAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.user?.isAdmin == true) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = StudioAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = uiState.user?.username ?: "Пользователь",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Zinc100
                            )
                            Text(
                                text = if (uiState.user?.isAdmin == true) "Администратор SyncWave" else "Пользователь",
                                fontSize = 11.sp,
                                color = Zinc400
                            )
                        }
                    }

                    if (uiState.user?.isAdmin == true) {
                        StudioBadge(
                            text = "ADMIN",
                            backgroundColor = StudioAccent.copy(alpha = 0.2f),
                            textColor = StudioAccent
                        )
                    }
                }

                StudioButton(
                    text = "Выйти из учетной записи",
                    onClick = { showLogoutDialog = true },
                    backgroundColor = StudioElevated,
                    textColor = StudioRed,
                    icon = Icons.AutoMirrored.Filled.ExitToApp
                )
            }
        }

        // Section 6: Project Footer (Exact Web Parity)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("SyncWave", color = Zinc300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("•", color = Zinc500, fontSize = 12.sp)
                Text("MIT License", color = Zinc500, fontSize = 12.sp)
                Text("•", color = Zinc500, fontSize = 12.sp)
                Text("v1.0.0", color = Zinc500, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }

            // GitHub Repository Link Button (Matching Web)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SkrinVex/SyncWave"))
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "GitHub",
                    tint = StudioAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "GitHub Repository",
                    color = StudioAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Edit Server URL Dialog
    if (uiState.isEditServerUrlModalOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeEditServerUrlModal() },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Сменить адрес сервера", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioTextField(
                        value = uiState.newServerUrl,
                        onValueChange = { viewModel.onNewServerUrlChange(it) },
                        label = "URL адрес",
                        placeholder = "https://syncwave.skrinvex.com"
                    )
                }
            },
            confirmButton = {
                StudioButton(
                    text = "Сохранить",
                    onClick = { viewModel.saveServerUrl() },
                    modifier = Modifier.width(110.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeEditServerUrlModal() }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }

    // Google Auth WebView Modal Dialog
    if (uiState.isGoogleAuthModalOpen) {
        GoogleAuthWebViewModal(
            onDismiss = { viewModel.closeGoogleAuthModal() },
            onCookiesSynced = { viewModel.refreshSettings() }
        )
    }

    // Delete Cookies Confirmation Dialog
    if (showDeleteCookiesDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCookiesDialog = false },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Удаление YouTube Cookies", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Вы действительно хотите удалить cookies авторизации? Синхронизация закрытых плейлистов и треков станет недоступна до повторного входа.", color = Zinc400, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteCookiesDialog = false
                    viewModel.deleteCookies()
                }) {
                    Text("Удалить", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCookiesDialog = false }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = StudioSurface,
            title = {
                Text("Выход из системы", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Вы уверены, что хотите выйти из аккаунта? Потребуется повторный вход.", color = Zinc400, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) {
                    Text("Выйти", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }

    // Downloaded Tracks Management Modal Bottom Sheet
    if (uiState.isDownloadsSheetOpen) {
        DownloadedTracksBottomSheet(
            downloadedTracks = uiState.downloadedTracks,
            totalStorageFormatted = uiState.downloadedTotalStorageFormatted,
            onDismiss = { viewModel.closeDownloadsSheet() },
            onPlayTrack = { track -> viewModel.playDownloadedTrack(track) },
            onDeleteTrack = { trackId -> viewModel.deleteDownloadedTrack(trackId) },
            onDeleteAll = { viewModel.deleteAllDownloadedTracks() }
        )
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(StudioElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = Zinc400,
                maxLines = 1
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Zinc100
            )
        }
    }
}

@Composable
fun SettingsMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Zinc400)
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = Zinc100
        )
    }
}
