package com.gk.news_pro.page.utils.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gk.news_pro.MainActivity
import com.gk.news_pro.R
import com.gk.news_pro.data.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

class MediaPlaybackService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private val binder = MediaPlaybackBinder()
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState
    private var currentStation: RadioStation? = null
    private val TAG = "MediaPlaybackService"

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MediaPlaybackChannel"
        private const val ACTION_PLAY = "com.gk.news_pro.ACTION_PLAY"
        private const val ACTION_PAUSE = "com.gk.news_pro.ACTION_PAUSE"
        private const val ACTION_STOP = "com.gk.news_pro.ACTION_STOP"
    }

    inner class MediaPlaybackBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
            else -> {
                if (currentStation == null) {
                    val notification = createEmptyNotification()
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    private fun createEmptyNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radio App")
            .setContentText("Select a station to play")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Controls media playback"
                }
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel: ${e.message}", e)
        }
    }

    private fun updateNotification() {
        try {
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification updated")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification: ${e.message}", e)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PLAY }
        val pauseIntent = Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PAUSE }
        val stopIntent = Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_STOP }

        val playPendingIntent = PendingIntent.getService(
            this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pausePendingIntent = PendingIntent.getService(
            this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentStation?.name ?: "No Station")
            .setContentText(currentStation?.country ?: "Select a station to play")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                if (_playbackState.value == PlaybackState.Playing) {
                    NotificationCompat.Action(
                        android.R.drawable.ic_media_pause,
                        "Pause",
                        pausePendingIntent
                    )
                } else {
                    NotificationCompat.Action(
                        android.R.drawable.ic_media_play,
                        "Play",
                        playPendingIntent
                    )
                }
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    "Stop",
                    stopPendingIntent
                )
            )
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .build()
    }

    fun play(station: RadioStation) {
        try {
            Log.d(TAG, "Playing station: ${station.name}, URL: ${station.url}")

            if (station.url.isBlank()) {
                Log.e(TAG, "Station URL is blank, cannot play")
                _playbackState.value = PlaybackState.Error("Invalid station URL")
                return
            }

            if (station.url == currentStation?.url && _playbackState.value == PlaybackState.Playing) {
                Log.d(TAG, "Station already playing, ignoring request")
                return
            }

            // Clean up existing player if any
            cleanupPlayer()

            // Set current station before starting playback
            currentStation = station

            // Create and start a new foreground notification immediately to prevent ANR
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)

            // Configure DataSource and MediaSourceFactory
            val dataSourceFactory = DefaultDataSource.Factory(this)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            // Initialize player
            exoPlayer = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    try {
                        val mediaItem = MediaItem.fromUri(station.url)
                        setMediaItem(mediaItem)

                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                when (state) {
                                    Player.STATE_READY -> {
                                        try {
                                            play()
                                            _playbackState.value = PlaybackState.Playing
                                            updateNotification()
                                            Log.d(TAG, "Player is ready and playing")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error in STATE_READY: ${e.message}", e)
                                            _playbackState.value = PlaybackState.Error("Playback error: ${e.message}")
                                        }
                                    }
                                    Player.STATE_ENDED -> {
                                        Log.d(TAG, "Playback ended")
                                        _playbackState.value = PlaybackState.Idle
                                        stop()
                                    }
                                    Player.STATE_BUFFERING -> {
                                        Log.d(TAG, "Buffering...")
                                    }
                                    Player.STATE_IDLE -> {
                                        Log.d(TAG, "Player idle")
                                    }
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                Log.e(TAG, "Player error: ${error.message}", error)
                                val errorMessage = when {
                                    error.message?.contains("No suitable media source factory") == true -> {
                                        "This station's format is not supported. Try another station."
                                    }
                                    else -> error.message ?: "Unknown playback error"
                                }
                                _playbackState.value = PlaybackState.Error(errorMessage)
                                updateNotification()
                            }
                        })

                        prepare()
                        Log.d(TAG, "Player prepared")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error preparing player: ${e.message}", e)
                        _playbackState.value = PlaybackState.Error("Error preparing player: ${e.message}")
                        updateNotification()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing station: ${e.message}", e)
            _playbackState.value = PlaybackState.Error("Error playing station: ${e.message}")
        }
    }

    fun pause() {
        try {
            Log.d(TAG, "Pausing playback")
            exoPlayer?.pause()
            _playbackState.value = PlaybackState.Paused
            updateNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback: ${e.message}", e)
        }
    }

    fun resume() {
        try {
            Log.d(TAG, "Resuming playback")
            exoPlayer?.play()
            _playbackState.value = PlaybackState.Playing
            updateNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback: ${e.message}", e)
        }
    }

    private fun cleanupPlayer() {
        try {
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
            Log.d(TAG, "Player cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up player: ${e.message}", e)
        }
    }

    fun stop() {
        try {
            Log.d(TAG, "Stopping playback")
            cleanupPlayer()
            currentStation = null
            _playbackState.value = PlaybackState.Idle

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            Log.d(TAG, "Foreground service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service being destroyed")
        stop()
        super.onDestroy()
    }
}

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}