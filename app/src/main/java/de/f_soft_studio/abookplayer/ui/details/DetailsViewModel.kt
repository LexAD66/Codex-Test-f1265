package de.f_soft_studio.abookplayer.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.f_soft_studio.abookplayer.data.db.BookmarkEntity
import de.f_soft_studio.abookplayer.data.db.BookWithChapters
import de.f_soft_studio.abookplayer.domain.usecase.AddBookmarkUseCase
import de.f_soft_studio.abookplayer.domain.usecase.DeleteBookmarkUseCase
import de.f_soft_studio.abookplayer.domain.usecase.GetTocWithDurationsUseCase
import de.f_soft_studio.abookplayer.domain.usecase.ObserveBookmarksUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel für die Detailansicht eines Hörbuchs.
 */
@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTocUseCase: GetTocWithDurationsUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val observeBookmarksUseCase: ObserveBookmarksUseCase
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _book = MutableStateFlow<BookWithChapters?>(null)
    private val _bookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    private val _note = MutableStateFlow("")
    private val _positionMs = MutableStateFlow(0L)

    val uiState: StateFlow<DetailsUiState> = combine(
        _book,
        _bookmarks,
        _note,
        _positionMs
    ) { book, bookmarks, note, position ->
        DetailsUiState(
            book = book,
            bookmarks = bookmarks,
            noteDraft = note,
            positionDraft = position
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DetailsUiState())

    init {
        viewModelScope.launch {
            _book.value = getTocUseCase(bookId)
        }
        viewModelScope.launch {
            observeBookmarksUseCase(bookId).collect { bookmarks ->
                _bookmarks.value = bookmarks
            }
        }
    }

    fun updateNote(note: String) {
        _note.value = note
    }

    fun updatePosition(position: Long) {
        _positionMs.value = position
    }

    fun saveBookmark(chapterId: Long) {
        viewModelScope.launch {
            addBookmarkUseCase(bookId, chapterId, _positionMs.value, _note.value, System.currentTimeMillis())
            _note.value = ""
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            deleteBookmarkUseCase(bookmark)
        }
    }
}

/**
 * UI-Zustand des Detailscreens.
 */
data class DetailsUiState(
    val book: BookWithChapters? = null,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val noteDraft: String = "",
    val positionDraft: Long = 0L
)
