package com.mediadownloader.data.repository

import com.mediadownloader.core.result.Resource
import com.mediadownloader.core.result.safeCall
import com.mediadownloader.data.local.database.UrlDao
import com.mediadownloader.domain.model.Url
import com.mediadownloader.domain.repository.UrlRepository
import com.mediadownloader.util.normalizeUrl
import com.mediadownloader.util.toDomain
import com.mediadownloader.util.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlRepositoryImpl @Inject constructor(
    private val dao: UrlDao
) : UrlRepository {

    override fun getAllUrls(): Flow<List<Url>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getUrlById(id: Long): Url? =
        dao.getById(id)?.toDomain()

    override suspend fun insertUrl(url: Url): Resource<Long> =
        safeCall { dao.insert(url.toEntity()) }

    override suspend fun updateUrl(url: Url): Resource<Unit> =
        safeCall { dao.update(url.toEntity()) }

    override suspend fun deleteUrl(id: Long): Resource<Unit> =
        safeCall { dao.deleteById(id) }

    override suspend fun deleteAllUrls(): Resource<Unit> =
        safeCall { dao.deleteAll() }

    override fun searchUrls(query: String): Flow<List<Url>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun upsertUrl(url: Url): Resource<Long> = safeCall {
        val normalized = url.rawUrl.normalizeUrl()
        val existing = dao.getByNormalizedUrl(normalized)
        if (existing != null) {
            dao.updateLastUsed(existing.id, System.currentTimeMillis())
            existing.id
        } else {
            dao.insert(url.copy(normalizedUrl = normalized).toEntity())
        }
    }
}
