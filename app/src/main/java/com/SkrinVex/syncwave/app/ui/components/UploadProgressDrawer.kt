package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.UploadStatus
import com.SkrinVex.syncwave.app.domain.model.UploadTask
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
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
fun UploadProgressDrawer(
    modifier: Modifier = Modifier
) {
    val uploadManager = SyncWaveApplication.instance.container.uploadManager
    val tasks by uploadManager.tasks.collectAsStateWithLifecycle()
    val isMinimized by uploadManager.isMinimized.collectAsStateWithLifecycle()
    val isUploading by uploadManager.isUploading.collectAsStateWithLifecycle()
    val completedCount by uploadManager.completedTasks.collectAsStateWithLifecycle()
    val failedCount by uploadManager.failedTasks.collectAsStateWithLifecycle()
    val overallProgress by uploadManager.overallProgress.collectAsStateWithLifecycle()

    if (tasks.isEmpty()) return

    AnimatedVisibility(
        visible = tasks.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (isMinimized) {
            // Minimized Floating Widget Card
            MinimizedUploadCard(
                isUploading = isUploading,
                completedCount = completedCount,
                totalCount = tasks.size,
                progress = overallProgress,
                onExpand = { uploadManager.setMinimized(false) },
                onClear = { uploadManager.clearCompleted() }
            )
        } else {
            // Expanded Detailed Upload Card
            ExpandedUploadCard(
                tasks = tasks,
                completedCount = completedCount,
                failedCount = failedCount,
                progress = overallProgress,
                onMinimize = { uploadManager.setMinimized(true) },
                onClear = { uploadManager.clearCompleted() },
                onRemoveTask = { uploadManager.removeTask(it) }
            )
        }
    }
}

@Composable
fun MinimizedUploadCard(
    isUploading: Boolean,
    completedCount: Int,
    totalCount: Int,
    progress: Int,
    onExpand: () -> Unit,
    onClear: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isUploading) -4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(StudioSurface.copy(alpha = 0.96f))
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable { onExpand() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioAccent.copy(alpha = 0.15f))
                        .border(1.dp, StudioAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = StudioAccent,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { translationY = bounceOffset }
                    )
                }

                // Text & Progress
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUploading) "Загрузка треков..." else "Загрузка завершена",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                        Text(
                            text = "$completedCount/$totalCount",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StudioAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = StudioAccent,
                        trackColor = StudioElevated
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExpand, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Развернуть",
                        tint = Zinc400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (!isUploading) {
                    IconButton(onClick = onClear, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = Zinc400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandedUploadCard(
    tasks: List<UploadTask>,
    completedCount: Int,
    failedCount: Int,
    progress: Int,
    onMinimize: () -> Unit,
    onClear: () -> Unit,
    onRemoveTask: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(StudioSurface.copy(alpha = 0.98f))
            .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioElevated.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioAccent.copy(alpha = 0.15f))
                            .border(1.dp, StudioAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = StudioAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Очередь загрузки",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Zinc100
                        )
                        Text(
                            text = "$completedCount из ${tasks.size} ($progress%)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc400
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (completedCount > 0 || failedCount > 0) {
                        Text(
                            text = "Очистить",
                            fontSize = 11.sp,
                            color = Zinc400,
                            modifier = Modifier
                                .clickable { onClear() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onMinimize, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Свернуть",
                            tint = Zinc400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Overall Progress Bar
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = StudioAccent,
                trackColor = StudioElevated
            )

            // Task Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    UploadTaskRow(
                        task = task,
                        onDismiss = { onRemoveTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UploadTaskRow(
    task: UploadTask,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioElevated.copy(alpha = 0.3f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = task.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Zinc100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (task.size > 0) {
                    Text(
                        text = task.formattedSize,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Zinc500
                    )
                }

                when (task.status) {
                    UploadStatus.UPLOADING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(StudioAccent)
                            )
                            Text(
                                text = "${task.progress}%",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = StudioAccent
                            )
                        }
                    }
                    UploadStatus.PROCESSING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(StudioAccent)
                            )
                            Text(
                                text = "Обработка сервера...",
                                fontSize = 10.sp,
                                color = StudioAccent
                            )
                        }
                    }
                    UploadStatus.DONE -> {
                        Text(
                            text = "✓ Загружен",
                            fontSize = 10.sp,
                            color = StudioEmerald
                        )
                    }
                    UploadStatus.ERROR -> {
                        Text(
                            text = "✕ ${task.errorMessage.ifBlank { "Ошибка" }}",
                            fontSize = 10.sp,
                            color = StudioRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    UploadStatus.PENDING -> {
                        Text(
                            text = "В очереди",
                            fontSize = 10.sp,
                            color = Zinc500
                        )
                    }
                }
            }
        }

        // Right Status / Dismiss Action
        when (task.status) {
            UploadStatus.UPLOADING, UploadStatus.PROCESSING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = StudioAccent,
                    strokeWidth = 2.dp
                )
            }
            UploadStatus.DONE -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = StudioEmerald,
                    modifier = Modifier.size(16.dp)
                )
            }
            UploadStatus.ERROR -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    tint = StudioRed,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDismiss() }
                )
            }
            UploadStatus.PENDING -> {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Zinc500)
                )
            }
        }
    }
}

