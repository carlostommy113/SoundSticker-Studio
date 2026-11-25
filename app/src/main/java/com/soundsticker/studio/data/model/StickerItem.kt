package com.soundsticker.studio.data.model

import com.google.gson.annotations.SerializedName

data class StickerItem(
    @SerializedName("image_file_name")
    val imageFileName: String,
    
    @SerializedName("emojis")
    val emojis: List<String> = listOf("🎵", "✨"),
    
    @SerializedName("accessibility_label")
    val accessibilityLabel: String = "Sound Sticker",
    
    @SerializedName("audio_file_name")
    val audioFileName: String? = null,
    
    @SerializedName("is_animated")
    val isAnimated: Boolean = false,
    
    @SerializedName("duration_ms")
    val durationMs: Long = 0L,
    
    @SerializedName("meme_top_text")
    val memeTopText: String? = null,
    
    @SerializedName("meme_bottom_text")
    val memeBottomText: String? = null,
    
    @SerializedName("created_timestamp")
    val createdTimestamp: Long = System.currentTimeMillis()
)
