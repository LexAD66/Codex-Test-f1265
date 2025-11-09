package de.f_soft_studio.abookplayer.playback

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Prüft, dass der PlaybackEventBus Ereignisse in Reihenfolge ausliefert.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackEventBusTest {

    @Test
    fun `events werden im selben Takt empfangen`() = runTest {
        val bus = PlaybackEventBus()
        val received = mutableListOf<PlaybackManager.PlaybackProgress>()
        val job = launch { bus.events.collect { received += it } }
        val event = PlaybackManager.PlaybackProgress(bookId = 1L, chapterId = 2L, positionMs = 3_000L, durationMs = 10_000L)
        bus.publish(event)
        advanceUntilIdle()
        assertEquals(listOf(event), received)
        job.cancel()
    }
}
