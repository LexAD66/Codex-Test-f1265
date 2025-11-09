package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.LibraryRepository
import javax.inject.Inject

/**
 * Liefert ein Hörbuch inklusive Kapitel, damit Details- und Player-Screen den
 * gleichen Datenstand verwenden.
 */
class GetTocWithDurationsUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(bookId: Long) = repository.getBookWithChapters(bookId)
}
