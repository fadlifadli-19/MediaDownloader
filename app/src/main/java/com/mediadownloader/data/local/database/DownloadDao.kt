package com.mediadownloader.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mediadownloader.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadEntity)

    @Update
    suspend fun update(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun deleteAllCompleted()

    @Query("SELECT * FROM downloads ORDER BY created_at DESC")
    fun getAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY created_at DESC")
    fun getByStatus(status: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun getById(id: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun search(query: String): Flow<List<DownloadEntity>>

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE downloads SET progress_percentage = :percentage, progress_speed = :speed, progress_eta = :eta WHERE id = :id")
    suspend fun updateProgress(id: String, percentage: Float, speed: String, eta: String)

    @Query("SELECT COUNT(*) FROM downloads WHERE status IN ('DOWNLOADING', 'QUEUED')")
    suspend fun getActiveCount(): Int
}
