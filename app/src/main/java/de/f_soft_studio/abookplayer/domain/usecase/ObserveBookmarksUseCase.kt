package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.PlaybackRepository
import javax.inject.Inject

/**
 * Beobachtet alle Lesezeichen eines Buches als Flow.
 */
class ObserveBookmarksUseCase @Inject constructor(
    private val repository: PlaybackRepository
) {
    operator fun invoke(bookId: Long) = repository.observeBookmarks(bookId)
}
