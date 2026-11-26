package com.heesoo.my_camera.presentation.screen.capture

import android.graphics.Bitmap
import com.heesoo.my_camera.core.di.IoDispatcher
import com.heesoo.my_camera.data.local.CaptureBitmapDecoder
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class CaptureThumbnailDecoder @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val decoder: CaptureBitmapDecoder,
) {
    suspend fun decode(photoFile: File, slotWidthPx: Int, slotHeightPx: Int): Bitmap? = withContext(ioDispatcher) {
        try {
            decoder.decode(
                photoFile = photoFile,
                targetLongestSide = maxOf(slotWidthPx, PREVIEW_MIN_LONGEST_SIDE),
                targetShortestSide = slotHeightPx,
            )
        } catch (_: IOException) {
            null
        }
    }

    private companion object {
        const val PREVIEW_MIN_LONGEST_SIDE = 960
    }
}
