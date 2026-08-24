package com.SkrinVex.syncwave.app.ui.screens.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.ui.components.StudioBadge
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
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val filteredLogs = when (uiState.selectedLogLevel) {
        "INFO" -> uiState.logs.filter { it.level.equals("info", ignoreCase = true) }
        "SUCCESS" -> uiState.logs.filter { it.level.equals("success", ignoreCase = true) }
        "WARN" -> uiState.logs.filter { it.level.equals("warn", ignoreCase = true) || it.level.equals("warning", ignoreCase = true) }
        "ERROR" -> uiState.logs.filter { it.level.equals("error", ignoreCase = true) }
        else -> uiState.logs
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Синхронизация",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Zinc100
                )
                Text(
                    text = "Фоновый сервис загрузки и логи yt-dlp",
                    fontSize = 12.sp,
                    color = Zinc400,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Sync All Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (uiState.progress.active) StudioElevated else StudioAccent)
                    .clickable(enabled = !uiState.progress.active && !uiState.isTriggering) { viewModel.triggerSyncAll() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.progress.active || uiState.isTriggering) {
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
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = if (uiState.progress.active) "Синхронизация..." else "Синхронизировать",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Zinc100
                )
            }
        }

        // Active Sync Task Card (if active)
        AnimatedVisibility(visible = uiState.progress.active) {
            val progress = uiState.progress
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = StudioSurface,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StudioEmerald)
                            )
                            Text(
                                text = "АКТИВНАЯ ЗАГРУЗКА",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StudioEmerald
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${progress.percentage.toInt()}%",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StudioEmerald
                            )

                            // Cancel Button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioRed.copy(alpha = 0.15f))
                                    .clickable { viewModel.cancelSync() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = StudioRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text("Прервать", color = StudioRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Text(
                        text = progress.currentTrackTitle.ifBlank { "Подготовка загрузки..." },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100
                    )

                    // Track Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        LinearProgressIndicator(
                            progress = { ((progress.trackPercentage / 100.0).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = StudioAccent,
                            trackColor = StudioElevated
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${progress.playlistTitle.ifBlank { "YouTube Music" }} • Трек ${progress.currentTrackIndex} из ${progress.totalTracks}",
                                fontSize = 10.sp,
                                color = Zinc400
                            )
                            if (progress.speed.isNotBlank()) {
                                Text(
                                    text = "${progress.speed} (ETA: ${progress.eta})",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Zinc400
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Terminal Console
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0A0A0D))
                .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
        ) {
            // Terminal Header Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(StudioElevated)
                    .border(1.dp, StudioBorder, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Window dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "daemon.log",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Zinc400
                    )
                }

                // Actions: Copy, Clear, and Level Filter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy Logs Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurface)
                            .clickable {
                                val text = uiState.logs.joinToString("\n") { "[${it.level.uppercase()}] ${it.message}" }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Sync Logs", text))
                                Toast.makeText(context, "Логи скопированы в буфер", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Zinc400,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Clear Logs Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurface)
                            .clickable { viewModel.openConfirmClearLogs() }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Zinc400,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Log Level Filters (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F12))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "INFO", "SUCCESS", "WARN", "ERROR").forEach { lvl ->
                    val isSelected = uiState.selectedLogLevel == lvl
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) StudioAccent else StudioElevated)
                            .clickable { viewModel.selectLogLevel(lvl) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = lvl,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Zinc100 else Zinc400
                        )
                    }
                }
            }

            // Terminal Content Stream
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет записей в журнале",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Zinc500
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLogs) { log ->
                        val badgeColor = when (log.level.lowercase()) {
                            "error" -> StudioRed
                            "warn", "warning" -> StudioWarn
                            "success" -> StudioEmerald
                            else -> Zinc500
                        }

                        val textColor = when (log.level.lowercase()) {
                            "error" -> StudioRed.copy(alpha = 0.9f)
                            "warn", "warning" -> StudioWarn.copy(alpha = 0.9f)
                            "success" -> StudioEmerald.copy(alpha = 0.9f)
                            else -> Zinc300
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            StudioBadge(
                                text = log.level.uppercase(),
                                backgroundColor = badgeColor.copy(alpha = 0.15f),
                                textColor = badgeColor
                            )

                            Text(
                                text = log.message,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Clear Logs Confirmation Dialog
    if (uiState.isConfirmClearLogsOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeConfirmClearLogs() },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Очистка журнала логов",
                    color = Zinc100,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите полностью очистить журнал логов синхронизации?",
                    color = Zinc400,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearLogs() }) {
                    Text("Очистить", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeConfirmClearLogs() }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }
}
