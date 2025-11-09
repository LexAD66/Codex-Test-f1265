package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.PlaybackRepository
import javax.inject.Inject

/**
 * Fluss der aktuellen Wiedergabepositionen.
 */
class ObservePositionsUseCase @Inject constructor(
    private val repository: PlaybackRepository
) {
    operator fun invoke(bookId: Long) = repository.observePositions(bookId)
}
