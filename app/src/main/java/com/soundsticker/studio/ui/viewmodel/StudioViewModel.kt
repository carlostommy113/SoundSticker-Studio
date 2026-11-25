package com.soundsticker.studio.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soundsticker.studio.SoundStickerApp
import com.soundsticker.studio.data.model.CreationMode
import com.soundsticker.studio.data.model.StickerItem
import com.soundsticker.studio.data.model.StickerPack
import com.soundsticker.studio.media.AudioPlayerHelper
import com.soundsticker.studio.media.AudioRecorderHelper
import com.soundsticker.studio.media.AudioTrimmerHelper
import com.soundsticker.studio.media.ImageMemeComposer
import com.soundsticker.studio.media.MediaStoreHelper
import com.soundsticker.studio.media.VideoProcessorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class StudioUiState(
    val currentMode: CreationMode = CreationMode.PHOTO_AUDIO,
    val isProcessing: Boolean = false,
    val statusMessage: String? = null,
    val showExportDialog: Boolean = false,
    val exportDialogPath: String? = null,

    // Photo + Audio
    val photoUri: Uri? = null,
    val memeTopText: String = "",
    val memeBottomText: String = "",
    val isRecording: Boolean = false,
    val audioFile: File? = null,
    val importedAudioUri: Uri? = null,
    val audioDurationMs: Long = 0L,
    val audioStartMs: Long = 0L,
    val audioEndMs: Long = 10_000L,
    val isPlayingAudio: Boolean = false,
    val audioPlaybackProgress: Float = 0f,

    // Video to Sticker
    val videoUri: Uri? = null,
    val videoDurationMs: Long = 0L,
    val videoStartMs: Long = 0L,
    val videoEndMs: Long = 5_000L,
    val keepVideoAudio: Boolean = true,
    val isPlayingVideoLoop: Boolean = false,

    // Packs
    val packs: List<StickerPack> = emptyList(),
    val playingStickerAudioKey: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = (application as SoundStickerApp).storageManager
    private val audioRecorder = AudioRecorderHelper(application)
    private val audioPlayer = AudioPlayerHelper(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        loadPacks()

        audioPlayer.onProgressUpdate = { current, total ->
            if (total > 0) {
                _uiState.update { it.copy(audioPlaybackProgress = current.toFloat() / total) }
            }
        }
        audioPlayer.onPlaybackFinished = {
            _uiState.update {
                it.copy(
                    isPlayingAudio = false,
                    isPlayingVideoLoop = false,
                    audioPlaybackProgress = 0f,
                    playingStickerAudioKey = null
                )
            }
        }

        audioRecorder.onMaxDurationReached = {
            stopRecording()
        }
    }

    fun setMode(mode: CreationMode) {
        audioPlayer.stop()
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun loadPacks() {
        viewModelScope.launch(Dispatchers.IO) {
            val packs = storageManager.getAllPacks()
            _uiState.update { it.copy(packs = packs) }
        }
    }

    // --- Photo + Audio Actions ---
    fun selectPhoto(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun updateMemeTopText(text: String) {
        _uiState.update { it.copy(memeTopText = text) }
    }

    fun updateMemeBottomText(text: String) {
        _uiState.update { it.copy(memeBottomText = text) }
    }

    fun startRecording() {
        audioPlayer.stop()
        val file = audioRecorder.startRecording(10_000L)
        _uiState.update {
            it.copy(
                isRecording = true,
                audioFile = file,
                importedAudioUri = null,
                audioDurationMs = 10_000L,
                audioStartMs = 0L,
                audioEndMs = 10_000L
            )
        }
    }

    fun stopRecording() {
        val file = audioRecorder.stopRecording()
        _uiState.update {
            it.copy(
                isRecording = false,
                audioFile = file,
                audioDurationMs = 10_000L
            )
        }
    }

    fun selectImportedAudio(uri: Uri) {
        audioPlayer.stop()
        viewModelScope.launch(Dispatchers.IO) {
            val duration = AudioTrimmerHelper.getDurationMs(getApplication(), uri)
            val effectiveDuration = if (duration > 0) duration else 10_000L
            val endTrim = effectiveDuration.coerceAtMost(10_000L)
            _uiState.update {
                it.copy(
                    importedAudioUri = uri,
                    audioFile = null,
                    audioDurationMs = effectiveDuration,
                    audioStartMs = 0L,
                    audioEndMs = endTrim
                )
            }
        }
    }

    fun setAudioTrimRange(startMs: Long, endMs: Long) {
        _uiState.update {
            it.copy(
                audioStartMs = startMs,
                audioEndMs = endMs
            )
        }
    }

    fun toggleAudioPreview() {
        if (_uiState.value.isPlayingAudio) {
            audioPlayer.stop()
            _uiState.update { it.copy(isPlayingAudio = false) }
        } else {
            val state = _uiState.value
            val target: Any? = state.audioFile ?: state.importedAudioUri
            if (target != null) {
                _uiState.update { it.copy(isPlayingAudio = true) }
                audioPlayer.play(
                    fileOrUri = target,
                    startMs = state.audioStartMs.toInt(),
                    endMs = state.audioEndMs.toInt()
                )
            }
        }
    }

    // --- Video to Sticker Actions ---
    fun selectVideo(uri: Uri?) {
        audioPlayer.stop()
        if (uri == null) {
            _uiState.update { it.copy(videoUri = null) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val duration = VideoProcessorHelper.getVideoDurationMs(getApplication(), uri)
            val effectiveDuration = if (duration > 0) duration else 5_000L
            val endTrim = effectiveDuration.coerceAtMost(5_000L)
            _uiState.update {
                it.copy(
                    videoUri = uri,
                    videoDurationMs = effectiveDuration,
                    videoStartMs = 0L,
                    videoEndMs = endTrim
                )
            }
        }
    }

    fun setVideoTrimRange(startMs: Long, endMs: Long) {
        _uiState.update {
            it.copy(
                videoStartMs = startMs,
                videoEndMs = endMs
            )
        }
    }

    fun setKeepVideoAudio(keep: Boolean) {
        _uiState.update { it.copy(keepVideoAudio = keep) }
    }

    // --- Save & Export Actions ---
    fun savePhotoAudioSticker(packId: String = "my_sound_stickers", packName: String = "Mis SoundStickers") {
        val state = _uiState.value
        val photoUri = state.photoUri ?: run {
            showToast("Selecciona una foto primero")
            return
        }

        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val time = System.currentTimeMillis()
                val stickerFileName = "sticker_$time.webp"

                val packDir = storageManager.getPackDir(packId)
                val destStickerFile = File(packDir, stickerFileName)
                val destTrayFile = File(packDir, "tray.webp")

                // 1. Process 512x512 Meme Sticker WebP & Tray (96x96)
                val imageOk = ImageMemeComposer.processImageWithMemeText(
                    context = getApplication(),
                    imageUri = photoUri,
                    topText = state.memeTopText,
                    bottomText = state.memeBottomText,
                    destStickerFile = destStickerFile,
                    destTrayFile = destTrayFile
                )

                // 2. Process Audio (soporte universal MP3, M4A, etc.)
                var finalAudioName: String? = null
                if (state.audioFile != null && state.audioFile.exists()) {
                    val audioFileName = "audio_$time.m4a"
                    val destAudioFile = File(packDir, audioFileName)
                    state.audioFile.copyTo(destAudioFile, overwrite = true)
                    finalAudioName = audioFileName
                } else if (state.importedAudioUri != null) {
                    val mimeType = getApplication<Application>().contentResolver.getType(state.importedAudioUri)
                    val ext = if (mimeType?.contains("mpeg") == true || mimeType?.contains("mp3") == true) "mp3" else "m4a"
                    val audioFileName = "audio_$time.$ext"
                    val destAudioFile = File(packDir, audioFileName)

                    var trimmed = false
                    if (ext == "m4a") {
                        trimmed = AudioTrimmerHelper.trimAudio(
                            context = getApplication(),
                            inputUri = state.importedAudioUri,
                            destFile = destAudioFile,
                            startMs = state.audioStartMs,
                            endMs = state.audioEndMs
                        )
                    }

                    if (!trimmed || !destAudioFile.exists() || destAudioFile.length() == 0L) {
                        try {
                            getApplication<Application>().contentResolver.openInputStream(state.importedAudioUri)?.use { input ->
                                FileOutputStream(destAudioFile).use { out -> input.copyTo(out) }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (destAudioFile.exists() && destAudioFile.length() > 0) {
                        finalAudioName = audioFileName
                    }
                }

                if (imageOk) {
                    val stickerItem = StickerItem(
                        imageFileName = stickerFileName,
                        audioFileName = finalAudioName,
                        durationMs = state.audioEndMs - state.audioStartMs,
                        memeTopText = state.memeTopText.ifBlank { null },
                        memeBottomText = state.memeBottomText.ifBlank { null },
                        isAnimated = false
                    )

                    // Guarda ÚNICAMENTE este sticker (sin duplicar copias falsas)
                    storageManager.addStickerToPack(packId, stickerItem, packName)

                    loadPacks()
                    showToast("¡Sticker añadido con éxito!")
                } else {
                    showToast("Error al procesar la imagen")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun saveVideoSticker(packId: String = "my_sound_stickers", packName: String = "Mis SoundStickers") {
        val state = _uiState.value
        val videoUri = state.videoUri ?: run {
            showToast("Selecciona un video primero")
            return
        }

        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val time = System.currentTimeMillis()
                val stickerFileName = "video_sticker_$time.webp"
                val audioFileName = "video_audio_$time.m4a"

                val packDir = storageManager.getPackDir(packId)
                val destStickerFile = File(packDir, stickerFileName)
                val destTrayFile = File(packDir, "tray.webp")
                val destAudioFile = File(packDir, audioFileName)

                val frameOk = VideoProcessorHelper.extractFrameAtTime(
                    context = getApplication(),
                    videoUri = videoUri,
                    timeMs = state.videoStartMs,
                    destStickerFile = destStickerFile
                )

                if (frameOk) {
                    if (!destTrayFile.exists()) {
                        destStickerFile.copyTo(destTrayFile, overwrite = true)
                    }

                    var hasAudio = false
                    if (state.keepVideoAudio) {
                        val extracted = AudioTrimmerHelper.trimAudio(
                            context = getApplication(),
                            inputUri = videoUri,
                            destFile = destAudioFile,
                            startMs = state.videoStartMs,
                            endMs = state.videoEndMs
                        )
                        hasAudio = extracted && destAudioFile.exists() && destAudioFile.length() > 0
                    }

                    val stickerItem = StickerItem(
                        imageFileName = stickerFileName,
                        audioFileName = if (hasAudio) audioFileName else null,
                        durationMs = state.videoEndMs - state.videoStartMs,
                        isAnimated = false
                    )

                    storageManager.addStickerToPack(packId, stickerItem, packName)

                    loadPacks()
                    showToast("¡Sticker añadido al pack!")
                } else {
                    showToast("Error al extraer fotograma del video")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error al procesar video: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun saveSoundStickerKit() {
        val state = _uiState.value
        _uiState.update { it.copy(isProcessing = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var savedPath: String? = null

                if (state.currentMode == CreationMode.VIDEO_TO_STICKER && state.videoUri != null) {
                    val tempClipFile = File(getApplication<Application>().cacheDir, "kit_clip_${System.currentTimeMillis()}.mp4")
                    val trimmed = VideoProcessorHelper.trimVideoSegment(
                        context = getApplication(),
                        inputUri = state.videoUri,
                        destFile = tempClipFile,
                        startMs = state.videoStartMs,
                        endMs = state.videoEndMs,
                        keepAudio = state.keepVideoAudio
                    )
                    if (trimmed) {
                        MediaStoreHelper.saveVideoKitToMovies(getApplication(), tempClipFile)
                        savedPath = "Movies/SoundStickers/ (Clip 5s con sonido)"
                    }
                } else if (state.photoUri != null) {
                    val tempImg = File(getApplication<Application>().cacheDir, "kit_img_${System.currentTimeMillis()}.webp")
                    ImageMemeComposer.processImageWithMemeText(
                        context = getApplication(),
                        imageUri = state.photoUri,
                        topText = state.memeTopText,
                        bottomText = state.memeBottomText,
                        destStickerFile = tempImg
                    )

                    val audioTarget = state.audioFile ?: if (state.importedAudioUri != null) {
                        val tempAudio = File(getApplication<Application>().cacheDir, "kit_aud_${System.currentTimeMillis()}.m4a")
                        val trimmed = AudioTrimmerHelper.trimAudio(getApplication(), state.importedAudioUri, tempAudio, state.audioStartMs, state.audioEndMs)
                        if (!trimmed || !tempAudio.exists() || tempAudio.length() == 0L) {
                            try {
                                getApplication<Application>().contentResolver.openInputStream(state.importedAudioUri)?.use { input ->
                                    FileOutputStream(tempAudio).use { out -> input.copyTo(out) }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        tempAudio
                    } else null

                    MediaStoreHelper.savePhotoAndAudioKit(getApplication(), tempImg, audioTarget)
                    savedPath = "Pictures/SoundStickers/ y Music/SoundStickers/"
                }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            showExportDialog = true,
                            exportDialogPath = savedPath
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error al guardar kit: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun openWhatsApp(useBusiness: Boolean = false) {
        MediaStoreHelper.launchWhatsApp(getApplication(), useBusiness)
    }

    fun exportPackToWhatsApp(activity: Activity, packId: String, packName: String, useBusiness: Boolean = false) {
        val pack = storageManager.getPack(packId)
        if (pack == null || pack.stickers.size < 3) {
            showToast("WhatsApp requiere un mínimo de 3 stickers en el pack para poder añadirlo (tienes ${pack?.stickers?.size ?: 0})")
            return
        }
        MediaStoreHelper.exportStickerPackToWhatsApp(activity, packId, packName, useBusiness)
    }

    fun playStickerPreview(packId: String, sticker: StickerItem) {
        val key = "${packId}_${sticker.imageFileName}"
        if (_uiState.value.playingStickerAudioKey == key) {
            audioPlayer.stop()
            _uiState.update { it.copy(playingStickerAudioKey = null) }
            return
        }

        val audioName = sticker.audioFileName
        if (audioName != null) {
            val audioFile = storageManager.getStickerFile(packId, audioName)
            if (audioFile.exists() && audioFile.length() > 0) {
                _uiState.update { it.copy(playingStickerAudioKey = key) }
                audioPlayer.play(audioFile, loopCount = 3)
                return
            }
        }
        showToast("Este sticker no tiene audio asociado")
    }

    fun deletePack(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            storageManager.deletePack(packId)
            loadPacks()
            showToast("Pack eliminado")
        }
    }

    private fun showToast(msg: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.stopRecording()
        audioPlayer.stop()
    }
}
