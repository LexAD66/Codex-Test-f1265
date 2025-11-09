package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.PlaybackRepository
import javax.inject.Inject

/**
 * Persistiert den Fortschritt eines Kapitels.
 */
class SavePositionUseCase @Inject constructor(
    private val repository: PlaybackRepository
) {
    suspend operator fun invoke(bookId: Long, chapterId: Long, positionMs: Long, timestamp: Long) {
        repository.persistPosition(bookId, chapterId, positionMs, timestamp)
    }
}
