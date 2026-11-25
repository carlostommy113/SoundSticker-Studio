package com.soundsticker.studio.media

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.soundsticker.studio.provider.WhatsAppStickerContentProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object MediaStoreHelper {

    const val WHATSAPP_PACKAGE = "com.whatsapp"
    const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    const val ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"

    fun saveVideoKitToMovies(
        context: Context,
        sourceVideoFile: File,
        baseName: String = "SoundSticker"
    ): Uri? {
        val fileName = "${baseName}_${System.currentTimeMillis()}.mp4"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SoundStickers")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return null

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(sourceVideoFile).use { input ->
                        input.copyTo(out)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
                uri
            } else {
                @Suppress("DEPRECATION")
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "SoundStickers"
                ).apply { mkdirs() }
                val targetFile = File(moviesDir, fileName)
                FileInputStream(sourceVideoFile).use { input ->
                    FileOutputStream(targetFile).use { out ->
                        input.copyTo(out)
                    }
                }
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun savePhotoAndAudioKit(
        context: Context,
        imageFile: File,
        audioFile: File?,
        baseName: String = "SoundSticker"
    ): Pair<Uri?, Uri?> {
        val time = System.currentTimeMillis()
        var imageUri: Uri? = null
        var audioUri: Uri? = null

        try {
            // 1. Save Image
            val imgName = "${baseName}_${time}.webp"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val imgValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, imgName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/webp")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SoundStickers")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imgValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(imageFile).use { input -> input.copyTo(out) }
                    }
                    imgValues.clear()
                    imgValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, imgValues, null, null)
                    imageUri = uri
                }
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "SoundStickers"
                ).apply { mkdirs() }
                val targetFile = File(picturesDir, imgName)
                FileInputStream(imageFile).use { it.copyTo(FileOutputStream(targetFile)) }
                imageUri = Uri.fromFile(targetFile)
            }

            // 2. Save Audio if available
            if (audioFile != null && audioFile.exists()) {
                val audioName = "${baseName}_Audio_${time}.m4a"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val audValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, audioName)
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/SoundStickers")
                        put(MediaStore.Audio.Media.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            FileInputStream(audioFile).use { input -> input.copyTo(out) }
                        }
                        audValues.clear()
                        audValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, audValues, null, null)
                        audioUri = uri
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val musicDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        "SoundStickers"
                    ).apply { mkdirs() }
                    val targetFile = File(musicDir, audioName)
                    FileInputStream(audioFile).use { it.copyTo(FileOutputStream(targetFile)) }
                    audioUri = Uri.fromFile(targetFile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(imageUri, audioUri)
    }

    fun launchWhatsApp(context: Context, useBusiness: Boolean = false): Boolean {
        val targetPackage = if (useBusiness) WHATSAPP_BUSINESS_PACKAGE else WHATSAPP_PACKAGE
        val intent = context.packageManager.getLaunchIntentForPackage(targetPackage)
        return if (intent != null) {
            context.startActivity(intent)
            true
        } else {
            val fallbackIntent = context.packageManager.getLaunchIntentForPackage(WHATSAPP_PACKAGE)
            if (fallbackIntent != null) {
                context.startActivity(fallbackIntent)
                true
            } else {
                Toast.makeText(context, "WhatsApp no está instalado en este dispositivo", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    fun exportStickerPackToWhatsApp(
        activity: Activity,
        packIdentifier: String,
        packName: String,
        useBusiness: Boolean = false
    ): Boolean {
        val targetPackage = if (useBusiness) WHATSAPP_BUSINESS_PACKAGE else WHATSAPP_PACKAGE
        return try {
            val intent = Intent().apply {
                action = ACTION_ENABLE_STICKER_PACK
                putExtra(WhatsAppStickerContentProvider.EXTRA_STICKER_PACK_ID, packIdentifier)
                putExtra(WhatsAppStickerContentProvider.EXTRA_STICKER_PACK_AUTHORITY, WhatsAppStickerContentProvider.AUTHORITY)
                putExtra(WhatsAppStickerContentProvider.EXTRA_STICKER_PACK_NAME, packName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                `package` = targetPackage
            }
            activity.startActivityForResult(intent, 200)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val genericIntent = Intent().apply {
                    action = ACTION_ENABLE_STICKER_PACK
                    putExtra(WhatsAppStickerContentProvider.EXTRA_STICKER_PACK_ID, packIdentifier)
                    putExtra(WhatsAppStickerContentProvider.EXTRA_STICKER_PACK_AUTHORITY, WhatsAppStickerContentProvider.AUTHORITY)
                    putExtra(WhatsAppStickerContentProvider.EXTRA_STICKER_PACK_NAME, packName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivityForResult(genericIntent, 200)
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(activity, "Error al conectar con WhatsApp: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                false
            }
        }
    }
}
