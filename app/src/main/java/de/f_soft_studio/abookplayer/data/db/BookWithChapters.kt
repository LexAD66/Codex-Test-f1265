package de.f_soft_studio.abookplayer.data.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Kombiniert Buch und Kapitel, damit Repositories alle Informationen in einem
 * Datenzugriff laden können.
 */
data class BookWithChapters(
    @Embedded
    val book: BookEntity,
    @Relation(parentColumn = "id", entityColumn = "book_id")
    val chapters: List<ChapterEntity>
)
