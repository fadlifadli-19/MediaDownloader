package com.mediadownloader.di

import com.mediadownloader.core.executor.YtDlpProcessExecutor
import com.mediadownloader.core.native.BinaryInstaller
import com.mediadownloader.data.source.yt.YtDlpManager
import com.mediadownloader.data.source.yt.YtDlpParser
import com.mediadownloader.data.source.yt.YtDlpProcessHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object YtDlpModule {

    @Provides
    @Singleton
    fun provideYtDlpProcessExecutor(): YtDlpProcessExecutor = YtDlpProcessExecutor()

    @Provides
    @Singleton
    fun provideYtDlpParser(): YtDlpParser = YtDlpParser()

    @Provides
    @Singleton
    fun provideYtDlpProcessHandler(executor: YtDlpProcessExecutor): YtDlpProcessHandler =
        YtDlpProcessHandler(executor)

    @Provides
    @Singleton
    fun provideYtDlpManager(
        binaryInstaller: BinaryInstaller,
        parser: YtDlpParser,
        processHandler: YtDlpProcessHandler,
        processExecutor: YtDlpProcessExecutor
    ): YtDlpManager = YtDlpManager(binaryInstaller, parser, processHandler, processExecutor)
}
