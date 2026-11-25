package com.soundsticker.studio.media

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

object AudioTrimmerHelper {

    private const val BUFFER_SIZE = 1024 * 1024

    fun getDurationMs(context: Context, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    fun trimAudio(
        context: Context,
        inputUri: Uri,
        destFile: File,
        startMs: Long,
        endMs: Long
    ): Boolean {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        return try {
            extractor.setDataSource(context, inputUri, null)
            val trackIndex = selectAudioTrack(extractor)
            if (trackIndex < 0) return false

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)

            muxer = MediaMuxer(destFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(format)
            muxer.start()

            val startUs = startMs * 1000L
            val endUs = endMs * 1000L

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            val buffer = ByteBuffer.allocate(BUFFER_SIZE)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) {
                    break
                }

                val presentationTimeUs = extractor.sampleTime
                if (presentationTimeUs > endUs) {
                    break
                }

                if (presentationTimeUs >= startUs) {
                    bufferInfo.presentationTimeUs = presentationTimeUs - startUs
                    bufferInfo.flags = extractor.sampleFlags
                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                }

                extractor.advance()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                extractor.release()
            } catch (e: Exception) {
                // Ignored
            }
            try {
                muxer?.stop()
                muxer?.release()
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                return i
            }
        }
        return -1
    }
}
