package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import kotlin.math.roundToInt

@Composable
fun StudioAudioScrubber(
    currentPositionMs: Long,
    bufferedPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val totalMs = durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(1f) }

    val actualProgress = if (isDragging) {
        dragProgress
    } else {
        (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    }

    val bufferedProgress = (bufferedPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    val thumbSize by animateDpAsState(
        targetValue = if (isDragging) 16.dp else 12.dp,
        label = "thumb_size"
    )

    val thumbRadiusPx = with(density) { (if (isDragging) 8.dp else 6.dp).roundToPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(totalMs) {
                detectTapGestures(
                    onPress = { offset ->
                        val targetFrac = (offset.x / widthPx).coerceIn(0f, 1f)
                        onSeek((targetFrac * totalMs).toLong())
                    }
                )
            }
            .pointerInput(totalMs) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / widthPx).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek((dragProgress * totalMs).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / widthPx).coerceIn(0f, 1f)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Background Inactive Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF27272A))
        )

        // Buffered Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(bufferedProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF52525B))
        )

        // Played Active Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(actualProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(StudioAccent)
        )

        // Draggable Scrubber Thumb
        val thumbOffsetXPx = (actualProgress * widthPx).coerceIn(0f, widthPx)
        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetXPx.roundToInt() - thumbRadiusPx, y = 0) }
                .size(thumbSize)
                .shadow(6.dp, CircleShape, spotColor = StudioAccent)
                .clip(CircleShape)
                .background(Zinc100)
        )
    }
}
