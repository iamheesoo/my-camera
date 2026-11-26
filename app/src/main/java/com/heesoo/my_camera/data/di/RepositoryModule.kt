package com.heesoo.my_camera.data.di

import com.heesoo.my_camera.data.repository.PhotoRepositoryImpl
import com.heesoo.my_camera.data.repository.TutorialRepositoryImpl
import com.heesoo.my_camera.domain.repository.PhotoRepository
import com.heesoo.my_camera.domain.repository.TutorialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(implementation: PhotoRepositoryImpl): PhotoRepository
    @Binds
    @Singleton
    abstract fun bindTutorialRepository(implementation: TutorialRepositoryImpl): TutorialRepository
}
