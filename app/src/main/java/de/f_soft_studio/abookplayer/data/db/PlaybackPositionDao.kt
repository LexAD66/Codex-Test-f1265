package de.f_soft_studio.abookplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO für gespeicherte Wiedergabepositionen.
 */
@Dao
interface PlaybackPositionDao {

    @Query("SELECT * FROM playback_positions WHERE chapter_id = :chapterId")
    suspend fun getPosition(chapterId: Long): PlaybackPositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlaybackPositionEntity)

    @Query("SELECT * FROM playback_positions WHERE book_id = :bookId")
    fun observeForBook(bookId: Long): Flow<List<PlaybackPositionEntity>>
}
