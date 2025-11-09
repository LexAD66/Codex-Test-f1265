package de.f_soft_studio.abookplayer.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground-Service für die Hörbuchwiedergabe. Die umfangreiche Dokumentation
 * erklärt, wie wir Media3 und Benachrichtigungen kombinieren.
 */
@AndroidEntryPoint
class AbookPlaybackService : MediaSessionService() {

    @Inject lateinit var playbackManager: PlaybackManager
    @Inject lateinit var playbackEventBus: PlaybackEventBus

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var notificationManager: PlayerNotificationManager? = null

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent?.action) {
                playbackManager.pause()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(noisyReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(noisyReceiver, filter)
        }
        createNotificationChannel()
        setupNotification()
        serviceScope.launch {
            playbackManager.playbackUpdates.collect { progress ->
                playbackEventBus.publish(progress)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(noisyReceiver)
        notificationManager?.setPlayer(null)
        serviceScope.cancel()
        playbackManager.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = playbackManager.mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACT_PLAY_FILES) {
            val paths = intent.getStringArrayListExtra(EXTRA_FILE_PATHS).orEmpty()
            val queue = paths.mapNotNull { path ->
                val file = File(path)
                if (file.exists()) {
                    PlaybackManager.QueueItem(
                        bookId = null,
                        chapterId = null,
                        file = file,
                        title = file.nameWithoutExtension,
                        durationMs = null
                    )
                } else null
            }
            if (queue.isNotEmpty()) {
                playbackManager.setQueue(queue)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    private fun setupNotification() {
        val manager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
                    val title = player.mediaMetadata.title ?: MediaMetadata.Builder().setTitle(getString(de.f_soft_studio.abookplayer.R.string.app_name)).build().title
                    return title ?: getString(de.f_soft_studio.abookplayer.R.string.app_name)
                }

                override fun createCurrentContentIntent(player: androidx.media3.common.Player) =
                    playbackManager.mediaSession.sessionActivity

                override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? =
                    player.mediaMetadata.artist

                override fun getCurrentLargeIcon(player: androidx.media3.common.Player, callback: PlayerNotificationManager.BitmapCallback) = null
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: android.app.Notification, ongoing: Boolean) {
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopForeground(true)
                    stopSelf()
                }
            })
            .setSmallIconResourceId(de.f_soft_studio.abookplayer.R.drawable.ic_launcher_foreground)
            .build().apply {
                setUseChronometer(true)
                setPlayer(playbackManager.mediaSession.player)
            }
        notificationManager = manager
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(de.f_soft_studio.abookplayer.R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(de.f_soft_studio.abookplayer.R.string.notification_channel_description)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "abook_playback"
        private const val EXTRA_FILE_PATHS = "extra_file_paths"
        const val ACT_PLAY_FILES = "ACT_PLAY_FILES"

        fun enqueuePlayCommand(context: Context, files: List<File>) {
            val intent = Intent(context, AbookPlaybackService::class.java).apply {
                action = ACT_PLAY_FILES
                putStringArrayListExtra(EXTRA_FILE_PATHS, ArrayList(files.map { it.absolutePath }))
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
