package de.f_soft_studio.abookplayer.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Verwaltet den Media3-Player und abstrahiert häufige Operationen.
 * Der Manager erzeugt MediaItems aus lokalen Dateien, kümmert sich um
 * Schlaf-Timer und Geschwindigkeitswechsel und meldet Fortschritte als Flow.
 */
@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext context: Context
) {
    /**
     * Repräsentiert ein Element in der Wiedergabeliste.
     */
    data class QueueItem(
        val bookId: Long?,
        val chapterId: Long?,
        val file: File,
        val title: String,
        val durationMs: Long?
    )

    /** Fortschritts-Event für die Persistierung. */
    data class PlaybackProgress(
        val bookId: Long?,
        val chapterId: Long?,
        val positionMs: Long,
        val durationMs: Long?
    )

    private val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
        .setUsage(androidx.media3.common.C.USAGE_MEDIA)
        .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_SPEECH)
        .build()

    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(androidx.media3.common.C.WAKE_MODE_LOCAL)
        .build()

    val mediaSession: MediaSession = MediaSession.Builder(context, player).build()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining

    val playbackUpdates = MutableSharedFlow<PlaybackProgress>(extraBufferCapacity = 32)

    private var sleepTimerJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                serviceScope.launch {
                    playbackUpdates.emit(
                        PlaybackProgress(
                            bookId = null,
                            chapterId = null,
                            positionMs = player.currentPosition,
                            durationMs = player.duration.takeIf { it > 0 }
                        )
                    )
                }
            }
        })
        serviceScope.launch {
            while (isActive) {
                val currentMediaItem = player.currentMediaItem
                playbackUpdates.emit(
                    PlaybackProgress(
                        bookId = currentMediaItem?.requestMetadata?.extras?.getLongOrNull(KEY_BOOK_ID),
                        chapterId = currentMediaItem?.mediaId?.toLongOrNull(),
                        positionMs = player.currentPosition,
                        durationMs = player.duration.takeIf { it > 0 }
                    )
                )
                delay(1_000)
            }
        }
    }

    fun setQueue(items: List<QueueItem>, startIndex: Int = 0) {
        val mediaItems = items.map { item ->
            MediaItem.Builder()
                .setUri(Uri.fromFile(item.file))
                .setMediaId(item.chapterId?.toString() ?: item.file.absolutePath)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setSubtitle(item.file.name)
                        .build()
                )
                .setRequestMetadata(
                    MediaItem.RequestMetadata.Builder()
                        .setExtras(android.os.Bundle().apply {
                            item.bookId?.let { putLong(KEY_BOOK_ID, it) }
                        })
                        .build()
                )
                .build()
        }
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true
    }

    fun play() = player.play()

    fun pause() = player.pause()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun seekForwardBy(millis: Long) {
        player.seekTo(player.currentPosition + millis)
    }

    fun seekBackwardBy(millis: Long) {
        player.seekTo((player.currentPosition - millis).coerceAtLeast(0))
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun scheduleSleepTimer(durationMs: Long?) {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = durationMs
        if (durationMs == null) return
        sleepTimerJob = serviceScope.launch {
            var remaining = durationMs
            while (remaining > 0 && isActive) {
                delay(1_000)
                remaining -= 1_000
                _sleepTimerRemaining.value = remaining
            }
            if (isActive) {
                pause()
                _sleepTimerRemaining.value = null
            }
        }
    }

    fun clearSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = null
    }

    fun release() {
        sleepTimerJob?.cancel()
        serviceScope.cancel()
        mediaSession.release()
        player.release()
    }

    companion object {
        private const val KEY_BOOK_ID = "book_id"
    }
}

private fun android.os.Bundle.getLongOrNull(key: String): Long? = if (containsKey(key)) getLong(key) else null
