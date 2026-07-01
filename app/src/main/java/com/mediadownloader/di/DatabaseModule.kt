package com.mediadownloader.di

import android.content.Context
import com.mediadownloader.data.local.database.AppDatabase
import com.mediadownloader.data.local.database.DownloadDao
import com.mediadownloader.data.local.database.UrlDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideDownloadDao(db: AppDatabase): DownloadDao = db.downloadDao()

    @Provides
    @Singleton
    fun provideUrlDao(db: AppDatabase): UrlDao = db.urlDao()
}
