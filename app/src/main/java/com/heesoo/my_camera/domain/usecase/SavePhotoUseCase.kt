package com.heesoo.my_camera.domain.usecase

import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.domain.repository.PhotoRepository
import java.io.File
import javax.inject.Inject

internal class SavePhotoUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(frame: PhotoFrame, photoFileList: List<File>) {
        repository.save(frame = frame, photoFileList = photoFileList)
    }
}
