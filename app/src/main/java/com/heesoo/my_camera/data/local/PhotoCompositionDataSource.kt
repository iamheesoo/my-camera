package com.heesoo.my_camera.data.local

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.heesoo.my_camera.domain.model.PhotoFrame
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal interface PhotoCompositionDataSource {
    suspend fun compose(frame: PhotoFrame, photoFileList: List<File>): Bitmap
}

internal class PhotoCompositionDataSourceImpl @Inject constructor(
    private val decoder: CaptureBitmapDecoder,
) : PhotoCompositionDataSource {
    override suspend fun compose(frame: PhotoFrame, photoFileList: List<File>): Bitmap {
        require(photoFileList.size == frame.slotCount)
        val output = Bitmap.createBitmap(frame.width * OUTPUT_SCALE, frame.height * OUTPUT_SCALE, Bitmap.Config.ARGB_8888)
        var isComplete = false
        try {
            val canvas = Canvas(output)
            canvas.drawColor(Color.WHITE)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            photoFileList.forEachIndexed { slotIndex, photoFile ->
                currentCoroutineContext().ensureActive()
                val photo = decoder.decode(photoFile = photoFile, targetLongestSide = PhotoFrame.SLOT_WIDTH * OUTPUT_SCALE)
                    ?: throw IOException("Could not decode slot ${slotIndex + 1}")
                try {
                    val left = frame.slotLeft(slotIndex) * OUTPUT_SCALE
                    val top = frame.slotTop(slotIndex) * OUTPUT_SCALE
                    val destination = Rect(left, top, left + PhotoFrame.SLOT_WIDTH * OUTPUT_SCALE, top + PhotoFrame.SLOT_HEIGHT * OUTPUT_SCALE)
                    canvas.drawBitmap(photo, centerCrop(photo), destination, paint)
                } finally {
                    photo.recycle()
                }
            }
            isComplete = true
            return output
        } finally {
            if (!isComplete) {
                output.recycle()
            }
        }
    }

    private fun centerCrop(photo: Bitmap): Rect {
        val width = minOf(
            photo.width,
            (photo.height * PhotoFrame.SLOT_ASPECT_RATIO).toInt().coerceAtLeast(1),
        )
        val height = minOf(photo.height, (width / PhotoFrame.SLOT_ASPECT_RATIO).toInt().coerceAtLeast(1))
        val left = (photo.width - width) / 2
        val top = (photo.height - height) / 2
        return Rect(left, top, left + width, top + height)
    }

    private companion object {
        const val OUTPUT_SCALE = 8
    }
}
