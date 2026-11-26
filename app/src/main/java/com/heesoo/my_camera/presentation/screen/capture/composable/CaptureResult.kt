package com.heesoo.my_camera.presentation.screen.capture.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.heesoo.my_camera.R
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.composable.frame.FrameLayout
import java.io.File

@Composable
internal fun CaptureResult(
    modifier: Modifier = Modifier,
    frame: PhotoFrame,
    photoFileList: List<File>,
    selectedSlotIndex: Int?,
    isSlotSelectionEnabled: Boolean,
    onClickSlot: (Int) -> Unit,
    loadThumbnail: suspend (File, Int, Int) -> Bitmap?,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val frameAspectRatio = frame.width.toFloat() / frame.height
        val fittedWidth = minOf(maxWidth, maxHeight * frameAspectRatio)
        if (fittedWidth > 0.dp) {
            val (slotWidthPx, slotHeightPx) = with(LocalDensity.current) {
                val unit = fittedWidth / frame.width
                (unit * PhotoFrame.SLOT_WIDTH).roundToPx().coerceAtLeast(1) to
                    (unit * PhotoFrame.SLOT_HEIGHT).roundToPx().coerceAtLeast(1)
            }
            FrameLayout(
                modifier = Modifier.width(fittedWidth).selectableGroup(),
                frame = frame,
            ) { slotIndex ->
                val photoFile = photoFileList[slotIndex]
                val thumbnail by produceState<Bitmap?>(
                    initialValue = null,
                    key1 = photoFile,
                    key2 = slotWidthPx,
                    key3 = slotHeightPx,
                ) {
                    value = loadThumbnail(photoFile, slotWidthPx, slotHeightPx)
                }
                val bitmap = thumbnail
                val description = stringResource(R.string.captured_slot, slotIndex + 1)
                val isSelected = slotIndex == selectedSlotIndex
                val selectionBorder = if (isSelected) {
                    Modifier.border(width = 4.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                        .then(selectionBorder)
                        .selectable(
                            selected = isSelected,
                            enabled = isSlotSelectionEnabled,
                            role = Role.RadioButton,
                            onClick = { onClickSlot(slotIndex) },
                        )
                        .semantics { contentDescription = description },
                    contentAlignment = Alignment.Center,
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.Medium,
                        )
                    } else {
                        Text(text = (slotIndex + 1).toString(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
