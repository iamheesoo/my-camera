package com.heesoo.my_camera.data.repository

import com.heesoo.my_camera.core.di.IoDispatcher
import com.heesoo.my_camera.data.local.MediaStorePhotoDataSource
import com.heesoo.my_camera.data.local.PhotoCompositionDataSource
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.domain.repository.PhotoRepository
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PhotoRepositoryImpl @Inject constructor(
    private val compositionDataSource: PhotoCompositionDataSource,
    private val mediaStoreDataSource: MediaStorePhotoDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PhotoRepository {
    override suspend fun save(frame: PhotoFrame, photoFileList: List<File>) = withContext(ioDispatcher) {
        val photo = compositionDataSource.compose(frame = frame, photoFileList = photoFileList)
        try {
            mediaStoreDataSource.save(photo)
        } finally {
            photo.recycle()
        }
    }
}
