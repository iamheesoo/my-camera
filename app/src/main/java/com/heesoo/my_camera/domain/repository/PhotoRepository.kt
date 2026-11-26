package com.heesoo.my_camera.domain.repository

import com.heesoo.my_camera.domain.model.PhotoFrame
import java.io.File

internal interface PhotoRepository {
    suspend fun clearStaleCaptures()

    suspend fun save(frame: PhotoFrame, photoFileList: List<File>)
}
