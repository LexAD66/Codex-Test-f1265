package de.f_soft_studio.abookplayer.data.repo

import de.f_soft_studio.abookplayer.data.db.BookDao
import de.f_soft_studio.abookplayer.data.db.BookEntity
import de.f_soft_studio.abookplayer.data.db.BookWithChapters
import de.f_soft_studio.abookplayer.data.db.ChapterDao
import de.f_soft_studio.abookplayer.data.db.ChapterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kapselt alle Zugriffe rund um die Bibliothek. Die Repository-Schicht sorgt
 * dafür, dass ViewModels mit stabilen Kotlin-Datentypen arbeiten können.
 */
@Singleton
class LibraryRepository @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao
) {

    /**
     * Modell, das die UI direkt verwenden kann. Wir speichern neben den
     * Basismeta­daten auch Ableitungen wie die Kapitellänge.
     */
    data class LibraryBook(
        val book: BookEntity,
        val chapters: List<ChapterSummary>
    ) {
        val totalDurationMs: Long = chapters.sumOf { it.durationMs }
    }

    /**
     * Zusammenfassung für Kapitel, damit die UI keine Room-Entity benötigt.
     */
    data class ChapterSummary(
        val id: Long,
        val title: String,
        val durationMs: Long,
        val filePath: String,
        val index: Int
    )

    private fun mapToLibraryBooks(books: List<BookEntity>, chapters: List<ChapterEntity>): List<LibraryBook> {
        val grouped = chapters.groupBy { it.bookId }
        return books.map { book ->
            LibraryBook(
                book = book,
                chapters = grouped[book.id].orEmpty().sortedBy { it.chapterIndex }.map { chapter ->
                    ChapterSummary(
                        id = chapter.id,
                        title = chapter.title,
                        durationMs = chapter.durationMs,
                        filePath = chapter.filePath,
                        index = chapter.chapterIndex
                    )
                }
            )
        }
    }

    fun observeLibrary(): Flow<List<LibraryBook>> = combine(
        bookDao.observeBooks(),
        chapterDao.observeAllChapters()
    ) { books, chapters -> mapToLibraryBooks(books, chapters) }

    fun observeRecentlyPlayed(): Flow<List<LibraryBook>> = combine(
        bookDao.observeRecentlyPlayed(),
        chapterDao.observeAllChapters()
    ) { books, chapters -> mapToLibraryBooks(books, chapters) }

    fun observeCompleted(): Flow<List<LibraryBook>> = combine(
        bookDao.observeCompleted(),
        chapterDao.observeAllChapters()
    ) { books, chapters -> mapToLibraryBooks(books, chapters) }

    suspend fun getBookWithChapters(bookId: Long): BookWithChapters? = bookDao.getBookWithChapters(bookId)

    suspend fun replaceChapters(bookId: Long, chapters: List<ChapterEntity>) {
        chapterDao.deleteForBook(bookId)
        chapterDao.upsertAll(chapters)
    }

    suspend fun updateCompletion(bookId: Long, isCompleted: Boolean, lastPlayedAt: Long?) {
        bookDao.updateCompletion(bookId, isCompleted, lastPlayedAt)
    }
}
