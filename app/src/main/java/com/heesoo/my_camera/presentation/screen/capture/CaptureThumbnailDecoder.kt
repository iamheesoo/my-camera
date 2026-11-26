package com.heesoo.my_camera.presentation.screen.capture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.heesoo.my_camera.core.di.IoDispatcher
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class CaptureThumbnailDecoder @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun decode(photoFile: File): Bitmap? = withContext(ioDispatcher) {
        try {
            val exif = ExifInterface(photoFile)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(photoFile.path, options)
            val longestSide = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (longestSide / (sampleSize * 2) >= THUMBNAIL_MAX_SIDE) {
                sampleSize *= 2
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize
            BitmapFactory.decodeFile(photoFile.path, options)?.let { bitmap ->
                val transform = Matrix().apply {
                    if (exif.isFlipped) {
                        setScale(-1f, 1f)
                    }
                    postRotate(exif.rotationDegrees.toFloat())
                }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transform, true)
            }
        } catch (_: IOException) {
            null
        }
    }

    private companion object {
        const val THUMBNAIL_MAX_SIDE = 512
    }
}
