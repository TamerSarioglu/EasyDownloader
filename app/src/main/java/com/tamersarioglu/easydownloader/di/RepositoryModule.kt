package com.tamersarioglu.easydownloader.di

import com.tamersarioglu.easydownloader.data.repository.VideoDownloaderRepositoryImpl
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVideoDownloaderRepository(
        repositoryImpl: VideoDownloaderRepositoryImpl
    ): VideoDownloaderRepository
}