package com.heesoo.my_camera.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class CaptureCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cleanupMutex = Mutex()
    private var hasCleaned = false

    suspend fun clearStaleFiles() = cleanupMutex.withLock {
        if (!hasCleaned) {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && CAPTURE_FILE_NAME.matches(file.name)) {
                    file.delete()
                }
            }
            hasCleaned = true
        }
    }

    companion object {
        private val CAPTURE_FILE_NAME = Regex(
            "(?:photobooth|camera-check)-[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.jpg",
        )

        fun newPhotoFile(cacheDirectory: File): File =
            File(cacheDirectory, "photobooth-${UUID.randomUUID()}.jpg")
    }
}
