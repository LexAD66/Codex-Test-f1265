package de.f_soft_studio.abookplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Zentrale Room-Datenbank. Wir setzen die Version bewusst auf 2, um eine erste
 * Migration zu dokumentieren. Diese Migration erweitert die Tabelle `books`
 * um den Zeitstempel der letzten Wiedergabe.
 */
@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        PlaybackPositionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AbookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun playbackPositionDao(): PlaybackPositionDao

    companion object {
        /**
         * Migration von Version 1 auf 2: Fügt die Spalte `last_played_at` hinzu.
         * Wir setzen einen DEFAULT-Wert auf NULL, damit bestehende Installationen
         * ohne Datenverlust aktualisiert werden.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE books ADD COLUMN last_played_at INTEGER"
                )
            }
        }

        /**
         * Baut die Datenbankinstanz auf.
         */
        fun build(context: Context): AbookDatabase = Room.databaseBuilder(
            context,
            AbookDatabase::class.java,
            "abook.db"
        ).addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
}
