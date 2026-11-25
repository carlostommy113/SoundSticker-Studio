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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundsticker.studio.R
import com.soundsticker.studio.ui.components.RangeSliderTrimmer
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
fun VideoToStickerScreen(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.selectVideo(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Video a Sticker Animado con Sonido (hasta 5s)",
            color = WhatsAppTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Video Selection Area
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(WhatsAppSurface)
                .border(2.dp, if (state.videoUri != null) EmeraldGreen else WhatsAppSurfaceVariant, RoundedCornerShape(16.dp))
                .clickable { videoPicker.launch("video/*") },
            contentAlignment = Alignment.Center
        ) {
            if (state.videoUri != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_video),
                        contentDescription = "Video cargado",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Video cargado correctamente",
                        color = WhatsAppTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Toca para cambiar video",
                        color = WhatsAppTextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_video),
                        contentDescription = "Elegir Video",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para elegir video de la galería",
                        color = WhatsAppTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Se recortará a máximo 5 segundos",
                        color = WhatsAppTextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trimmer & Audio Options
        if (state.videoUri != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Selecciona el fragmento (Máx. 5 seg):",
                        color = WhatsAppTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RangeSliderTrimmer(
                        totalDurationMs = if (state.videoDurationMs > 0) state.videoDurationMs else 5_000L,
                        startMs = state.videoStartMs,
                        endMs = state.videoEndMs,
                        maxAllowedDurationMs = 5_000L,
                        onRangeChange = { s, e -> viewModel.setVideoTrimRange(s, e) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio track toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Conservar pista de audio original",
                                color = WhatsAppTextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "El sonido del clip se incluirá en el sticker",
                                color = WhatsAppTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = state.keepVideoAudio,
                            onCheckedChange = { viewModel.toggleVideoAudio(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldGreen,
                                checkedTrackColor = WhatsAppSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Loops Preview Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.playVideoLoopPreview() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (state.isPlayingVideoLoop) ErrorRed else EmeraldGreen)
                        ) {
                            Icon(
                                painter = painterResource(id = if (state.isPlayingVideoLoop) R.drawable.ic_stop else R.drawable.ic_play),
                                contentDescription = "Loop Preview",
                                tint = WhatsAppDarkBg,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_cut),
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Previsualizar en bucle (3 repeticiones)",
                                    color = WhatsAppTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Igual al comportamiento nativo de WhatsApp",
                                color = WhatsAppTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            if (state.isProcessing) {
                CircularProgressIndicator(color = EmeraldGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Procesando clip y audio...", color = WhatsAppTextSecondary, fontSize = 13.sp)
            } else {
                Button(
                    onClick = { viewModel.saveVideoSticker() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = WhatsAppDarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Añadir a Pack de WhatsApp",
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
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
