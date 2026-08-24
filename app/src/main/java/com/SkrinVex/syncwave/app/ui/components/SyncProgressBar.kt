package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SkrinVex.syncwave.app.domain.model.SyncProgress
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioEmerald
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@Composable
fun SyncProgressBar(
    syncProgress: SyncProgress,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = syncProgress.active) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurface)
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SYNCING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = StudioEmerald
                        )
                    }
                    Text(
                        text = syncProgress.currentTrackTitle.ifBlank { "Синхронизация..." },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Zinc100,
                        maxLines = 1
                    )
                }

                Text(
                    text = String.format("%.1f%%", syncProgress.percentage),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioAccent
                )
            }

            LinearProgressIndicator(
                progress = { (syncProgress.percentage / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = StudioAccent,
                trackColor = StudioElevated,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (syncProgress.totalTracks > 0) "${syncProgress.currentTrackIndex} / ${syncProgress.totalTracks} треков" else "",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Zinc500
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (syncProgress.speed.isNotBlank()) {
                        Text(
                            text = syncProgress.speed,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc400
                        )
                    }
                    if (syncProgress.eta.isNotBlank()) {
                        Text(
                            text = "ETA ${syncProgress.eta}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc500
                        )
                    }
                }
            }
        }
    }
}
