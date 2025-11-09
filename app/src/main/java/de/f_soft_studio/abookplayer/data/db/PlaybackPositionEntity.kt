package de.f_soft_studio.abookplayer.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Speichert die letzte bekannte Wiedergabeposition pro Kapitel.
 * Diese Tabelle dient als Brücke zwischen dem Foreground-Service und der UI.
 */
@Entity(
    tableName = "playback_positions",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapter_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaybackPositionEntity(
    @PrimaryKey
    @ColumnInfo(name = "chapter_id")
    val chapterId: Long,
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    @ColumnInfo(name = "position_ms")
    val positionMs: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
