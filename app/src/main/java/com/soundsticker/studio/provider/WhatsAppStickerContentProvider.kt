package com.soundsticker.studio.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.soundsticker.studio.data.local.StickerStorageManager
import com.soundsticker.studio.data.model.StickerPack
import java.io.File
import java.io.FileNotFoundException

class WhatsAppStickerContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.soundsticker.studio.stickercontentprovider"
        const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
        const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
        const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"

        const val STICKER_PACK_IDENTIFIER = "sticker_pack_identifier"
        const val STICKER_PACK_NAME = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER = "sticker_pack_publisher"
        const val STICKER_PACK_ICON = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK = "ios_app_store_link"
        const val PUBLISHER_EMAIL = "publisher_email"
        const val PUBLISHER_WEBSITE = "publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "privacy_policy_website"
        const val LICENSE_AGREEMENT_WEBSITE = "license_agreement_website"
        const val IMAGE_DATA_VERSION = "image_data_version"
        const val AVOID_CACHE = "avoid_cache"
        const val ANIMATED_STICKER_PACK = "animated_sticker_pack"

        const val STICKER_FILE_NAME = "sticker_file_name"
        const val STICKER_EMOJI = "sticker_emojis"
        const val STICKER_ACCESSIBILITY_LABEL = "sticker_accessibility_label"

        private const val METADATA_CODE = 1
        private const val METADATA_PACK_CODE = 2
        private const val STICKERS_CODE = 3
        private const val STICKERS_ASSET_CODE = 4

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "metadata", METADATA_CODE)
            addURI(AUTHORITY, "metadata/*", METADATA_PACK_CODE)
            addURI(AUTHORITY, "stickers/*", STICKERS_CODE)
            addURI(AUTHORITY, "stickers_asset/*/*", STICKERS_ASSET_CODE)
        }
    }

    private fun getStorage(): StickerStorageManager {
        val ctx = context ?: throw IllegalStateException("Context is null")
        return StickerStorageManager(ctx.applicationContext)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val code = uriMatcher.match(uri)
        return when (code) {
            METADATA_CODE -> getAllPacksCursor()
            METADATA_PACK_CODE -> {
                val packId = uri.lastPathSegment ?: return null
                getPackCursor(packId)
            }
            STICKERS_CODE -> {
                val packId = uri.lastPathSegment ?: return null
                getStickersCursor(packId)
            }
            else -> null
        }
    }

    private fun getAllPacksCursor(): Cursor {
        val packs = getStorage().getAllPacks()
        val cursor = MatrixCursor(
            arrayOf(
                STICKER_PACK_IDENTIFIER,
                STICKER_PACK_NAME,
                STICKER_PACK_PUBLISHER,
                STICKER_PACK_ICON,
                ANDROID_APP_DOWNLOAD_LINK,
                IOS_APP_DOWNLOAD_LINK,
                PUBLISHER_EMAIL,
                PUBLISHER_WEBSITE,
                PRIVACY_POLICY_WEBSITE,
                LICENSE_AGREEMENT_WEBSITE,
                IMAGE_DATA_VERSION,
                AVOID_CACHE,
                ANIMATED_STICKER_PACK
            )
        )
        for (pack in packs) {
            cursor.addRow(packToRow(pack))
        }
        return cursor
    }

    private fun getPackCursor(packId: String): Cursor? {
        val pack = getStorage().getPack(packId) ?: return null
        val cursor = MatrixCursor(
            arrayOf(
                STICKER_PACK_IDENTIFIER,
                STICKER_PACK_NAME,
                STICKER_PACK_PUBLISHER,
                STICKER_PACK_ICON,
                ANDROID_APP_DOWNLOAD_LINK,
                IOS_APP_DOWNLOAD_LINK,
                PUBLISHER_EMAIL,
                PUBLISHER_WEBSITE,
                PRIVACY_POLICY_WEBSITE,
                LICENSE_AGREEMENT_WEBSITE,
                IMAGE_DATA_VERSION,
                AVOID_CACHE,
                ANIMATED_STICKER_PACK
            )
        )
        cursor.addRow(packToRow(pack))
        return cursor
    }

    private fun packToRow(pack: StickerPack): Array<Any?> {
        return arrayOf(
            pack.identifier,
            pack.name,
            pack.publisher,
            pack.trayImageFile,
            pack.androidPlayStoreLink,
            pack.iosAppStoreLink,
            pack.publisherEmail,
            pack.publisherWebsite,
            pack.privacyPolicyWebsite,
            pack.licenseAgreementWebsite,
            pack.imageDataVersion,
            if (pack.avoidCache) 1 else 0,
            if (pack.animatedStickerPack) 1 else 0
        )
    }

    private fun getStickersCursor(packId: String): Cursor? {
        val pack = getStorage().getPack(packId) ?: return null
        val cursor = MatrixCursor(
            arrayOf(
                STICKER_FILE_NAME,
                STICKER_EMOJI,
                STICKER_ACCESSIBILITY_LABEL
            )
        )
        for (sticker in pack.stickers) {
            val emojiList = if (sticker.emojis.isEmpty()) "🎵,✨" else sticker.emojis.joinToString(",")
            cursor.addRow(
                arrayOf(
                    sticker.imageFileName,
                    emojiList,
                    sticker.accessibilityLabel
                )
            )
        }
        return cursor
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val match = uriMatcher.match(uri)
        if (match == STICKERS_ASSET_CODE) {
            val segments = uri.pathSegments
            if (segments.size >= 3) {
                val packId = segments[1]
                val fileName = segments[2]
                val file = getStorage().getStickerFile(packId, fileName)
                if (file.exists()) {
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    return AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
                }
            }
        }
        throw FileNotFoundException("Asset not found for URI: $uri")
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd.$AUTHORITY.metadata"
            METADATA_PACK_CODE -> "vnd.android.cursor.item/vnd.$AUTHORITY.metadata"
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd.$AUTHORITY.stickers"
            STICKERS_ASSET_CODE -> "image/webp"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
