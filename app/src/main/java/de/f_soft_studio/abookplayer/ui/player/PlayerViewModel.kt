package de.f_soft_studio.abookplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.f_soft_studio.abookplayer.domain.usecase.SavePositionUseCase
import de.f_soft_studio.abookplayer.playback.PlaybackEventBus
import de.f_soft_studio.abookplayer.playback.PlaybackManager
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel für den Player. Hier laufen alle Steuersignale zusammen.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val eventBus: PlaybackEventBus,
    private val savePositionUseCase: SavePositionUseCase
) : ViewModel() {

    private val _speed = MutableStateFlow(1.0f)
    private val _sleepTimerMs = MutableStateFlow<Long?>(null)
    private val _position = MutableStateFlow(0L)
    private val _duration = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<PlayerUiState> = combine(
        _speed,
        _sleepTimerMs,
        _position,
        _duration
    ) { speed, sleep, position, duration ->
        PlayerUiState(
            isPlaying = playbackManager.mediaSession.player.isPlaying,
            speed = speed,
            sleepTimerRemainingMs = sleep,
            positionMs = position,
            durationMs = duration
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlayerUiState())

    init {
        viewModelScope.launch {
            playbackManager.sleepTimerRemaining.collect { remaining ->
                _sleepTimerMs.value = remaining
            }
        }
        viewModelScope.launch {
            eventBus.events.collect { progress ->
                _position.value = progress.positionMs
                _duration.value = progress.durationMs
                val bookId = progress.bookId
                val chapterId = progress.chapterId
                if (bookId != null && chapterId != null) {
                    savePositionUseCase(bookId, chapterId, progress.positionMs, System.currentTimeMillis())
                }
            }
        }
    }

    fun playPause() {
        val player = playbackManager.mediaSession.player
        if (player.isPlaying) playbackManager.pause() else playbackManager.play()
    }

    fun seekForward() = playbackManager.seekForwardBy(30_000)

    fun seekBackward() = playbackManager.seekBackwardBy(30_000)

    fun updateSpeed(newSpeed: Float) {
        _speed.value = newSpeed
        playbackManager.setPlaybackSpeed(newSpeed)
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    fun updateSleepTimer(durationMs: Long?) {
        playbackManager.scheduleSleepTimer(durationMs)
    }
}

/**
 * Zustand für den Player-Screen.
 */
data class PlayerUiState(
    val isPlaying: Boolean = false,
    val speed: Float = 1.0f,
    val sleepTimerRemainingMs: Long? = null,
    val positionMs: Long = 0L,
    val durationMs: Long? = null
)
