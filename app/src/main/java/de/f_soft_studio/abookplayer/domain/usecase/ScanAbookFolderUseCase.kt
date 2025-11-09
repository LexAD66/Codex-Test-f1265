package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repo.ScannerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use-Case, der den Repository-Scan kapselt. Durch diese zusätzliche Schicht
 * können wir später Mock-Implementierungen in Tests einsetzen.
 */
class ScanAbookFolderUseCase @Inject constructor(
    private val repository: ScannerRepository
) {
    operator fun invoke(): Flow<ScannerRepository.ScanProgress> = repository.scanLibrary()
}
