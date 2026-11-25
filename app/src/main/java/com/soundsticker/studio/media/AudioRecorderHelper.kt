package com.soundsticker.studio.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: File? = null
    private val handler = Handler(Looper.getMainLooper())
    private var amplitudeRunnable: Runnable? = null
    private var autoStopRunnable: Runnable? = null

    var onAmplitudeUpdate: ((Int) -> Unit)? = null
    var onMaxDurationReached: (() -> Unit)? = null

    fun startRecording(maxDurationMs: Long = 10_000L): File? {
        if (isRecording) return outputFile

        try {
            outputFile = File(context.cacheDir, "recorded_audio_${System.currentTimeMillis()}.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true

            // Amplitude poll loop (every 100ms)
            amplitudeRunnable = object : Runnable {
                override fun run() {
                    if (isRecording && mediaRecorder != null) {
                        try {
                            val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                            onAmplitudeUpdate?.invoke(maxAmp)
                        } catch (e: Exception) {
                            // Ignored
                        }
                        handler.postDelayed(this, 100)
                    }
                }
            }
            handler.post(amplitudeRunnable!!)

            // Auto-stop at max duration (default 10s)
            autoStopRunnable = Runnable {
                if (isRecording) {
                    stopRecording()
                    onMaxDurationReached?.invoke()
                }
            }
            handler.postDelayed(autoStopRunnable!!, maxDurationMs)

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            return null
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return outputFile

        try {
            amplitudeRunnable?.let { handler.removeCallbacks(it) }
            autoStopRunnable?.let { handler.removeCallbacks(it) }
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
        }
        return outputFile
    }

    fun isRecording(): Boolean = isRecording
}
