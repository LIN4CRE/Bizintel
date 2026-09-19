package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "researched_businesses")
data class ResearchedBusinessEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val industry: String,
    val healthScore: Int,
    val healthGrade: String,
    val researchedAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val jsonData: String,
    val pdfUri: String? = null
)
