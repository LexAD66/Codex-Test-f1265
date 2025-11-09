package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.db.BookmarkEntity
import de.f_soft_studio.abookplayer.data.repo.PlaybackRepository
import javax.inject.Inject

/**
 * Entfernt ein Lesezeichen dauerhaft.
 */
class DeleteBookmarkUseCase @Inject constructor(
    private val repository: PlaybackRepository
) {
    suspend operator fun invoke(bookmark: BookmarkEntity) {
        repository.removeBookmark(bookmark)
    }
}
