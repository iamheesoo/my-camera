package com.heesoo.my_camera.domain.usecase

import com.heesoo.my_camera.domain.repository.TutorialRepository
import javax.inject.Inject

internal class GetTutorialCompletionUseCase @Inject constructor(
    private val repository: TutorialRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isCompleted()
}
