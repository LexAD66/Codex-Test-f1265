package de.f_soft_studio.abookplayer.playback

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Zentraler Ereignisbus zwischen Service und UI. Wir verwenden einen
 * SharedFlow mit einem kleinen Buffer, damit kurzfristige Peaks nicht zu
 * Datenverlust führen.
 */
@Singleton
class PlaybackEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<PlaybackManager.PlaybackProgress>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<PlaybackManager.PlaybackProgress> = _events

    suspend fun publish(event: PlaybackManager.PlaybackProgress) {
        _events.emit(event)
    }
}
