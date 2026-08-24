package com.SkrinVex.syncwave.app.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        Text(
            text = "Настройки",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Zinc100
        )

        // User Profile Card
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioAccent.copy(alpha = 0.15f))
                                .border(1.dp, StudioAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
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
                                text = if (uiState.user?.isAdmin == true) "Администратор сервера" else "Пользователь",
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

                // Storage Quota Progress
                if (uiState.settings.userStorageQuotaBytes > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Использование квоты", fontSize = 11.sp, color = Zinc400)
                            Text(
                                text = "${uiState.settings.formattedUserStorage} / ${uiState.settings.formattedUserQuota}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Zinc300
                            )
                        }
                        LinearProgressIndicator(
                            progress = { uiState.settings.quotaUsageFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = StudioAccent,
                            trackColor = StudioElevated
                        )
                    }
                }
            }
        }

        // Server Connection Card
        StudioCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        text = "ACTIVE",
                        backgroundColor = StudioEmerald.copy(alpha = 0.15f),
                        textColor = StudioEmerald
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioElevated)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
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
                    IconButton(
                        onClick = { viewModel.openEditServerUrlModal() },
                        modifier = Modifier.size(24.dp)
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
        }

        // System Diagnostics Card
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

                SettingsMetricRow("Аудиокодек", uiState.settings.audioFormat.uppercase())
                SettingsMetricRow("Статус Cookies", uiState.settings.cookiesStatus.uppercase())
                SettingsMetricRow("Всего треков на сервере", "${uiState.settings.totalTracksCount}")
                SettingsMetricRow("Версия yt-dlp", uiState.settings.ytdlpVersion.ifBlank { "Встроенный" })
                SettingsMetricRow("Версия FFmpeg", uiState.settings.ffmpegVersion.ifBlank { "Встроенный" })
            }
        }

        // Logout Button
        StudioButton(
            text = "Выйти из аккаунта",
            onClick = { showLogoutDialog = true },
            backgroundColor = StudioElevated,
            textColor = StudioRed,
            icon = Icons.Default.ExitToApp
        )
    }

    // Edit Server URL Dialog
    if (uiState.isEditServerUrlModalOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeEditServerUrlModal() },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Сменить сервер", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                Text("Вы уверены, что хотите выйти из учетной записи?", color = Zinc400, fontSize = 13.sp)
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
