package com.mediadownloader.di

import com.mediadownloader.core.dispatcher.AppDispatcherProvider
import com.mediadownloader.core.dispatcher.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: AppDispatcherProvider): DispatcherProvider
}
