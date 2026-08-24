package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc400

@Composable
fun TrackCardItem(
    track: Track,
    coverUrl: String,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl)
            .size(256, 256)
            .crossfade(100)
            .build()
    }

    StudioCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = StudioSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Artwork Image with Equalizer overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioElevated)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        StudioEqualizerAnimation(
                            isPlaying = isPlaying,
                            maxHeight = 22.dp,
                            barWidth = 4.dp,
                            color = StudioAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = track.title,
                fontSize = 12.sp,
                fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrentTrack) StudioAccent else Zinc100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Artist
            Text(
                text = track.artist.ifBlank { "Unknown Artist" },
                fontSize = 10.sp,
                color = Zinc400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
