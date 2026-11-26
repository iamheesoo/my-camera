package com.heesoo.my_camera.data.repository

import com.heesoo.my_camera.data.local.TutorialLocalDataSource
import com.heesoo.my_camera.domain.repository.TutorialRepository
import javax.inject.Inject

internal class TutorialRepositoryImpl @Inject constructor(
    private val localDataSource: TutorialLocalDataSource,
) : TutorialRepository {
    override suspend fun isCompleted(): Boolean = localDataSource.isCompleted()

    override suspend fun markCompleted() = localDataSource.markCompleted()
}
