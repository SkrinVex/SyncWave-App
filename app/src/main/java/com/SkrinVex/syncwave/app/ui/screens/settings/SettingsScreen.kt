package com.SkrinVex.syncwave.app.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.SkrinVex.syncwave.app.ui.theme.StudioWarn
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAuth: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

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
        // Header
        Column {
            Text(
                text = "Настройки",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Zinc100
            )
            Text(
                text = "Параметры системы, хранилище и диагностика",
                fontSize = 12.sp,
                color = Zinc400,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Section 1: Library & Storage Statistics Card (Moved from LibraryView)
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
                        value = uiState.stats.formattedTotalStorageSize,
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
                if (uiState.settings.userStorageQuotaBytes > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Использование дисковой квоты", fontSize = 11.sp, color = Zinc400)
                            Text(
                                text = "${uiState.settings.formattedUserStorage} / ${uiState.settings.formattedUserQuota} (${(uiState.settings.quotaUsageFraction * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Zinc300
                            )
                        }
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

        // Section 2: Server Connection Card
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
                        text = "ONLINE",
                        backgroundColor = StudioEmerald.copy(alpha = 0.15f),
                        textColor = StudioEmerald
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
                                    tint = Zinc400,
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

        // Section 3: System Information & Diagnostics (Matching Web Diag)
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

        // Section 4: User Profile & Logout
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

        // Project Footer
        Text(
            text = "SyncWave • v1.0.0 • Studio Engine",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Zinc500,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
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
