package com.gk.news_pro.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.page.utils.service.MediaPlaybackService
import com.gk.news_pro.page.utils.service.PlaybackState
import kotlinx.coroutines.flow.StateFlow

object MediaPlayerManager {
    private var service: MediaPlaybackService? = null
    private var isBound = false
    private var pendingStation: RadioStation? = null
    private var onPreparedCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private const val TAG = "MediaPlayerManager"

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as MediaPlaybackService.MediaPlaybackBinder
                this@MediaPlayerManager.service = binder.getService()
                isBound = true
                Log.d(TAG, "Service bound successfully")

                pendingStation?.let {
                    Log.d(TAG, "Playing pending station: ${it.name}")
                    this@MediaPlayerManager.service?.play(it)
                    onPreparedCallback?.invoke()
                    pendingStation = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to service: ${e.message}", e)
                onErrorCallback?.invoke("Failed to connect to playback service: ${e.message}")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            service = null
            Log.d(TAG, "Service disconnected")
        }
    }

    fun bindService(context: Context) {
        if (isBound) {
            Log.d(TAG, "Service already bound, skipping bind")
            return
        }

        try {
            val intent = Intent(context, MediaPlaybackService::class.java)
            // Start the service to keep it running even if the app is in background
            context.startService(intent)
            val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "Binding service, result: $bound")
            if (!bound) {
                Log.e(TAG, "Failed to bind service")
                onErrorCallback?.invoke("Failed to bind playback service")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding service: ${e.message}", e)
            onErrorCallback?.invoke("Error binding service: ${e.message}")
        }
    }

    fun unbindService(context: Context) {
        if (isBound) {
            try {
                context.unbindService(connection)
                isBound = false
                service = null
                Log.d(TAG, "Service unbound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service: ${e.message}", e)
            }
        }
    }

    fun initialize(
        context: Context,
        url: String,
        station: RadioStation,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (url.isBlank()) {
            Log.e(TAG, "Invalid URL for station: ${station.name}")
            onError("Invalid URL for station: ${station.name}")
            return
        }

        Log.d(TAG, "Initializing player for station: ${station.name} with URL: $url")
        onPreparedCallback = onPrepared
        onErrorCallback = onError

        try {
            if (!isBound) {
                Log.d(TAG, "Service not bound, binding and setting pending station")
                pendingStation = station
                bindService(context)
            } else {
                Log.d(TAG, "Service already bound, playing station directly")
                service?.play(station)
                onPrepared()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing playback: ${e.message}", e)
            onError("Error initializing playback: ${e.message}")
        }
    }

    fun pause() {
        try {
            Log.d(TAG, "Pausing playback")
            service?.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback: ${e.message}", e)
            onErrorCallback?.invoke("Error pausing playback")
        }
    }

    fun resume() {
        try {
            Log.d(TAG, "Resuming playback")
            service?.resume()
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback: ${e.message}", e)
            onErrorCallback?.invoke("Error resuming playback")
        }
    }

    fun stop() {
        try {
            Log.d(TAG, "Stopping playback")
            service?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback: ${e.message}", e)
        }
    }

    fun isPlaying(): Boolean {
        return try {
            service?.playbackState?.value == PlaybackState.Playing
        } catch (e: Exception) {
            Log.e(TAG, "Error checking playback state: ${e.message}", e)
            false
        }
    }

    fun getPlaybackState(): StateFlow<PlaybackState>? {
        return service?.playbackState
    }
}