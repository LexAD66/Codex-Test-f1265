package de.f_soft_studio.abookplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data-Access-Object für Bücher. Die Methoden liefern Flows, damit die UI
 * sofort auf Änderungen reagieren kann.
 */
@Dao
interface BookDao {

    /**
     * Liefert alle Bücher alphabetisch sortiert als kontinuierlichen Datenstrom.
     */
    @Query("SELECT * FROM books ORDER BY title")
    fun observeBooks(): Flow<List<BookEntity>>

    /**
     * Filtert nach dem Flag "Zuletzt gespielt".
     */
    @Query("SELECT * FROM books WHERE last_played_at IS NOT NULL ORDER BY last_played_at DESC")
    fun observeRecentlyPlayed(): Flow<List<BookEntity>>

    /**
     * Liefert abgeschlossene Bücher.
     */
    @Query("SELECT * FROM books WHERE is_completed = 1 ORDER BY title")
    fun observeCompleted(): Flow<List<BookEntity>>

    /**
     * Speichert oder aktualisiert ein Buch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: BookEntity): Long

    /**
     * Aktualisiert gezielt den Abschlussstatus und den Zeitstempel.
     */
    @Query("UPDATE books SET is_completed = :isCompleted, last_played_at = :lastPlayedAt WHERE id = :bookId")
    suspend fun updateCompletion(bookId: Long, isCompleted: Boolean, lastPlayedAt: Long?)

    /**
     * Löscht den kompletten Bestand – nur für Dev/Tests.
     */
    @Query("DELETE FROM books")
    suspend fun clear()

    /**
     * Hilfsfunktion, um Buch mit Kapiteln zu laden.
     */
    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithChapters(bookId: Long): BookWithChapters?
}
