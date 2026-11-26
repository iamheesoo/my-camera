package com.heesoo.my_camera.data.di

import com.heesoo.my_camera.data.repository.PhotoRepositoryImpl
import com.heesoo.my_camera.domain.repository.PhotoRepository
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
}
