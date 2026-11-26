package com.heesoo.my_camera.presentation.screen.capture.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
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
    loadThumbnail: suspend (File) -> Bitmap?,
) {
    val maxWidth = when (frame) {
        PhotoFrame.STRIP -> 240.dp
        PhotoFrame.GRID -> 480.dp
    }
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FrameLayout(modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(), frame = frame) { slotIndex ->
            val photoFile = photoFileList[slotIndex]
            val thumbnail by produceState<Bitmap?>(initialValue = null, key1 = photoFile) {
                value = loadThumbnail(photoFile)
            }
            val bitmap = thumbnail
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.captured_slot, slotIndex + 1),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (slotIndex + 1).toString(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
