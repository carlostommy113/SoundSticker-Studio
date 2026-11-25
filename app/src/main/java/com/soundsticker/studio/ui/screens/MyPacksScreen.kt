package com.soundsticker.studio.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soundsticker.studio.SoundStickerApp
import com.soundsticker.studio.data.model.StickerItem
import com.soundsticker.studio.data.model.StickerPack
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
fun MyPacksScreen(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (state.packs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Aún no tienes packs creados",
                    color = WhatsAppTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Crea tu primer sticker en las pestañas anteriores",
                    color = WhatsAppTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(state.packs, key = { it.identifier }) { pack ->
            StickerPackCard(
                pack = pack,
                playingStickerKey = state.playingStickerAudioKey,
                onPlayStickerAudio = { sticker -> viewModel.playStickerPreview(pack.identifier, sticker) },
                onExportWhatsApp = { isBusiness ->
                    if (activity != null) {
                        viewModel.exportPackToWhatsApp(activity, pack.identifier, pack.name, isBusiness)
                    }
                },
                onDeletePack = { viewModel.deletePack(pack.identifier) }
            )
        }
    }
}

@Composable
private fun StickerPackCard(
    pack: StickerPack,
    playingStickerKey: String?,
    onPlayStickerAudio: (StickerItem) -> Unit,
    onExportWhatsApp: (isBusiness: Boolean) -> Unit,
    onDeletePack: () -> Unit
) {
    val storage = SoundStickerApp.instance.storageManager

    Card(
        colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Pack Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pack.name,
                        color = WhatsAppTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pack.stickers.size} stickers • ${pack.publisher}",
                        color = WhatsAppTextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDeletePack) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Pack",
                        tint = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stickers Grid (3 columns)
            val displayStickers = pack.stickers.take(12)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (displayStickers.size > 3) 220.dp else 110.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayStickers, key = { it.imageFileName }) { sticker ->
                    val file = storage.getStickerFile(pack.identifier, sticker.imageFileName)
                    val isPlayingThis = playingStickerKey == "${pack.identifier}_${sticker.imageFileName}"

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(WhatsAppDarkBg)
                            .border(
                                width = if (isPlayingThis) 2.dp else 1.dp,
                                color = if (isPlayingThis) EmeraldGreen else WhatsAppSurfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPlayStickerAudio(sticker) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = sticker.accessibilityLabel,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Speaker Icon in corner
                        if (sticker.audioFileName != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlayingThis) EmeraldGreen else WhatsAppDarkBg.copy(alpha = 0.75f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Tocar para escuchar",
                                    tint = if (isPlayingThis) WhatsAppDarkBg else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Export Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onExportWhatsApp(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = WhatsAppDarkBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Añadir a WhatsApp",
                        color = WhatsAppDarkBg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { onExportWhatsApp(true) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "WhatsApp Business",
                        color = WhatsAppTextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
