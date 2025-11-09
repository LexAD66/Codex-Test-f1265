package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Liefert den Bibliotheksstrom je nach Filter.
 */
class GetLibraryFlowUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    enum class Filter { ALLE, ZULETZT, ABGESCHLOSSEN }

    operator fun invoke(filter: Filter): Flow<List<LibraryRepository.LibraryBook>> = when (filter) {
        Filter.ALLE -> repository.observeLibrary()
        Filter.ZULETZT -> repository.observeRecentlyPlayed()
        Filter.ABGESCHLOSSEN -> repository.observeCompleted()
    }
}
