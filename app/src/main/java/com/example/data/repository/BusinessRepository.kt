package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ResearchedBusinessEntity
import com.example.data.model.BusinessResearch
import com.example.data.remote.GeminiBusinessService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BusinessRepository(
    private val database: AppDatabase,
    private val remoteService: GeminiBusinessService = GeminiBusinessService()
) {
    private val dao = database.researchedBusinessDao()
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val jsonAdapter = moshi.adapter(BusinessResearch::class.java)

    val allResearched: Flow<List<ResearchedBusinessEntity>> = dao.getAllResearched()
    val bookmarkedBusinesses: Flow<List<ResearchedBusinessEntity>> = dao.getBookmarks()

    suspend fun researchBusiness(query: String): Pair<BusinessResearch, Long> = withContext(Dispatchers.IO) {
        val research = remoteService.researchBusiness(query)
        val json = jsonAdapter.toJson(research)

        // Check if already in database
        val existing = dao.getByName(research.companyName)
        val id = if (existing != null) {
            val updated = existing.copy(
                industry = research.industry,
                healthScore = research.financialHealth.healthScore,
                healthGrade = research.financialHealth.healthGrade,
                researchedAt = System.currentTimeMillis(),
                jsonData = json
            )
            dao.update(updated)
            existing.id
        } else {
            val entity = ResearchedBusinessEntity(
                companyName = research.companyName,
                industry = research.industry,
                healthScore = research.financialHealth.healthScore,
                healthGrade = research.financialHealth.healthGrade,
                researchedAt = System.currentTimeMillis(),
                isBookmarked = false,
                jsonData = json
            )
            dao.insert(entity)
        }

        Pair(research, id)
    }

    suspend fun getResearchById(id: Long): Pair<BusinessResearch, ResearchedBusinessEntity>? = withContext(Dispatchers.IO) {
        val entity = dao.getById(id) ?: return@withContext null
        val model = jsonAdapter.fromJson(entity.jsonData) ?: return@withContext null
        Pair(model, entity)
    }

    suspend fun toggleBookmark(id: Long, isCurrentlyBookmarked: Boolean) = withContext(Dispatchers.IO) {
        dao.updateBookmark(id, !isCurrentlyBookmarked)
    }

    suspend fun updatePdfUri(id: Long, pdfUri: String) = withContext(Dispatchers.IO) {
        dao.updatePdfUri(id, pdfUri)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    fun parseModelFromJson(json: String): BusinessResearch? {
        return try {
            jsonAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
