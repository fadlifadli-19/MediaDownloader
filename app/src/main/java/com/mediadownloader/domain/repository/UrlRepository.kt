package com.mediadownloader.domain.repository

import com.mediadownloader.core.result.Resource
import com.mediadownloader.domain.model.Url
import kotlinx.coroutines.flow.Flow

interface UrlRepository {
    fun getAllUrls(): Flow<List<Url>>
    suspend fun getUrlById(id: Long): Url?
    suspend fun insertUrl(url: Url): Resource<Long>
    suspend fun updateUrl(url: Url): Resource<Unit>
    suspend fun deleteUrl(id: Long): Resource<Unit>
    suspend fun deleteAllUrls(): Resource<Unit>
    fun searchUrls(query: String): Flow<List<Url>>
    suspend fun upsertUrl(url: Url): Resource<Long>
}
