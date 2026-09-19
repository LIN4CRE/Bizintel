package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ResearchedBusinessEntity
import com.example.data.model.BusinessResearch
import com.example.data.repository.BusinessRepository
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class ResearchTab(val label: String) {
    OVERVIEW("Financials"),
    COMPETITION("Competitors & SWOT"),
    TRENDS("Market Trends"),
    DASHBOARD("Web Dashboard"),
    ARENA_PROMPTS("Arena.ai Prompts")
}

data class BusinessUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val loadingStep: String = "",
    val currentResearch: BusinessResearch? = null,
    val currentEntityId: Long? = null,
    val isBookmarked: Boolean = false,
    val historyList: List<ResearchedBusinessEntity> = emptyList(),
    val bookmarkedList: List<ResearchedBusinessEntity> = emptyList(),
    val activeTab: ResearchTab = ResearchTab.OVERVIEW,
    val isGeneratingPdf: Boolean = false,
    val generatedPdfFile: File? = null,
    val statusMessage: String? = null,
    val showHistoryDrawer: Boolean = false,
    val showExportDialog: Boolean = false
)

class BusinessResearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState: StateFlow<BusinessUiState> = _uiState.asStateFlow()

    init {
        // Collect history and bookmarks
        viewModelScope.launch {
            repository.allResearched.collect { list ->
                _uiState.update { it.copy(historyList = list) }
            }
        }
        viewModelScope.launch {
            repository.bookmarkedBusinesses.collect { bookmarks ->
                _uiState.update { it.copy(bookmarkedList = bookmarks) }
            }
        }

        // Auto-research an initial flagship business (e.g., Stripe) so user immediately lands on a rich dashboard!
        researchBusiness("Stripe")
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectTab(tab: ResearchTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleHistoryDrawer(show: Boolean) {
        _uiState.update { it.copy(showHistoryDrawer = show) }
    }

    fun toggleExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun researchBusiness(businessName: String) {
        val clean = businessName.trim()
        if (clean.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingStep = "Querying global commercial records for $clean...",
                    searchQuery = clean
                )
            }

            // Progressive step indication for rich user feedback
            launch {
                delay(600)
                if (_uiState.value.isLoading) {
                    _uiState.update { it.copy(loadingStep = "Analyzing financial disclosures, margins & cash runway...") }
                }
                delay(700)
                if (_uiState.value.isLoading) {
                    _uiState.update { it.copy(loadingStep = "Mapping competitive moat & SWOT quadrants...") }
                }
                delay(600)
                if (_uiState.value.isLoading) {
                    _uiState.update { it.copy(loadingStep = "Synthesizing web dashboard telemetry & Arena.ai prompts...") }
                }
            }

            try {
                val (research, id) = repository.researchBusiness(clean)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentResearch = research,
                        currentEntityId = id,
                        isBookmarked = false,
                        generatedPdfFile = null,
                        statusMessage = "Intelligence synthesized for ${research.companyName}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Error researching $clean: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun loadFromHistory(id: Long) {
        viewModelScope.launch {
            val result = repository.getResearchById(id)
            if (result != null) {
                val (model, entity) = result
                _uiState.update {
                    it.copy(
                        currentResearch = model,
                        currentEntityId = entity.id,
                        isBookmarked = entity.isBookmarked,
                        searchQuery = model.companyName,
                        showHistoryDrawer = false,
                        statusMessage = "Loaded ${model.companyName} from history"
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        val currentId = _uiState.value.currentEntityId ?: return
        val currentBookmarked = _uiState.value.isBookmarked
        viewModelScope.launch {
            repository.toggleBookmark(currentId, currentBookmarked)
            _uiState.update { it.copy(isBookmarked = !currentBookmarked) }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_uiState.value.currentEntityId == id) {
                _uiState.update {
                    it.copy(
                        currentEntityId = null,
                        isBookmarked = false
                    )
                }
            }
        }
    }

    fun generatePdf(context: Context, onShareReady: ((File) -> Unit)? = null) {
        val research = _uiState.value.currentResearch ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }
            try {
                val file = PdfReportGenerator.generateReport(context, research)
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        generatedPdfFile = file,
                        statusMessage = "PDF Report generated: ${file.name}"
                    )
                }
                onShareReady?.invoke(file)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        statusMessage = "Failed to create PDF: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun openArenaAi(context: Context, promptToPreload: String? = null) {
        if (!promptToPreload.isNullOrBlank()) {
            copyToClipboard(context, "Arena.ai Web Prompt", promptToPreload)
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://arena.ai"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open browser for Arena.ai", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
