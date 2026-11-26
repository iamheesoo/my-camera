package com.heesoo.my_camera.presentation.composable.frame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.heesoo.my_camera.domain.model.PhotoFrame

@Composable
internal fun FrameLayout(
    modifier: Modifier = Modifier,
    frame: PhotoFrame,
    slot: @Composable (Int) -> Unit,
) {
    BoxWithConstraints(modifier = modifier, propagateMinConstraints = true) {
        val unit = maxWidth / frame.width
        val padding = unit * PhotoFrame.FRAME_PADDING
        val gap = unit * PhotoFrame.SLOT_GAP
        Column(
            modifier = Modifier.background(Color.White).padding(padding),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            repeat(frame.rowCount) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    repeat(frame.columnCount) { column ->
                        Box(
                            modifier = Modifier.weight(1f)
                                .aspectRatio(PhotoFrame.SLOT_ASPECT_RATIO)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            slot(row * frame.columnCount + column)
                        }
                    }
                }
            }
        }
    }
}
