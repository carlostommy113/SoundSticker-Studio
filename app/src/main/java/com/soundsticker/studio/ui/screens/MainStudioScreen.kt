package com.soundsticker.studio.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.soundsticker.studio.R
import com.soundsticker.studio.data.model.CreationMode
import com.soundsticker.studio.ui.components.WhatsAppExportDialog
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppDarkBg
import com.soundsticker.studio.ui.theme.WhatsAppSurface
import com.soundsticker.studio.ui.theme.WhatsAppTextPrimary
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary
import com.soundsticker.studio.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStudioScreen(
    viewModel: StudioViewModel,
    onRequestMicPermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showHelpScreen by remember { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    if (showHelpScreen) {
        HelpGuideScreen(
            onBack = { showHelpScreen = false },
            onOpenWhatsApp = { viewModel.openWhatsApp(false) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SoundSticker Studio",
                        color = WhatsAppTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showHelpScreen = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Guía de Ayuda",
                            tint = EmeraldGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WhatsAppSurface,
                    titleContentColor = WhatsAppTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = WhatsAppSurface
            ) {
                NavigationBarItem(
                    selected = state.currentMode == CreationMode.PHOTO_AUDIO,
                    onClick = { viewModel.setMode(CreationMode.PHOTO_AUDIO) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_image),
                            contentDescription = "Foto + Audio"
                        )
                    },
                    label = { Text("Foto + Audio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppDarkBg,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen,
                        unselectedIconColor = WhatsAppTextSecondary,
                        unselectedTextColor = WhatsAppTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = state.currentMode == CreationMode.VIDEO_TO_STICKER,
                    onClick = { viewModel.setMode(CreationMode.VIDEO_TO_STICKER) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_video),
                            contentDescription = "Video a Sticker"
                        )
                    },
                    label = { Text("Video a Sticker", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppDarkBg,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen,
                        unselectedIconColor = WhatsAppTextSecondary,
                        unselectedTextColor = WhatsAppTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = state.currentMode == CreationMode.MY_PACKS,
                    onClick = { viewModel.setMode(CreationMode.MY_PACKS) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pack),
                            contentDescription = "Mis Packs"
                        )
                    },
                    label = { Text("Mis Packs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppDarkBg,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen,
                        unselectedIconColor = WhatsAppTextSecondary,
                        unselectedTextColor = WhatsAppTextSecondary
                    )
                )
            }
        },
        containerColor = WhatsAppDarkBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.currentMode) {
                CreationMode.PHOTO_AUDIO -> {
                    PhotoAudioStudioScreen(
                        state = state,
                        viewModel = viewModel,
                        onRequestMicPermission = onRequestMicPermission
                    )
                }
                CreationMode.VIDEO_TO_STICKER -> {
                    VideoToStickerScreen(
                        state = state,
                        viewModel = viewModel
                    )
                }
                CreationMode.MY_PACKS -> {
                    MyPacksScreen(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }

            // WhatsApp Export Step-by-Step Dialog
            WhatsAppExportDialog(
                isOpen = state.showExportDialog,
                onDismiss = { viewModel.dismissExportDialog() },
                onOpenWhatsApp = { viewModel.openWhatsApp(false) },
                kitSavedPath = state.exportDialogPath
            )
        }
    }
}
