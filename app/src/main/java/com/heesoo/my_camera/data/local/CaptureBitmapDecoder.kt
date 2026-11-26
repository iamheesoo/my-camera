package com.heesoo.my_camera.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import javax.inject.Inject

internal class CaptureBitmapDecoder @Inject constructor() {
    fun decode(
        photoFile: File,
        targetLongestSide: Int,
        targetShortestSide: Int = 1,
    ): Bitmap? {
        val exif = ExifInterface(photoFile)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(photoFile.path, options)
        val longestSide = maxOf(options.outWidth, options.outHeight)
        val shortestSide = minOf(options.outWidth, options.outHeight)
        var sampleSize = 1
        while (longestSide / (sampleSize * 2) >= targetLongestSide &&
            shortestSide / (sampleSize * 2) >= targetShortestSide
        ) {
            sampleSize *= 2
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize
        return BitmapFactory.decodeFile(photoFile.path, options)?.let { bitmap ->
            val transform = Matrix().apply {
                if (exif.isFlipped) {
                    setScale(-1f, 1f)
                }
                postRotate(exif.rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transform, true).also { decoded ->
                if (decoded !== bitmap) {
                    bitmap.recycle()
                }
            }
        }
    }
}
