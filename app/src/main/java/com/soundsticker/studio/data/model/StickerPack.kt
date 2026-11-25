package com.soundsticker.studio.data.model

import com.google.gson.annotations.SerializedName

data class StickerPack(
    @SerializedName("android_play_store_link")
    val androidPlayStoreLink: String = "",
    
    @SerializedName("ios_app_store_link")
    val iosAppStoreLink: String = "",
    
    @SerializedName("publisher_email")
    val publisherEmail: String = "support@soundsticker.studio",
    
    @SerializedName("publisher_website")
    val publisherWebsite: String = "https://soundsticker.studio",
    
    @SerializedName("privacy_policy_website")
    val privacyPolicyWebsite: String = "https://soundsticker.studio/privacy",
    
    @SerializedName("license_agreement_website")
    val licenseAgreementWebsite: String = "https://soundsticker.studio/license",
    
    @SerializedName("image_data_version")
    val imageDataVersion: String = "1",
    
    @SerializedName("avoid_cache")
    val avoidCache: Boolean = false,
    
    @SerializedName("animated_sticker_pack")
    val animatedStickerPack: Boolean = false,
    
    @SerializedName("identifier")
    val identifier: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("publisher")
    val publisher: String = "SoundSticker Studio",
    
    @SerializedName("tray_image_file")
    val trayImageFile: String = "tray.webp",
    
    @SerializedName("stickers")
    val stickers: MutableList<StickerItem> = mutableListOf()
)
