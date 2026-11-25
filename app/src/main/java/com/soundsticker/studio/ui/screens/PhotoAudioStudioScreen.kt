package com.soundsticker.studio.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soundsticker.studio.R
import com.soundsticker.studio.ui.components.MemeTextOverlay
import com.soundsticker.studio.ui.components.RangeSliderTrimmer
import com.soundsticker.studio.ui.components.WaveformBar
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.ErrorRed
import com.soundsticker.studio.ui.theme.WhatsAppDarkBg
import com.soundsticker.studio.ui.theme.WhatsAppSurface
import com.soundsticker.studio.ui.theme.WhatsAppSurfaceVariant
import com.soundsticker.studio.ui.theme.WhatsAppTextPrimary
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary
import com.soundsticker.studio.ui.viewmodel.StudioUiState
import com.soundsticker.studio.ui.viewmodel.StudioViewModel

@Composable
fun PhotoAudioStudioScreen(
    state: StudioUiState,
    viewModel: StudioViewModel,
    onRequestMicPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Pickers
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.selectPhoto(uri)
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.selectImportedAudio(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Photo Picker / 512x512 Preview Area ---
        Text(
            text = "1. Selecciona y Personaliza tu Imagen (1:1 512x512)",
            color = WhatsAppTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(WhatsAppSurface)
                .border(2.dp, if (state.photoUri != null) EmeraldGreen else WhatsAppSurfaceVariant, RoundedCornerShape(16.dp))
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (state.photoUri != null) {
                AsyncImage(
                    model = state.photoUri,
                    contentDescription = "Selected sticker image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Live Meme Text Preview Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.memeTopText.isNotBlank()) {
                        Text(
                            text = state.memeTopText.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(1.dp))
                    }

                    if (state.memeBottomText.isNotBlank()) {
                        Text(
                            text = state.memeBottomText.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Elegir Imagen",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para elegir foto de la galería",
                        color = WhatsAppTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Se adaptará a 512x512 px automáticamente",
                        color = WhatsAppTextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Meme Text Fields ---
        MemeTextOverlay(
            topText = state.memeTopText,
            onTopTextChange = { viewModel.updateMemeTopText(it) },
            bottomText = state.memeBottomText,
            onBottomTextChange = { viewModel.updateMemeBottomText(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. Audio Selection & Trimmer (Up to 10s) ---
        Text(
            text = "2. Asocia un Sonido (Máximo 10 Segundos)",
            color = WhatsAppTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Audio Source Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Record with Mic
                    Button(
                        onClick = {
                            if (state.isRecording) {
                                viewModel.stopRecording()
                            } else {
                                onRequestMicPermission()
                                viewModel.startRecording()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isRecording) ErrorRed else WhatsAppSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = if (state.isRecording) R.drawable.ic_stop else R.drawable.ic_mic),
                            contentDescription = null,
                            tint = if (state.isRecording) Color.White else EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isRecording) "Detener (10s)" else "Grabar Mic",
                            color = if (state.isRecording) Color.White else WhatsAppTextPrimary
                        )
                    }

                    // Import Audio File
                    OutlinedButton(
                        onClick = { audioPicker.launch("audio/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_speaker),
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Elegir Archivo", color = WhatsAppTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Waveform bar
                WaveformBar(
                    isPlaying = state.isPlayingAudio,
                    isRecording = state.isRecording,
                    progressFraction = state.audioPlaybackProgress
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Trimmer if audio is loaded
                if (state.audioDurationMs > 0 || state.audioFile != null || state.importedAudioUri != null) {
                    RangeSliderTrimmer(
                        totalDurationMs = if (state.audioDurationMs > 0) state.audioDurationMs else 10_000L,
                        startMs = state.audioStartMs,
                        endMs = state.audioEndMs,
                        maxAllowedDurationMs = 10_000L,
                        onRangeChange = { s, e -> viewModel.setAudioTrimRange(s, e) }
                    )

                    // Preview Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleAudioPreview() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (state.isPlayingAudio) ErrorRed else EmeraldGreen)
                        ) {
                            Icon(
                                painter = painterResource(id = if (state.isPlayingAudio) R.drawable.ic_stop else R.drawable.ic_play),
                                contentDescription = "Play/Stop Preview",
                                tint = WhatsAppDarkBg,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (state.isPlayingAudio) "Reproduciendo vista previa..." else "Toca para escuchar el audio recortado",
                            color = WhatsAppTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Action Buttons ---
        if (state.isProcessing) {
            CircularProgressIndicator(color = EmeraldGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Procesando y generando sticker...", color = WhatsAppTextSecondary, fontSize = 13.sp)
        } else {
            Button(
                onClick = { viewModel.savePhotoAudioSticker() },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = WhatsAppDarkBg)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Añadir a Pack de WhatsApp (ContentProvider)",
                    color = WhatsAppDarkBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.saveSoundStickerKit() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar kit para Sticker con Sonido",
                    color = WhatsAppTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
