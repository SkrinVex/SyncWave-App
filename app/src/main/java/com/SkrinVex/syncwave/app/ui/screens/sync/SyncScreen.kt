package com.SkrinVex.syncwave.app.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.ui.components.StudioBadge
import com.SkrinVex.syncwave.app.ui.components.StudioButton
import com.SkrinVex.syncwave.app.ui.components.StudioCard
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
fun SyncScreen(
    viewModel: SyncViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = uiState.progress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Header
        Text(
            text = "Синхронизация",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Zinc100
        )
        Text(
            text = "Телеметрия воркера и системные логи",
            fontSize = 12.sp,
            color = Zinc400,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Worker Telemetry Status Card
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
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (progress.active) StudioEmerald else Zinc500)
                        )
                        Text(
                            text = if (progress.active) "Воркер активен" else "Воркер свободен (Ожидание)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                    }

                    if (progress.active) {
                        StudioBadge(
                            text = String.format("%.1f%%", progress.percentage),
                            backgroundColor = StudioElevated,
                            textColor = StudioAccent
                        )
                    }
                }

                if (progress.active) {
                    // Active Progress Details
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = progress.currentTrackTitle.ifBlank { "Подготовка загрузки..." },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Zinc300,
                            maxLines = 1
                        )

                        LinearProgressIndicator(
                            progress = { (progress.percentage / 100f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = StudioAccent,
                            trackColor = StudioElevated
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${progress.currentTrackIndex} / ${progress.totalTracks} треков",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Zinc500
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (progress.speed.isNotBlank()) {
                                    Text(
                                        text = progress.speed,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = StudioAccent
                                    )
                                }
                                if (progress.eta.isNotBlank()) {
                                    Text(
                                        text = "ETA ${progress.eta}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Zinc400
                                    )
                                }
                            }
                        }
                    }

                    // Cancel Sync Button
                    StudioButton(
                        text = "Остановить синхронизацию",
                        onClick = { viewModel.cancelSync() },
                        backgroundColor = StudioElevated,
                        textColor = StudioRed,
                        icon = Icons.Default.Close
                    )
                } else {
                    // Trigger All Sync Button
                    StudioButton(
                        text = "Синхронизировать все плейлисты",
                        onClick = { viewModel.triggerSyncAll() },
                        isLoading = uiState.isTriggering,
                        icon = Icons.Default.Sync
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Worker Console Logs Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Логи выполнения (${uiState.logs.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Zinc300
            )

            if (uiState.logs.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearLogs() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Очистить логи",
                        tint = Zinc500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Console Log Terminal Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(StudioSurface)
                .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            if (uiState.logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Логи пусты. Запустите синхронизацию для просмотра сообщений воркера.",
                        fontSize = 12.sp,
                        color = Zinc500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        SyncLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncLogItem(log: SyncLog) {
    val levelColor = when (log.level.lowercase()) {
        "success" -> StudioEmerald
        "warn" -> StudioWarn
        "error" -> StudioRed
        else -> StudioAccent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StudioElevated.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Level Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(levelColor.copy(alpha = 0.15f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = log.level.uppercase(),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = levelColor
            )
        }

        // Message
        Text(
            text = log.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Zinc300,
            modifier = Modifier.weight(1f)
        )
    }
}
