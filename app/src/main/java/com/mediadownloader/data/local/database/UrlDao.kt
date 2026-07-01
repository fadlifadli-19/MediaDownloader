package com.mediadownloader.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mediadownloader.data.local.entity.UrlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UrlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UrlEntity): Long

    @Update
    suspend fun update(entity: UrlEntity)

    @Query("DELETE FROM urls WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM urls")
    suspend fun deleteAll()

    @Query("SELECT * FROM urls ORDER BY last_used DESC")
    fun getAll(): Flow<List<UrlEntity>>

    @Query("SELECT * FROM urls WHERE id = :id")
    suspend fun getById(id: Long): UrlEntity?

    @Query("SELECT * FROM urls WHERE raw_url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY last_used DESC")
    fun search(query: String): Flow<List<UrlEntity>>

    @Query("SELECT * FROM urls WHERE normalized_url = :normalizedUrl LIMIT 1")
    suspend fun getByNormalizedUrl(normalizedUrl: String): UrlEntity?

    @Query("UPDATE urls SET last_used = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)
}
