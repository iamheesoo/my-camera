package com.heesoo.my_camera.data.di

import com.heesoo.my_camera.data.local.MediaStorePhotoDataSource
import com.heesoo.my_camera.data.local.MediaStorePhotoDataSourceImpl
import com.heesoo.my_camera.data.local.PhotoCompositionDataSource
import com.heesoo.my_camera.data.local.PhotoCompositionDataSourceImpl
import com.heesoo.my_camera.data.local.TutorialLocalDataSource
import com.heesoo.my_camera.data.local.TutorialLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindPhotoComposition(implementation: PhotoCompositionDataSourceImpl): PhotoCompositionDataSource

    @Binds
    @Singleton
    abstract fun bindMediaStorePhoto(implementation: MediaStorePhotoDataSourceImpl): MediaStorePhotoDataSource
    @Binds
    @Singleton
    abstract fun bindTutorialLocalDataSource(implementation: TutorialLocalDataSourceImpl): TutorialLocalDataSource
}
