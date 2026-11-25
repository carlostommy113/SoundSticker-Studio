package com.soundsticker.studio.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.io.File

class AudioPlayerHelper(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var targetLoops = 1
    private var currentLoopCount = 0

    var onProgressUpdate: ((currentMs: Int, totalMs: Int) -> Unit)? = null
    var onPlaybackFinished: (() -> Unit)? = null

    fun play(
        fileOrUri: Any,
        startMs: Int = 0,
        endMs: Int = -1,
        loopCount: Int = 1
    ) {
        stop()

        try {
            val player = MediaPlayer()
            when (fileOrUri) {
                is File -> player.setDataSource(fileOrUri.absolutePath)
                is Uri -> player.setDataSource(context, fileOrUri)
                is String -> player.setDataSource(fileOrUri)
                else -> return
            }

            player.prepare()
            val duration = if (endMs > 0 && endMs <= player.duration) endMs else player.duration
            targetLoops = loopCount
            currentLoopCount = 0

            player.seekTo(startMs)
            player.start()

            mediaPlayer = player

            progressRunnable = object : Runnable {
                override fun run() {
                    val p = mediaPlayer
                    if (p != null && p.isPlaying) {
                        val current = p.currentPosition
                        val effectiveTotal = if (endMs > 0) (endMs - startMs) else p.duration
                        val effectiveCurrent = (current - startMs).coerceAtLeast(0)
                        onProgressUpdate?.invoke(effectiveCurrent, effectiveTotal)

                        if (endMs > 0 && current >= endMs) {
                            currentLoopCount++
                            if (currentLoopCount < targetLoops) {
                                p.seekTo(startMs)
                            } else {
                                stop()
                                onPlaybackFinished?.invoke()
                                return
                            }
                        }
                        handler.postDelayed(this, 50)
                    }
                }
            }
            handler.post(progressRunnable!!)

            player.setOnCompletionListener {
                currentLoopCount++
                if (currentLoopCount < targetLoops) {
                    player.seekTo(startMs)
                    player.start()
                } else {
                    stop()
                    onPlaybackFinished?.invoke()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}
