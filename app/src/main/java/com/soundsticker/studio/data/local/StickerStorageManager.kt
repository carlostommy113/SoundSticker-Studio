package com.soundsticker.studio.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.soundsticker.studio.data.model.StickerItem
import com.soundsticker.studio.data.model.StickerPack
import java.io.File
import java.io.FileOutputStream

class StickerStorageManager(private val context: Context) {
    private val gson = Gson()
    private val rootDir = File(context.filesDir, "sticker_packs").apply { mkdirs() }
    private val indexFile = File(rootDir, "packs_index.json")

    @Synchronized
    fun getAllPacks(): List<StickerPack> {
        if (!indexFile.exists()) {
            return emptyList()
        }
        return try {
            val json = indexFile.readText()
            val type = object : TypeToken<List<StickerPack>>() {}.type
            gson.fromJson<List<StickerPack>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @Synchronized
    fun getPack(identifier: String): StickerPack? {
        return getAllPacks().find { it.identifier == identifier }
    }

    @Synchronized
    fun savePack(pack: StickerPack) {
        val packs = getAllPacks().toMutableList()
        val index = packs.indexOfFirst { it.identifier == pack.identifier }
        if (index >= 0) {
            packs[index] = pack
        } else {
            packs.add(pack)
        }
        persistPacks(packs)
    }

    @Synchronized
    fun deletePack(identifier: String) {
        val packs = getAllPacks().filterNot { it.identifier == identifier }
        persistPacks(packs)
        val packDir = File(rootDir, identifier)
        if (packDir.exists()) {
            packDir.deleteRecursively()
        }
    }

    @Synchronized
    fun addStickerToPack(packIdentifier: String, sticker: StickerItem, packName: String = "SoundStickers"): StickerPack {
        val packDir = File(rootDir, packIdentifier).apply { mkdirs() }
        var pack = getPack(packIdentifier) ?: StickerPack(
            identifier = packIdentifier,
            name = packName,
            publisher = "SoundSticker Studio",
            trayImageFile = "tray.webp"
        )

        val updatedStickers = pack.stickers.toMutableList()
        updatedStickers.add(0, sticker)

        val updatedPack = pack.copy(stickers = updatedStickers)
        savePack(updatedPack)
        return updatedPack
    }

    @Synchronized
    fun getPackDir(identifier: String): File {
        return File(rootDir, identifier).apply { mkdirs() }
    }

    @Synchronized
    fun getStickerFile(packIdentifier: String, fileName: String): File {
        return File(getPackDir(packIdentifier), fileName)
    }

    private fun persistPacks(packs: List<StickerPack>) {
        val json = gson.toJson(packs)
        indexFile.writeText(json)
    }

    fun initDefaultSamplePackIfNeeded() {
        val packs = getAllPacks()
        if (packs.isNotEmpty()) return

        val defaultPackId = "default_sound_pack"
        val packDir = getPackDir(defaultPackId)

        // 1. Generate Tray Icon (96x96 WebP)
        val trayFile = File(packDir, "tray.webp")
        if (!trayFile.exists()) {
            val trayBitmap = createSampleBadgeBitmap(96, "SSS", 0xFF25D366.toInt())
            FileOutputStream(trayFile).use { out ->
                trayBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
            }
        }

        // WhatsApp requires at least 3 stickers per pack!
        val stickers = mutableListOf<StickerItem>()
        val sampleNames = listOf("sample_1.webp", "sample_2.webp", "sample_3.webp")
        val sampleTitles = listOf("🎵 VIBE CHECK", "🔥 BOOM!", "⭐ SOUND STICKER")
        val sampleColors = listOf(0xFF128C7E.toInt(), 0xFFE91E63.toInt(), 0xFF673AB7.toInt())

        for (i in sampleNames.indices) {
            val fileName = sampleNames[i]
            val stickerFile = File(packDir, fileName)
            if (!stickerFile.exists()) {
                val stickerBitmap = createSampleBadgeBitmap(512, sampleTitles[i], sampleColors[i])
                FileOutputStream(stickerFile).use { out ->
                    stickerBitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
                }
            }
            stickers.add(
                StickerItem(
                    imageFileName = fileName,
                    emojis = listOf("🎵", "🔥"),
                    accessibilityLabel = sampleTitles[i],
                    memeTopText = sampleTitles[i],
                    isAnimated = false
                )
            )
        }

        val defaultPack = StickerPack(
            identifier = defaultPackId,
            name = "Pack Oficial SoundStickers",
            publisher = "SoundSticker Studio",
            trayImageFile = "tray.webp",
            stickers = stickers
        )
        savePack(defaultPack)
    }

    private fun createSampleBadgeBitmap(size: Int, text: String, bgColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val cornerRadius = size * 0.18f
        canvas.drawRoundRect(RectF(size * 0.05f, size * 0.05f, size * 0.95f, size * 0.95f), cornerRadius, cornerRadius, bgPaint)

        // Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.03f
        }
        canvas.drawRoundRect(RectF(size * 0.05f, size * 0.05f, size * 0.95f, size * 0.95f), cornerRadius, cornerRadius, borderPaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = size * 0.12f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, size / 2f, textY, textPaint)

        return bitmap
    }
}
