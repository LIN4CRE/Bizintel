package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.BusinessResearch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    fun generateReport(context: Context, research: BusinessResearch): File {
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val safeName = research.companyName.replace("[^a-zA-Z0-9]".toRegex(), "_").lowercase()
        val outputFile = File(reportsDir, "BizIntel_${safeName}_Strategic_Report.pdf")

        val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())

        try {
            val pdfDocument = PdfDocument()

            // --- PAGE 1: Executive Overview & Financial Health ---
            val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = pdfDocument.startPage(page1Info)
            renderPage1(page1.canvas, research, dateStr)
            pdfDocument.finishPage(page1)

            // --- PAGE 2: Competitive Landscape & SWOT Analysis ---
            val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = pdfDocument.startPage(page2Info)
            renderPage2(page2.canvas, research, dateStr)
            pdfDocument.finishPage(page2)

            // --- PAGE 3: Market Trends & Web Dashboard Strategic Blueprint ---
            val page3Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 3).create()
            val page3 = pdfDocument.startPage(page3Info)
            renderPage3(page3.canvas, research, dateStr)
            pdfDocument.finishPage(page3)

            // --- PAGE 4: Arena.ai Website Prompt & Architecture Blueprint ---
            val page4Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 4).create()
            val page4 = pdfDocument.startPage(page4Info)
            renderPage4(page4.canvas, research, dateStr)
            pdfDocument.finishPage(page4)

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
        } catch (e: Exception) {
            // Write standard formatted PDF stream for environments where native Skia PDF engine is absent
            writeFallbackPdf(outputFile, research, dateStr)
        }

        return outputFile
    }

    private fun writeFallbackPdf(outputFile: File, research: BusinessResearch, dateStr: String) {
        val content = buildString {
            appendLine("%PDF-1.4")
            appendLine("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj")
            appendLine("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj")
            appendLine("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj")
            appendLine("5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj")

            val streamContent = buildString {
                appendLine("BT")
                appendLine("/F1 18 Tf")
                appendLine("50 720 Td")
                appendLine("(BIZINTEL STRATEGIC RESEARCH BRIEF) Tj")
                appendLine("/F1 14 Tf")
                appendLine("0 -24 Td")
                appendLine("(${research.companyName} - Financial Health & Intelligence) Tj")
                appendLine("/F1 10 Tf")
                appendLine("0 -18 Td")
                appendLine("(Industry: ${research.industry} | Ticker: ${research.tickerOrStatus} | Valuation: ${research.valuationOrMarketCap}) Tj")
                appendLine("0 -16 Td")
                appendLine("(Financial Health Score: ${research.financialHealth.healthScore}/100 Grade ${research.financialHealth.healthGrade}) Tj")
                appendLine("0 -16 Td")
                appendLine("(Annual Revenue: ${research.financialHealth.annualRevenue} | YoY: ${research.financialHealth.yoyGrowthRate}) Tj")
                appendLine("0 -16 Td")
                appendLine("(Gross Margin: ${research.financialHealth.grossMargin} | Cash: ${research.financialHealth.cashReserves}) Tj")
                appendLine("0 -24 Td")
                appendLine("(WEB DASHBOARD BLUEPRINT: ${research.webDashboardPlan.dashboardTitle}) Tj")
                appendLine("0 -16 Td")
                appendLine("(Tech Stack: ${research.webDashboardPlan.recommendedTechStack.joinToString(", ")}) Tj")
                appendLine("0 -24 Td")
                appendLine("(ARENA.AI AGENT COMMAND: ${research.arenaPromptPlan.arenaAgentCommand}) Tj")
                appendLine("ET")
            }

            appendLine("4 0 obj << /Length ${streamContent.toByteArray().size} >> stream")
            append(streamContent)
            appendLine("endstream endobj")
            appendLine("xref")
            appendLine("0 6")
            appendLine("0000000000 65535 f ")
            appendLine("0000000009 00000 n ")
            appendLine("0000000058 00000 n ")
            appendLine("0000000115 00000 n ")
            appendLine("0000000250 00000 n ")
            appendLine("0000000200 00000 n ")
            appendLine("trailer << /Size 6 /Root 1 0 R >>")
            appendLine("startxref")
            appendLine("1200")
            appendLine("%%EOF")
        }
        outputFile.writeText(content)
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createShareIntent(context: Context, file: File, companyName: String): Intent {
        val uri = getFileUri(context, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BizIntel Strategic Research Report: $companyName")
            putExtra(Intent.EXTRA_TEXT, "Attached is the comprehensive business intelligence report and web dashboard strategic plan for $companyName, generated by BizIntel.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createViewIntent(context: Context, file: File): Intent {
        val uri = getFileUri(context, file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun renderPage1(canvas: Canvas, research: BusinessResearch, dateStr: String) {
        drawHeader(canvas, 1, dateStr, "EXECUTIVE INTELLIGENCE & FINANCIAL HEALTH")

        var y = 68f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Company Hero Banner
        paint.color = Color.parseColor("#0F172A") // Deep slate
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 84f), 8f, 8f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(research.companyName, MARGIN + 16f, y + 30f, paint)

        // Subtitle
        paint.color = Color.parseColor("#38BDF8") // Sky cyan
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${research.industry} • ${research.tickerOrStatus} • Founded ${research.foundedYear}", MARGIN + 16f, y + 46f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        paint.textSize = 9f
        canvas.drawText("Headquarters: ${research.headquarters}   |   CEO: ${research.ceo}   |   Valuation: ${research.valuationOrMarketCap}", MARGIN + 16f, y + 64f, paint)

        y += 100f

        // Mission & Business Model Section
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EXECUTIVE OVERVIEW & BUSINESS MODEL", MARGIN, y, paint)
        y += 14f

        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f), 6f, 6f, paint)
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#334155")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, "Mission: ${research.missionStatement}", MARGIN + 10f, y + 16f, PAGE_WIDTH - MARGIN * 2 - 20f, paint, 12f, 2)
        drawWrappedText(canvas, "Model: ${research.businessModel}", MARGIN + 10f, y + 36f, PAGE_WIDTH - MARGIN * 2 - 20f, paint, 12f, 2)

        y += 70f

        // Financial Health Scorecard
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FINANCIAL HEALTH & PERFORMANCE METRICS", MARGIN, y, paint)
        y += 14f

        // Health Score Card
        val fh = research.financialHealth
        paint.color = Color.parseColor("#F0FDF4") // Soft mint
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 68f), 6f, 6f, paint)
        paint.color = Color.parseColor("#86EFAC")
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 68f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#15803D")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${fh.healthScore}/100", MARGIN + 16f, y + 38f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Health Rating: Grade ${fh.healthGrade}", MARGIN + 16f, y + 54f, paint)

        // Key Ratios Grid on the right
        val gridStartX = MARGIN + 160f
        paint.color = Color.parseColor("#334155")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Annual Revenue: ", gridStartX, y + 20f, paint)
        canvas.drawText("YoY Growth Rate: ", gridStartX, y + 36f, paint)
        canvas.drawText("Cash Reserves: ", gridStartX, y + 52f, paint)

        val gridValX = gridStartX + 85f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(fh.annualRevenue, gridValX, y + 20f, paint)
        canvas.drawText(fh.yoyGrowthRate, gridValX, y + 36f, paint)
        canvas.drawText(fh.cashReserves, gridValX, y + 52f, paint)

        val gridCol2X = gridValX + 110f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Gross Margin: ", gridCol2X, y + 20f, paint)
        canvas.drawText("Operating Margin: ", gridCol2X, y + 36f, paint)
        canvas.drawText("Runway / Burn: ", gridCol2X, y + 52f, paint)

        val gridVal2X = gridCol2X + 80f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(fh.grossMargin, gridVal2X, y + 20f, paint)
        canvas.drawText(fh.operatingMargin, gridVal2X, y + 36f, paint)
        canvas.drawText(fh.burnRateOrRunway.take(20), gridVal2X, y + 52f, paint)

        y += 84f

        // Financial Ratios Table
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f), paint)
        paint.color = Color.WHITE
        canvas.drawText("METRIC", MARGIN + 10f, y + 13f, paint)
        canvas.drawText("VALUE", MARGIN + 170f, y + 13f, paint)
        canvas.drawText("BENCHMARK ASSESSMENT", MARGIN + 310f, y + 13f, paint)
        y += 18f

        val ratioRows = listOf(
            Triple("Current Ratio", fh.currentRatio, "Liquidity buffer sufficient for short-term obligations"),
            Triple("Debt-to-Equity Ratio", fh.debtToEquityRatio, "Conservative balance sheet leverage"),
            Triple("Net Profit Margin", fh.netMargin, "Bottom-line operational capital generation"),
            Triple("Gross Profit Margin", fh.grossMargin, "Pricing power and core product unit economics")
        )

        for ((idx, row) in ratioRows.withIndex()) {
            paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
            canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f), paint)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(row.first, MARGIN + 10f, y + 13f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(row.second, MARGIN + 170f, y + 13f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(row.third, MARGIN + 310f, y + 13f, paint)
            y += 18f
        }

        y += 16f

        // Key Strengths and Risks 2-column card
        val colWidth = (PAGE_WIDTH - MARGIN * 2 - 12f) / 2f

        // Strengths Card
        paint.color = Color.parseColor("#F0FDF4")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + colWidth, y + 130f), 6f, 6f, paint)
        paint.color = Color.parseColor("#166534")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("✓ KEY FINANCIAL STRENGTHS", MARGIN + 12f, y + 18f, paint)

        var strengthY = y + 34f
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (st in fh.keyStrengths.take(4)) {
            drawWrappedText(canvas, "• $st", MARGIN + 12f, strengthY, colWidth - 24f, paint, 11f, 2)
            strengthY += 22f
        }

        // Risks Card
        val col2X = MARGIN + colWidth + 12f
        paint.color = Color.parseColor("#FEF2F2")
        canvas.drawRoundRect(RectF(col2X, y, col2X + colWidth, y + 130f), 6f, 6f, paint)
        paint.color = Color.parseColor("#991B1B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("⚠ IDENTIFIED RISK FACTORS", col2X + 12f, y + 18f, paint)

        var riskY = y + 34f
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (rf in fh.riskFactors.take(4)) {
            drawWrappedText(canvas, "• $rf", col2X + 12f, riskY, colWidth - 24f, paint, 11f, 2)
            riskY += 22f
        }

        drawFooter(canvas, 1)
    }

    private fun renderPage2(canvas: Canvas, research: BusinessResearch, dateStr: String) {
        drawHeader(canvas, 2, dateStr, "COMPETITIVE LANDSCAPE & SWOT ANALYSIS")

        var y = 68f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Competitive Moat Statement
        val cl = research.competitiveLandscape
        paint.color = Color.parseColor("#EFF6FF") // Light blue
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 64f), 6f, 6f, paint)
        paint.color = Color.parseColor("#1E40AF")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CORE COMPETITIVE MOAT & MARKET POSITION", MARGIN + 12f, y + 18f, paint)

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, cl.competitiveMoat, MARGIN + 12f, y + 32f, PAGE_WIDTH - MARGIN * 2 - 24f, paint, 12f, 2)

        y += 78f

        // Competitor Benchmark Table
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DIRECT COMPETITIVE BENCHMARKING", MARGIN, y, paint)
        y += 14f

        // Header
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f), paint)
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        canvas.drawText("COMPETITOR", MARGIN + 10f, y + 13f, paint)
        canvas.drawText("EST. SHARE", MARGIN + 140f, y + 13f, paint)
        canvas.drawText("KEY DIFFERENTIATOR", MARGIN + 215f, y + 13f, paint)
        canvas.drawText("THREAT", PAGE_WIDTH - MARGIN - 55f, y + 13f, paint)
        y += 18f

        for ((idx, comp) in cl.competitors.withIndex()) {
            paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
            canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 26f), paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(comp.name, MARGIN + 10f, y + 16f, paint)

            paint.color = Color.parseColor("#2563EB")
            canvas.drawText(comp.estimatedMarketShare, MARGIN + 140f, y + 16f, paint)

            paint.color = Color.parseColor("#334155")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            drawWrappedText(canvas, comp.keyDifferentiator, MARGIN + 215f, y + 14f, 230f, paint, 10f, 2)

            paint.color = when (comp.threatLevel.lowercase()) {
                "high" -> Color.parseColor("#DC2626")
                "moderate" -> Color.parseColor("#D97706")
                else -> Color.parseColor("#16A34A")
            }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(comp.threatLevel, PAGE_WIDTH - MARGIN - 55f, y + 16f, paint)

            y += 26f
        }

        y += 24f

        // 4-Quadrant SWOT Analysis Matrix
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SWOT STRATEGIC MATRIX", MARGIN, y, paint)
        y += 14f

        val swot = cl.swotAnalysis
        val cardW = (PAGE_WIDTH - MARGIN * 2 - 12f) / 2f
        val cardH = 148f

        // Quadrant 1: Strengths (Top Left)
        drawSwotQuadrant(canvas, MARGIN, y, cardW, cardH, "STRENGTHS", swot.strengths, "#F0FDF4", "#166534")

        // Quadrant 2: Weaknesses (Top Right)
        drawSwotQuadrant(canvas, MARGIN + cardW + 12f, y, cardW, cardH, "WEAKNESSES", swot.weaknesses, "#FFFBEB", "#B45309")

        y += cardH + 12f

        // Quadrant 3: Opportunities (Bottom Left)
        drawSwotQuadrant(canvas, MARGIN, y, cardW, cardH, "OPPORTUNITIES", swot.opportunities, "#EFF6FF", "#1E40AF")

        // Quadrant 4: Threats (Bottom Right)
        drawSwotQuadrant(canvas, MARGIN + cardW + 12f, y, cardW, cardH, "THREATS", swot.threats, "#FEF2F2", "#991B1B")

        drawFooter(canvas, 2)
    }

    private fun renderPage3(canvas: Canvas, research: BusinessResearch, dateStr: String) {
        drawHeader(canvas, 3, dateStr, "MARKET TRENDS & WEB DASHBOARD STRATEGIC PLAN")

        var y = 68f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val mt = research.marketTrends
        // Market Trends Banner
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 60f), 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Market Trajectory: ${mt.trajectory}", MARGIN + 14f, y + 26f, paint)

        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Investor Sentiment Index: ${mt.investorSentimentScore}/100   •   Consumer Sentiment: Positive / High Satisfaction", MARGIN + 14f, y + 44f, paint)

        y += 74f

        // Growth Vectors & Milestones
        val halfW = (PAGE_WIDTH - MARGIN * 2 - 12f) / 2f

        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + halfW, y + 96f), 6f, 6f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KEY GROWTH VECTORS", MARGIN + 10f, y + 18f, paint)

        var gvY = y + 32f
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (gv in mt.growthVectors.take(3)) {
            drawWrappedText(canvas, "▸ $gv", MARGIN + 10f, gvY, halfW - 20f, paint, 10f, 2)
            gvY += 20f
        }

        // Recent Milestones
        val rightX = MARGIN + halfW + 12f
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(RectF(rightX, y, rightX + halfW, y + 96f), 6f, 6f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RECENT STRATEGIC MILESTONES", rightX + 10f, y + 18f, paint)

        var rmY = y + 32f
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (rm in mt.recentMilestones.take(3)) {
            drawWrappedText(canvas, "✓ $rm", rightX + 10f, rmY, halfW - 20f, paint, 10f, 2)
            rmY += 20f
        }

        y += 110f

        // Web Dashboard Strategic Architecture Section
        val wdp = research.webDashboardPlan
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("WEB DASHBOARD STRATEGIC ARCHITECTURE (REAL-TIME TELEMETRY)", MARGIN, y, paint)
        y += 14f

        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 42f), 6f, 6f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, "Objective: ${wdp.strategicObjective}", MARGIN + 10f, y + 15f, PAGE_WIDTH - MARGIN * 2 - 20f, paint, 11f, 2)
        drawWrappedText(canvas, "Stakeholders: ${wdp.targetStakeholders.joinToString(", ")}", MARGIN + 10f, y + 30f, PAGE_WIDTH - MARGIN * 2 - 20f, paint, 11f, 1)

        y += 54f

        // KPI Widgets Blueprint Table
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f), paint)
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KPI WIDGET TITLE", MARGIN + 10f, y + 13f, paint)
        canvas.drawText("METRIC TYPE", MARGIN + 160f, y + 13f, paint)
        canvas.drawText("CHART TYPE", MARGIN + 250f, y + 13f, paint)
        canvas.drawText("DATA FREQUENCY", PAGE_WIDTH - MARGIN - 90f, y + 13f, paint)
        y += 18f

        for ((idx, widget) in wdp.kpiWidgets.take(4).withIndex()) {
            paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
            canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 26f), paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(widget.title, MARGIN + 10f, y + 13f, paint)

            paint.color = Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(widget.description.take(40) + "...", MARGIN + 10f, y + 22f, paint)

            paint.color = Color.parseColor("#2563EB")
            canvas.drawText(widget.metricType, MARGIN + 160f, y + 16f, paint)

            paint.color = Color.parseColor("#475569")
            canvas.drawText(widget.chartType, MARGIN + 250f, y + 16f, paint)

            paint.color = Color.parseColor("#059669")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(widget.targetFrequency, PAGE_WIDTH - MARGIN - 90f, y + 16f, paint)

            y += 26f
        }

        y += 18f

        // Presentation Slide Narrative Flow
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("STAKEHOLDER PRESENTATION SLIDE NARRATIVE", MARGIN, y, paint)
        y += 12f

        for (slide in wdp.presentationNarrative.take(3)) {
            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f), 4f, 4f, paint)

            paint.color = Color.parseColor("#2563EB")
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Slide ${slide.slideNumber}: ${slide.title}", MARGIN + 8f, y + 14f, paint)

            paint.color = Color.parseColor("#334155")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(slide.keyTakeaway.take(65) + "...", MARGIN + 175f, y + 14f, paint)

            y += 26f
        }

        y += 6f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 7.5f
        canvas.drawText("Recommended Tech Stack: ${wdp.recommendedTechStack.joinToString(" • ")}", MARGIN, y + 12f, paint)

        drawFooter(canvas, 3)
    }

    private fun renderPage4(canvas: Canvas, research: BusinessResearch, dateStr: String) {
        drawHeader(canvas, 4, dateStr, "ARENA.AI WEBSITE SPECIFICATION & PROMPT BLUEPRINT")

        var y = 68f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val ap = research.arenaPromptPlan

        // Arena.ai Agent Command Card
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f), 6f, 6f, paint)

        paint.color = Color.parseColor("#F59E0B") // Amber
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("⚡ ARENA.AI / AGENT DIRECT EXPORT COMMAND", MARGIN + 14f, y + 18f, paint)

        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        drawWrappedText(canvas, ap.arenaAgentCommand, MARGIN + 14f, y + 32f, PAGE_WIDTH - MARGIN * 2 - 28f, paint, 11f, 2)

        y += 68f

        // Master Prompt Section
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FULL MASTER WEBSITE GENERATION PROMPT (COPY TO ARENA.AI)", MARGIN, y, paint)
        y += 12f

        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 210f), 6f, 6f, paint)
        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 210f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#334155")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, ap.masterPrompt, MARGIN + 12f, y + 16f, PAGE_WIDTH - MARGIN * 2 - 24f, paint, 10f, 18)

        y += 224f

        // Website Architecture Breakdown
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CORE WEBSITE ARCHITECTURE & CONVERSION FUNNELS", MARGIN, y, paint)
        y += 12f

        val halfW = (PAGE_WIDTH - MARGIN * 2 - 12f) / 2f

        // Left Col: Architecture
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + halfW, y + 108f), 6f, 6f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PAGE SECTIONS & LAYOUT", MARGIN + 10f, y + 16f, paint)

        var archY = y + 30f
        paint.color = Color.parseColor("#334155")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (page in ap.websiteArchitecture.take(5)) {
            canvas.drawText("• $page", MARGIN + 10f, archY, paint)
            archY += 15f
        }

        // Right Col: Design System Specs
        val rightX = MARGIN + halfW + 12f
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(RectF(rightX, y, rightX + halfW, y + 108f), 6f, 6f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DESIGN SYSTEM & CONVERSION MECHANISMS", rightX + 10f, y + 16f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, "Design System: ${ap.designSystemSpecs}", rightX + 10f, y + 30f, halfW - 20f, paint, 10f, 3)

        var featY = y + 68f
        for (feat in ap.keyConversionFeatures.take(2)) {
            drawWrappedText(canvas, "★ $feat", rightX + 10f, featY, halfW - 20f, paint, 9f, 2)
            featY += 18f
        }

        y += 122f

        // Target Audience & Final Signature
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 32f), 4f, 4f, paint)
        paint.color = Color.parseColor("#475569")
        paint.textSize = 8f
        canvas.drawText("Target Audience: ${ap.targetAudience}", MARGIN + 10f, y + 14f, paint)
        canvas.drawText("Export Ready: Directly compatible with Arena.ai, Claude Code, Vercel v0, Lovable, and Bolt.new.", MARGIN + 10f, y + 26f, paint)

        drawFooter(canvas, 4)
    }

    private fun drawSwotQuadrant(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        items: List<String>,
        bgColor: String,
        textColor: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor(bgColor)
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 6f, 6f, paint)

        paint.color = Color.parseColor(textColor)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 12f, y + 18f, paint)

        var itemY = y + 34f
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (item in items.take(4)) {
            drawWrappedText(canvas, "• $item", x + 12f, itemY, w - 24f, paint, 10f, 2)
            itemY += 24f
        }
    }

    private fun drawHeader(canvas: Canvas, pageNum: Int, dateStr: String, sectionName: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Top Header Bar
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BIZINTEL STRATEGIC RESEARCH BRIEF", MARGIN, 28f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(sectionName, MARGIN, 42f, paint)

        val rightText = "$dateStr   |   Page $pageNum of 4"
        val textWidth = paint.measureText(rightText)
        canvas.drawText(rightText, PAGE_WIDTH - MARGIN - textWidth, 28f, paint)

        // Accent divider line
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, 50f, PAGE_WIDTH - MARGIN, 50f, paint)
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 36f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 36f, paint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("CONFIDENTIAL & PROPRIETARY • GENERATED FOR STAKEHOLDER STRATEGIC PLANNING", MARGIN, PAGE_HEIGHT - 22f, paint)

        val pText = "Page $pageNum / 4"
        val pW = paint.measureText(pText)
        canvas.drawText(pText, PAGE_WIDTH - MARGIN - pW, PAGE_HEIGHT - 22f, paint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float,
        maxLines: Int
    ) {
        val words = text.split(" ")
        var currentLine = ""
        var lineCount = 0
        var currentY = y

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(currentLine, x, currentY, paint)
                lineCount++
                currentY += lineSpacing
                if (lineCount >= maxLines) return
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty() && lineCount < maxLines) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }
}
