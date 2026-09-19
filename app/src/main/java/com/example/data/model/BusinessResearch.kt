package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BusinessResearch(
    val companyName: String,
    val tickerOrStatus: String = "Private",
    val industry: String,
    val foundedYear: String,
    val headquarters: String,
    val ceo: String,
    val valuationOrMarketCap: String,
    val employeeCount: String,
    val businessModel: String,
    val missionStatement: String,
    val financialHealth: FinancialHealth,
    val competitiveLandscape: CompetitiveLandscape,
    val marketTrends: MarketTrends,
    val webDashboardPlan: WebDashboardPlan,
    val arenaPromptPlan: ArenaPromptPlan
)

@JsonClass(generateAdapter = true)
data class FinancialHealth(
    val healthScore: Int, // 0 to 100
    val healthGrade: String, // e.g. "A+", "A", "B+"
    val annualRevenue: String,
    val yoyGrowthRate: String,
    val grossMargin: String,
    val operatingMargin: String,
    val netMargin: String,
    val cashReserves: String,
    val burnRateOrRunway: String,
    val debtToEquityRatio: String,
    val currentRatio: String,
    val keyStrengths: List<String>,
    val riskFactors: List<String>
)

@JsonClass(generateAdapter = true)
data class CompetitiveLandscape(
    val competitors: List<CompetitorInfo>,
    val competitiveMoat: String,
    val marketPositionSummary: String,
    val swotAnalysis: SwotAnalysis
)

@JsonClass(generateAdapter = true)
data class CompetitorInfo(
    val name: String,
    val estimatedMarketShare: String,
    val keyDifferentiator: String,
    val pricingStrategy: String,
    val threatLevel: String // "High", "Moderate", "Low"
)

@JsonClass(generateAdapter = true)
data class SwotAnalysis(
    val strengths: List<String>,
    val weaknesses: List<String>,
    val opportunities: List<String>,
    val threats: List<String>
)

@JsonClass(generateAdapter = true)
data class MarketTrends(
    val trajectory: String, // e.g. "Strong Bullish Expansion", "Consistent Growth", etc.
    val growthVectors: List<String>,
    val recentMilestones: List<String>,
    val macroHeadwinds: List<String>,
    val macroTailwinds: List<String>,
    val investorSentimentScore: Int, // 0 to 100
    val consumerSentimentSummary: String
)

@JsonClass(generateAdapter = true)
data class WebDashboardPlan(
    val dashboardTitle: String,
    val strategicObjective: String,
    val targetStakeholders: List<String>,
    val kpiWidgets: List<KpiWidget>,
    val presentationNarrative: List<PresentationSlide>,
    val recommendedTechStack: List<String>
)

@JsonClass(generateAdapter = true)
data class KpiWidget(
    val title: String,
    val metricType: String, // "Revenue Velocity", "Customer Retention", etc.
    val chartType: String, // "Line Trend", "Bar Breakdown", "Gauge", "Cohort Matrix"
    val description: String,
    val targetFrequency: String // "Real-Time (WebSockets)", "Daily Sync", "Quarterly"
)

@JsonClass(generateAdapter = true)
data class PresentationSlide(
    val slideNumber: Int,
    val title: String,
    val keyTakeaway: String,
    val visualFocus: String
)

@JsonClass(generateAdapter = true)
data class ArenaPromptPlan(
    val masterPrompt: String,
    val arenaAgentCommand: String,
    val websiteArchitecture: List<String>,
    val designSystemSpecs: String,
    val keyConversionFeatures: List<String>,
    val targetAudience: String
)
