package com.mediadownloader.di

import com.mediadownloader.data.repository.DownloadRepositoryImpl
import com.mediadownloader.data.repository.UrlRepositoryImpl
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.domain.repository.UrlRepository
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
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindUrlRepository(impl: UrlRepositoryImpl): UrlRepository
}
