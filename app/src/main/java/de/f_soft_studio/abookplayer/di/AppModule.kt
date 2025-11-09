package de.f_soft_studio.abookplayer.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.f_soft_studio.abookplayer.data.db.AbookDatabase
import de.f_soft_studio.abookplayer.data.db.BookDao
import de.f_soft_studio.abookplayer.data.db.BookmarkDao
import de.f_soft_studio.abookplayer.data.db.ChapterDao
import de.f_soft_studio.abookplayer.data.db.PlaybackPositionDao
import javax.inject.Singleton

/**
 * Dient der Bereitstellung zentraler Datenbank-Abhängigkeiten.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AbookDatabase =
        AbookDatabase.build(context)

    @Provides
    fun provideBookDao(db: AbookDatabase): BookDao = db.bookDao()

    @Provides
    fun provideChapterDao(db: AbookDatabase): ChapterDao = db.chapterDao()

    @Provides
    fun provideBookmarkDao(db: AbookDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun providePlaybackPositionDao(db: AbookDatabase): PlaybackPositionDao = db.playbackPositionDao()
}
