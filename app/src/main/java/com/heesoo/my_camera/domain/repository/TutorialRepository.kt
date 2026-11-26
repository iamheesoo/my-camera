package com.heesoo.my_camera.domain.repository

internal interface TutorialRepository {
    suspend fun isCompleted(): Boolean

    suspend fun markCompleted()
}
