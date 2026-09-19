package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ResearchedBusinessEntity
import com.example.data.remote.GeminiBusinessService
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context matches BizIntel`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BizIntel", appName)
    }

    @Test
    fun `research intelligence service generates comprehensive profile`() {
        val service = GeminiBusinessService()
        val research = service.generateComprehensiveBusinessReport("Stripe")

        assertEquals("Stripe", research.companyName)
        assertTrue(research.financialHealth.healthScore >= 80)
        assertTrue(research.competitiveLandscape.competitors.isNotEmpty())
        assertTrue(research.competitiveLandscape.swotAnalysis.strengths.isNotEmpty())
        assertTrue(research.marketTrends.growthVectors.isNotEmpty())
        assertTrue(research.webDashboardPlan.kpiWidgets.isNotEmpty())
        assertTrue(research.arenaPromptPlan.masterPrompt.isNotBlank())
        assertTrue(research.arenaPromptPlan.arenaAgentCommand.startsWith("/agent"))
    }

    @Test
    fun `pdf generator produces non-empty report file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = GeminiBusinessService()
        val research = service.generateComprehensiveBusinessReport("Tesla")

        val pdfFile = PdfReportGenerator.generateReport(context, research)
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 1024) // PDF should be substantial
    }

    @Test
    fun `database inserts and queries researched businesses`() = runBlocking {
        val dao = db.researchedBusinessDao()
        val entity = ResearchedBusinessEntity(
            companyName = "Stripe",
            industry = "Fintech",
            healthScore = 91,
            healthGrade = "A+",
            jsonData = "{}"
        )
        val id = dao.insert(entity)
        assertTrue(id > 0)

        val retrieved = dao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Stripe", retrieved?.companyName)

        val list = dao.getAllResearched().first()
        assertEquals(1, list.size)
    }
}
