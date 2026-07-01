package com.mediadownloader.data.repository

import com.mediadownloader.core.result.Resource
import com.mediadownloader.core.result.safeCall
import com.mediadownloader.data.local.database.DownloadDao
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.util.toDomain
import com.mediadownloader.util.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val dao: DownloadDao
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<Download>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getDownloadsByStatus(status: DownloadStatus): Flow<List<Download>> =
        dao.getByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override fun getDownloadById(id: String): Flow<Download?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun insertDownload(download: Download): Resource<String> =
        safeCall {
            dao.insert(download.toEntity())
            download.id
        }

    override suspend fun updateDownload(download: Download): Resource<Unit> =
        safeCall { dao.update(download.toEntity()) }

    override suspend fun updateStatus(id: String, status: DownloadStatus): Resource<Unit> =
        safeCall { dao.updateStatus(id, status.name) }

    override suspend fun updateProgress(
        id: String,
        percentage: Float,
        speed: String,
        eta: String
    ): Resource<Unit> = safeCall { dao.updateProgress(id, percentage, speed, eta) }

    override suspend fun deleteDownload(id: String): Resource<Unit> =
        safeCall { dao.deleteById(id) }

    override suspend fun deleteAllCompleted(): Resource<Unit> =
        safeCall { dao.deleteAllCompleted() }

    override fun searchDownloads(query: String): Flow<List<Download>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveDownloadCount(): Int = dao.getActiveCount()
}
