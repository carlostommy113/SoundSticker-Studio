package com.soundsticker.studio

import android.app.Application
import com.soundsticker.studio.data.local.StickerStorageManager

class SoundStickerApp : Application() {
    lateinit var storageManager: StickerStorageManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        storageManager = StickerStorageManager(this)
        storageManager.initDefaultSamplePackIfNeeded()
    }

    companion object {
        lateinit var instance: SoundStickerApp
            private set
    }
}
