package de.f_soft_studio.abookplayer.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Repräsentiert ein Hörbuch in der lokalen Datenbank.
 * Jede Zeile entspricht einem physisch vorhandenen Hörbuchordner oder einer
 * .abook-Datei. Wir speichern bewusst viele Metadaten, um die Bibliothek ohne
 * aufwändige Dateisystem-Scans darstellen zu können.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "author")
    val author: String?,
    @ColumnInfo(name = "series")
    val series: String?,
    @ColumnInfo(name = "total_duration_ms")
    val totalDurationMs: Long,
    @ColumnInfo(name = "cover_uri")
    val coverUri: String?,
    @ColumnInfo(name = "root_path")
    val rootPath: String,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "last_played_at")
    val lastPlayedAt: Long?
)
