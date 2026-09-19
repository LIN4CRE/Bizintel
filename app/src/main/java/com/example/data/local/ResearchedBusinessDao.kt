package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResearchedBusinessDao {
    @Query("SELECT * FROM researched_businesses ORDER BY researchedAt DESC")
    fun getAllResearched(): Flow<List<ResearchedBusinessEntity>>

    @Query("SELECT * FROM researched_businesses WHERE isBookmarked = 1 ORDER BY researchedAt DESC")
    fun getBookmarks(): Flow<List<ResearchedBusinessEntity>>

    @Query("SELECT * FROM researched_businesses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ResearchedBusinessEntity?

    @Query("SELECT * FROM researched_businesses WHERE LOWER(companyName) = LOWER(:name) LIMIT 1")
    suspend fun getByName(name: String): ResearchedBusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ResearchedBusinessEntity): Long

    @Update
    suspend fun update(entity: ResearchedBusinessEntity)

    @Query("UPDATE researched_businesses SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Long, isBookmarked: Boolean)

    @Query("UPDATE researched_businesses SET pdfUri = :pdfUri WHERE id = :id")
    suspend fun updatePdfUri(id: Long, pdfUri: String)

    @Query("DELETE FROM researched_businesses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM researched_businesses")
    suspend fun clearAll()
}
