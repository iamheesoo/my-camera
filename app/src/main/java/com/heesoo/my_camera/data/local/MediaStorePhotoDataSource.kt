package com.heesoo.my_camera.data.local

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal interface MediaStorePhotoDataSource {
    suspend fun save(photo: Bitmap)
}

internal class MediaStorePhotoDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaStorePhotoDataSource {
    @Suppress("DEPRECATION")
    override suspend fun save(photo: Bitmap) {
        val resolver = context.contentResolver
        val fileName = "MyCamera-${UUID.randomUUID()}.jpg"
        val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (isScopedStorage) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/My Camera")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val directory = File(pictures, "My Camera")
                if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) {
                    throw IOException("Could not create pictures directory")
                }
                put(MediaStore.Images.Media.DATA, File(directory, fileName).absolutePath)
            }
        }
        currentCoroutineContext().ensureActive()
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Could not insert photo")
        var isSaved = false
        try {
            val output = resolver.openOutputStream(uri) ?: throw IOException("Could not open photo output")
            output.use { stream ->
                if (!photo.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                    throw IOException("Could not encode JPEG")
                }
            }
            currentCoroutineContext().ensureActive()
            if (isScopedStorage) {
                val published = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                if (resolver.update(uri, published, null, null) != 1) {
                    throw IOException("Could not publish photo")
                }
            }
            isSaved = true
        } finally {
            if (!isSaved) {
                runCatching { resolver.delete(uri, null, null) }
            }
        }
    }

    private companion object {
        const val JPEG_QUALITY = 92
    }
}
