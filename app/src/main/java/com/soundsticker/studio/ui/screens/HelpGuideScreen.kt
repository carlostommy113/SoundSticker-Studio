package com.soundsticker.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundsticker.studio.R
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppDarkBg
import com.soundsticker.studio.ui.theme.WhatsAppSurface
import com.soundsticker.studio.ui.theme.WhatsAppTextPrimary
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpGuideScreen(
    onBack: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de Stickers con Sonido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WhatsAppSurface,
                    titleContentColor = WhatsAppTextPrimary,
                    navigationIconContentColor = WhatsAppTextPrimary
                )
            )
        },
        containerColor = WhatsAppDarkBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuideCard(
                icon = painterResource(id = R.drawable.ic_speaker),
                title = "¿Cómo funcionan los Stickers con Sonido?",
                description = "WhatsApp permite enviar stickers con sonido en sus chats y estados. SoundSticker Studio te permite preparar el sticker visual a 512x512 y el audio recortado listo para ser enviado."
            )

            GuideCard(
                icon = painterResource(id = R.drawable.ic_save),
                title = "El Kit para Sticker con Sonido",
                description = "Al tocar 'Guardar kit para Sticker con Sonido', la app guarda en 'Movies/SoundStickers/' el video o imagen + pista de sonido perfectamente recortada (5s para video, 10s para foto). Puedes adjuntarlo directamente desde el selector de medios de WhatsApp."
            )

            GuideCard(
                icon = rememberVectorPainter(Icons.Default.Send),
                title = "Exportación oficial de Stickers",
                description = "La aplicación implementa el ContentProvider oficial de WhatsApp (com.whatsapp.sticker.READ). Puedes añadir paquetes de stickers completos directamente a WhatsApp y WhatsApp Business con un solo toque."
            )

            GuideCard(
                icon = rememberVectorPainter(Icons.Default.CheckCircle),
                title = "Regla de los 3 Stickers",
                description = "Por especificación obligatoria de WhatsApp, cada paquete debe tener un mínimo de 3 stickers y un máximo de 30. SoundSticker Studio asegura automáticamente que tus packs cumplan este requisito."
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Abrir WhatsApp",
                    color = WhatsAppDarkBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GuideCard(
    icon: Painter,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WhatsAppDarkBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = WhatsAppTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = WhatsAppTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
