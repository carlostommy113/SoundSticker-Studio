package com.soundsticker.studio.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ImageMemeComposer {

    private const val STICKER_SIZE = 512
    private const val TRAY_SIZE = 96
    private const val MAX_STICKER_BYTES = 100 * 1024 // 100 KB max WhatsApp
    private const val MAX_TRAY_BYTES = 50 * 1024    // 50 KB max WhatsApp tray

    fun processImageWithMemeText(
        context: Context,
        imageUri: Uri,
        topText: String?,
        bottomText: String?,
        destStickerFile: File,
        destTrayFile: File? = null
    ): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return false
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return false

            // 1. Recorte a cuadrado 1:1 y redimensionar a 512x512
            val squareBitmap = cropToSquare(originalBitmap)
            val scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, STICKER_SIZE, STICKER_SIZE, true)
            if (squareBitmap != originalBitmap) squareBitmap.recycle()
            if (originalBitmap != scaledBitmap) originalBitmap.recycle()

            // 2. Aplicar texto Meme si existe
            val finalBitmap = if (!topText.isNullOrBlank() || !bottomText.isNullOrBlank()) {
                drawMemeText(scaledBitmap, topText, bottomText)
            } else {
                scaledBitmap
            }

            // 3. Comprimir a WebP menor a 100 KB
            compressUnderLimit(finalBitmap, destStickerFile, MAX_STICKER_BYTES)

            // 4. Crear Tray Icon (96x96 WebP menor a 50 KB)
            if (destTrayFile != null) {
                val trayBitmap = Bitmap.createScaledBitmap(finalBitmap, TRAY_SIZE, TRAY_SIZE, true)
                compressUnderLimit(trayBitmap, destTrayFile, MAX_TRAY_BYTES)
                if (trayBitmap != finalBitmap) trayBitmap.recycle()
            }

            if (finalBitmap != scaledBitmap) scaledBitmap.recycle()
            finalBitmap.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun cropToSquare(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val newDimension = min(width, height)
        val cropX = (width - newDimension) / 2
        val cropY = (height - newDimension) / 2
        return Bitmap.createBitmap(src, cropX, cropY, newDimension, newDimension)
    }

    private fun drawMemeText(base: Bitmap, topText: String?, bottomText: String?): Bitmap {
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val textSize = STICKER_SIZE * 0.085f
        val strokeWidth = textSize * 0.16f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.FILL
        }

        val centerX = STICKER_SIZE / 2f

        if (!topText.isNullOrBlank()) {
            val lines = topText.uppercase().split("\n")
            var y = textSize + 24f
            for (line in lines) {
                canvas.drawText(line, centerX, y, strokePaint)
                canvas.drawText(line, centerX, y, fillPaint)
                y += textSize + 8f
            }
        }

        if (!bottomText.isNullOrBlank()) {
            val lines = bottomText.uppercase().split("\n")
            var y = STICKER_SIZE - 32f - ((lines.size - 1) * (textSize + 8f))
            for (line in lines) {
                canvas.drawText(line, centerX, y, strokePaint)
                canvas.drawText(line, centerX, y, fillPaint)
                y += textSize + 8f
            }
        }

        return result
    }

    private fun compressUnderLimit(bitmap: Bitmap, destFile: File, maxBytes: Int) {
        var quality = 90
        var compressed: ByteArray
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(format, quality, stream)
            compressed = stream.toByteArray()
            quality -= 10
        } while (compressed.size > maxBytes && quality >= 20)

        FileOutputStream(destFile).use { out ->
            out.write(compressed)
            out.flush()
        }
    }
}
