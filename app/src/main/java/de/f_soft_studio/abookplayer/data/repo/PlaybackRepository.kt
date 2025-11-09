package de.f_soft_studio.abookplayer.data.repo

import de.f_soft_studio.abookplayer.data.db.BookmarkDao
import de.f_soft_studio.abookplayer.data.db.BookmarkEntity
import de.f_soft_studio.abookplayer.data.db.PlaybackPositionDao
import de.f_soft_studio.abookplayer.data.db.PlaybackPositionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltungslogik für Wiedergabepositionen und Lesezeichen. Die Klasse ist der
 * Vermittler zwischen MediaSessionService und UI-Schicht.
 */
@Singleton
class PlaybackRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val playbackPositionDao: PlaybackPositionDao
) {

    /**
     * Speichert die aktuelle Position eines Kapitels.
     */
    suspend fun persistPosition(bookId: Long, chapterId: Long, positionMs: Long, timestamp: Long) {
        playbackPositionDao.upsert(
            PlaybackPositionEntity(
                chapterId = chapterId,
                bookId = bookId,
                positionMs = positionMs,
                updatedAt = timestamp
            )
        )
    }

    /**
     * Gibt den gespeicherten Fortschritt für ein Buch zurück.
     */
    fun observePositions(bookId: Long): Flow<List<PlaybackPositionEntity>> =
        playbackPositionDao.observeForBook(bookId)

    /**
     * Legt ein neues Lesezeichen an.
     */
    suspend fun addBookmark(
        bookId: Long,
        chapterId: Long,
        positionMs: Long,
        note: String,
        createdAt: Long
    ): Long = bookmarkDao.insert(
        BookmarkEntity(
            bookId = bookId,
            chapterId = chapterId,
            positionMs = positionMs,
            note = note,
            createdAt = createdAt
        )
    )

    /**
     * Entfernt ein bestehendes Lesezeichen.
     */
    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.delete(bookmark)
    }

    /**
     * Stream mit allen Lesezeichen eines Buches.
     */
    fun observeBookmarks(bookId: Long): Flow<List<BookmarkEntity>> =
        bookmarkDao.observeBookmarks(bookId)
}
