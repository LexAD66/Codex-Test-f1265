package de.f_soft_studio.abookplayer.data.repo

import android.net.Uri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

/**
 * Der Scanner sucht nach Hörbüchern im Standardverzeichnis /storage/emulated/0/ABook/.
 * Wir nutzen einen Flow, damit die UI Fortschritte darstellen kann.
 */
@Singleton
class ScannerRepository @Inject constructor() {

    sealed interface ScanProgress {
        data object Idle : ScanProgress
        data class Running(
            val currentIndex: Int,
            val total: Int,
            val currentFile: File
        ) : ScanProgress
        data class Finished(val books: List<ScannedBook>) : ScanProgress
        data class Failed(val message: String, val throwable: Throwable?) : ScanProgress
    }

    data class ScannedBook(
        val title: String,
        val root: File,
        val audioFiles: List<File>,
        val cover: Uri?
    )

    private val defaultFolder: File = File("/storage/emulated/0/ABook")

    fun ensureDefaultFolder(): Result<File> = runCatching {
        if (!defaultFolder.exists()) {
            defaultFolder.mkdirs()
        }
        defaultFolder
    }

    fun scanLibrary(): Flow<ScanProgress> = callbackFlow {
        val folderResult = ensureDefaultFolder()
        if (folderResult.isFailure) {
            trySend(ScanProgress.Failed("Import fehlgeschlagen", folderResult.exceptionOrNull()))
            close(folderResult.exceptionOrNull())
            return@callbackFlow
        }
        val folder = folderResult.getOrThrow()
        val files = folder.listFiles()?.filter { it.isDirectory || it.extension.equals("abook", ignoreCase = true) }
            ?.sortedBy { it.name.lowercase(Locale.getDefault()) }
            ?: emptyList()
        if (files.isEmpty()) {
            trySend(ScanProgress.Finished(emptyList()))
            close()
            return@callbackFlow
        }
        val foundBooks = mutableListOf<ScannedBook>()
        files.forEachIndexed { index, candidate ->
            trySend(ScanProgress.Running(index + 1, files.size, candidate))
            val audioFiles = collectAudioFiles(candidate)
            if (audioFiles.isNotEmpty()) {
                foundBooks += ScannedBook(
                    title = candidate.nameWithoutExtension,
                    root = candidate,
                    audioFiles = audioFiles,
                    cover = findCover(candidate)
                )
            }
        }
        trySend(ScanProgress.Finished(foundBooks))
        close()
        awaitClose { }
    }.onStart { emit(ScanProgress.Idle) }
        .flowOn(Dispatchers.IO)

    private fun collectAudioFiles(file: File): List<File> {
        val candidates = if (file.isDirectory) {
            file.walkTopDown().filter { it.isFile }.toList()
        } else {
            listOf(file)
        }
        return candidates.filter { it.extension.lowercase(Locale.getDefault()) in SUPPORTED_AUDIO }
            .sortedBy { it.name }
    }

    private fun findCover(root: File): Uri? {
        val coverFile = root.listFiles()?.firstOrNull { possible ->
            possible.isFile && possible.nameWithoutExtension.equals("cover", ignoreCase = true)
        }
        return coverFile?.let { Uri.fromFile(it) }
    }

    companion object {
        private val SUPPORTED_AUDIO = setOf("mp3", "m4a", "aac", "ogg", "flac", "wav")
    }
}
