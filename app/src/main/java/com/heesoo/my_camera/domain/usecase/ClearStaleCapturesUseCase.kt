package com.heesoo.my_camera.domain.usecase

import com.heesoo.my_camera.domain.repository.PhotoRepository
import javax.inject.Inject

internal class ClearStaleCapturesUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke() = repository.clearStaleCaptures()
}
