package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.PlaybackRepository
import javax.inject.Inject

/**
 * Erstellt ein Lesezeichen mit Notiz.
 */
class AddBookmarkUseCase @Inject constructor(
    private val repository: PlaybackRepository
) {
    suspend operator fun invoke(bookId: Long, chapterId: Long, positionMs: Long, note: String, createdAt: Long): Long {
        return repository.addBookmark(bookId, chapterId, positionMs, note, createdAt)
    }
}
