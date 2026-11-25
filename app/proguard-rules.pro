# ProGuard rules for SoundSticker Studio
-keep class com.soundsticker.studio.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
