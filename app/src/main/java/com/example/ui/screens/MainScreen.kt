package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArenaPromptsTab
import com.example.ui.components.CompetitorsTab
import com.example.ui.components.FinancialOverviewTab
import com.example.ui.components.HistoryDrawer
import com.example.ui.components.MarketTrendsTab
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.ResearchHeader
import com.example.ui.components.WebDashboardTab
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.BusinessResearchViewModel
import com.example.ui.viewmodel.ResearchTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BusinessResearchViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var searchInput by remember { mutableStateOf(uiState.searchQuery) }

    // Sync search input if loaded from history
    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery != searchInput) {
            searchInput = uiState.searchQuery
        }
    }

    // Snackbar notifications
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearStatusMessage()
        }
    }

    val quickPicks = listOf("Stripe", "Tesla", "Airbnb", "Shopify", "NVIDIA", "Apple")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BI",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BizIntel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Market & Financial Intelligence",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleHistoryDrawer(true) },
                        modifier = Modifier.testTag("archive_history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.historyList.isNotEmpty()) {
                                    Badge { Text("${uiState.historyList.size}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History Archive"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("business_search_input"),
                        placeholder = { Text("Search any company (e.g. Stripe, Tesla, Acme Corp)...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchInput.isNotBlank()) {
                                IconButton(onClick = {
                                    searchInput = ""
                                    viewModel.onQueryChanged("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            if (searchInput.isNotBlank()) {
                                viewModel.researchBusiness(searchInput)
                            }
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Pick Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Suggestions:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        quickPicks.forEach { pick ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (uiState.currentResearch?.companyName?.equals(pick, ignoreCase = true) == true) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier
                                    .clickable {
                                        searchInput = pick
                                        focusManager.clearFocus()
                                        viewModel.researchBusiness(pick)
                                    }
                                    .testTag("quick_pick_$pick")
                            ) {
                                Text(
                                    text = pick,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.currentResearch?.companyName?.equals(pick, ignoreCase = true) == true) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("loading_view"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Synthesizing Business Intelligence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.loadingStep,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val research = uiState.currentResearch
                if (research != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header Card
                        ResearchHeader(
                            research = research,
                            isBookmarked = uiState.isBookmarked,
                            isGeneratingPdf = uiState.isGeneratingPdf,
                            onToggleBookmark = { viewModel.toggleBookmark() },
                            onGeneratePdf = {
                                viewModel.generatePdf(context) { file ->
                                    viewModel.toggleExportDialog(true)
                                }
                            },
                            onOpenArena = {
                                viewModel.selectTab(ResearchTab.ARENA_PROMPTS)
                            }
                        )

                        // Scrollable Tab Navigation
                        PrimaryScrollableTabRow(
                            selectedTabIndex = uiState.activeTab.ordinal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("research_tabs_row"),
                            edgePadding = 16.dp
                        ) {
                            ResearchTab.entries.forEach { tab ->
                                Tab(
                                    selected = uiState.activeTab == tab,
                                    onClick = { viewModel.selectTab(tab) },
                                    text = {
                                        Text(
                                            text = tab.label,
                                            fontWeight = if (uiState.activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                )
                            }
                        }

                        // Tab Content
                        Box(modifier = Modifier.weight(1f)) {
                            when (uiState.activeTab) {
                                ResearchTab.OVERVIEW -> FinancialOverviewTab(research = research)
                                ResearchTab.COMPETITION -> CompetitorsTab(research = research)
                                ResearchTab.TRENDS -> MarketTrendsTab(research = research)
                                ResearchTab.DASHBOARD -> WebDashboardTab(research = research)
                                ResearchTab.ARENA_PROMPTS -> ArenaPromptsTab(
                                    research = research,
                                    onCopyText = { label, text ->
                                        viewModel.copyToClipboard(context, label, text)
                                    },
                                    onOpenArena = { prompt ->
                                        viewModel.openArenaAi(context, prompt)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Empty / Prompt to Search
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Search Any Business",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter a company name above to generate financial health analysis, competitor SWOT matrices, market trends, web dashboard plans, and Arena.ai web building prompts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // History Sheet
    if (uiState.showHistoryDrawer) {
        HistoryDrawer(
            historyList = uiState.historyList,
            bookmarkedList = uiState.bookmarkedList,
            onSelectBusiness = { id -> viewModel.loadFromHistory(id) },
            onDeleteBusiness = { id -> viewModel.deleteHistoryItem(id) },
            onDismiss = { viewModel.toggleHistoryDrawer(false) }
        )
    }

    // PDF Export Dialog
    if (uiState.showExportDialog && uiState.generatedPdfFile != null) {
        PdfExportDialog(
            file = uiState.generatedPdfFile!!,
            companyName = uiState.currentResearch?.companyName ?: "Business",
            context = context,
            onDismiss = { viewModel.toggleExportDialog(false) }
        )
    }
}
