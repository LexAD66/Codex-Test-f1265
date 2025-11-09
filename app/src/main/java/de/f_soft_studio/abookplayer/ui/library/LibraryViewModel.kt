package de.f_soft_studio.abookplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.f_soft_studio.abookplayer.data.repo.LibraryRepository
import de.f_soft_studio.abookplayer.data.repo.ScannerRepository
import de.f_soft_studio.abookplayer.domain.usecase.GetLibraryFlowUseCase
import de.f_soft_studio.abookplayer.domain.usecase.ScanAbookFolderUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel für die Bibliothek. Hier ersetzen wir den veralteten Zugriff auf
 * `listAbooks` durch einen modernen Use-Case-Flow.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraryFlow: GetLibraryFlowUseCase,
    private val scanUseCase: ScanAbookFolderUseCase,
    private val scannerRepository: ScannerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(GetLibraryFlowUseCase.Filter.ALLE)
    private val _scanState = MutableStateFlow<ScannerRepository.ScanProgress>(ScannerRepository.ScanProgress.Idle)

    /**
     * UI-Zustand als StateFlow, damit Compose automatisch recomposed.
     */
    val uiState: StateFlow<LibraryUiState> = combine(
        _searchQuery,
        _filter,
        _scanState,
        _filter.flatMapLatest { filter -> getLibraryFlow(filter) }
    ) { query, filter, scan, books ->
        val normalized = query.trim().lowercase()
        val filteredBooks = if (normalized.isEmpty()) {
            books
        } else {
            books.filter { book ->
                book.book.title.lowercase().contains(normalized) ||
                    (book.book.author?.lowercase()?.contains(normalized) == true)
            }
        }
        LibraryUiState(
            books = filteredBooks,
            searchQuery = query,
            filter = filter,
            scanProgress = scan
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LibraryUiState())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: GetLibraryFlowUseCase.Filter) {
        _filter.value = filter
    }

    fun startScan() {
        viewModelScope.launch {
            scanUseCase().collect { progress ->
                _scanState.value = progress
            }
        }
    }

    init {
        // Begründung: Beim Start stellen wir sicher, dass der Ordner existiert.
        scannerRepository.ensureDefaultFolder()
    }
}

/**
 * Aggregierter Zustand der Bibliothek.
 */
data class LibraryUiState(
    val books: List<LibraryRepository.LibraryBook> = emptyList(),
    val searchQuery: String = "",
    val filter: GetLibraryFlowUseCase.Filter = GetLibraryFlowUseCase.Filter.ALLE,
    val scanProgress: ScannerRepository.ScanProgress = ScannerRepository.ScanProgress.Idle
)
