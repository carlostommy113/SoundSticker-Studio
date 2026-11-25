package com.soundsticker.studio.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.min

object VideoProcessorHelper {

    private const val BUFFER_SIZE = 1024 * 1024
    private const val STICKER_SIZE = 512

    fun getVideoDurationMs(context: Context, videoUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            dur?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    fun extractFrameAtTime(context: Context, videoUri: Uri, timeMs: Long, destStickerFile: File): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            val frameBitmap = retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: return false

            val squareBitmap = cropToSquare(frameBitmap)
            val scaled = Bitmap.createScaledBitmap(squareBitmap, STICKER_SIZE, STICKER_SIZE, true)
            if (squareBitmap != frameBitmap) squareBitmap.recycle()
            if (frameBitmap != scaled) frameBitmap.recycle()

            // Compress WebP
            val stream = ByteArrayOutputStream()
            var quality = 90
            scaled.compress(Bitmap.CompressFormat.WEBP, quality, stream)
            while (stream.size() > 100 * 1024 && quality >= 20) {
                stream.reset()
                quality -= 10
                scaled.compress(Bitmap.CompressFormat.WEBP, quality, stream)
            }

            FileOutputStream(destStickerFile).use { out ->
                out.write(stream.toByteArray())
                out.flush()
            }
            scaled.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    fun trimVideoSegment(
        context: Context,
        inputUri: Uri,
        destFile: File,
        startMs: Long,
        endMs: Long,
        keepAudio: Boolean = true
    ): Boolean {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        return try {
            extractor.setDataSource(context, inputUri, null)
            val trackCount = extractor.trackCount
            val trackMap = mutableMapOf<Int, Int>()

            muxer = MediaMuxer(destFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                val isVideo = mime.startsWith("video/")
                val isAudio = mime.startsWith("audio/")

                if (isVideo || (isAudio && keepAudio)) {
                    extractor.selectTrack(i)
                    val muxerTrack = muxer.addTrack(format)
                    trackMap[i] = muxerTrack
                }
            }

            if (trackMap.isEmpty()) return false

            muxer.start()

            val startUs = startMs * 1000L
            val endUs = endMs * 1000L
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            val buffer = ByteBuffer.allocate(BUFFER_SIZE)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                val sampleTrackIndex = extractor.sampleTrackIndex
                if (sampleTrackIndex < 0) break

                val muxerTrack = trackMap[sampleTrackIndex]
                if (muxerTrack != null) {
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) break

                    val presentationTimeUs = extractor.sampleTime
                    if (presentationTimeUs > endUs) break

                    if (presentationTimeUs >= startUs) {
                        bufferInfo.presentationTimeUs = presentationTimeUs - startUs
                        bufferInfo.flags = extractor.sampleFlags
                        muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
                    }
                }
                extractor.advance()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { extractor.release() } catch (e: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (e: Exception) {}
        }
    }

    private fun cropToSquare(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val minDim = min(width, height)
        val cropX = (width - minDim) / 2
        val cropY = (height - minDim) / 2
        return Bitmap.createBitmap(src, cropX, cropY, minDim, minDim)
    }
}
