package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessResearch
import com.example.data.model.CompetitorInfo
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight

@Composable
fun CompetitorsTab(
    research: BusinessResearch,
    modifier: Modifier = Modifier
) {
    val cl = research.competitiveLandscape
    val swot = cl.swotAnalysis

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("competitors_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Competitive Moat Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Competitive Moat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORE COMPETITIVE MOAT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = cl.competitiveMoat,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Position: ${cl.marketPositionSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Direct Competitors
        Text(
            text = "Direct Competitor Benchmarking",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        cl.competitors.forEach { comp ->
            CompetitorCard(comp = comp)
        }

        // SWOT Matrix
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Strategic SWOT Analysis Matrix",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Quadrant 1 & 2: Strengths & Weaknesses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SwotCard(
                title = "STRENGTHS",
                items = swot.strengths,
                titleColor = EmeraldGreen,
                bgColor = EmeraldLight.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
            SwotCard(
                title = "WEAKNESSES",
                items = swot.weaknesses,
                titleColor = AmberWarning,
                bgColor = AmberLight.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
        }

        // Quadrant 3 & 4: Opportunities & Threats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SwotCard(
                title = "OPPORTUNITIES",
                items = swot.opportunities,
                titleColor = MaterialTheme.colorScheme.primary,
                bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                modifier = Modifier.weight(1f)
            )
            SwotCard(
                title = "THREATS",
                items = swot.threats,
                titleColor = CrimsonAlert,
                bgColor = CrimsonLight.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CompetitorCard(comp: CompetitorInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comp.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${comp.estimatedMarketShare} share",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Threat Level Badge
                val (threatColor, threatBg) = when (comp.threatLevel.lowercase()) {
                    "high" -> Pair(CrimsonAlert, CrimsonLight)
                    "moderate" -> Pair(AmberWarning, AmberLight)
                    else -> Pair(EmeraldGreen, EmeraldLight)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = threatBg
                ) {
                    Text(
                        text = "${comp.threatLevel} Threat",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = threatColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Differentiator: ${comp.keyDifferentiator}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pricing Strategy: ${comp.pricingStrategy}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SwotCard(
    title: String,
    items: List<String>,
    titleColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .background(bgColor)
                .padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}
