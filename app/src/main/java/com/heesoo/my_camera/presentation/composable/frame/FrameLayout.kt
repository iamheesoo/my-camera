package com.heesoo.my_camera.presentation.composable.frame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heesoo.my_camera.domain.model.PhotoFrame

@Composable
internal fun FrameLayout(
    modifier: Modifier = Modifier,
    frame: PhotoFrame,
    slot: @Composable (Int) -> Unit,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(frame.rowCount) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(frame.columnCount) { column ->
                    Box(
                        modifier = Modifier.weight(1f)
                            .aspectRatio(4f / 3f)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        slot(row * frame.columnCount + column)
                    }
                }
            }
        }
    }
}
