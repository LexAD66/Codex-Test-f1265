package de.f_soft_studio.abookplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Zugriffsschicht für Kapitel. Wir liefern Flows, damit die Queue live
 * aktualisiert werden kann.
 */
@Dao
interface ChapterDao {

    @Query("SELECT * FROM chapters WHERE book_id = :bookId ORDER BY chapter_index")
    fun observeChapters(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters")
    fun observeAllChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE book_id = :bookId")
    suspend fun deleteForBook(bookId: Long)
}
