package com.tamersarioglu.easydownloader.di

import com.tamersarioglu.easydownloader.data.repository.VideoDownloaderRepositoryImpl
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing repository and use case dependencies.
 * 
 * This module binds repository interfaces to their implementations and ensures
 * proper dependency injection for all use cases throughout the application.
 * 
 * All use cases are automatically provided by Hilt since they have @Inject constructors
 * and depend on the repository interface which is bound here.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the VideoDownloaderRepository interface to its implementation.
     * 
     * This binding allows Hilt to inject the repository implementation wherever
     * the repository interface is required (e.g., in use cases).
     * 
     * The implementation is scoped as Singleton to ensure a single instance
     * throughout the application lifecycle.
     */
    @Binds
    @Singleton
    abstract fun bindVideoDownloaderRepository(
        repositoryImpl: VideoDownloaderRepositoryImpl
    ): VideoDownloaderRepository
}