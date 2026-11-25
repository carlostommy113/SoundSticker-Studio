package com.soundsticker.studio.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppSurfaceVariant
import com.soundsticker.studio.ui.theme.WhatsAppTextPrimary
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary

@Composable
fun MemeTextOverlay(
    topText: String,
    onTopTextChange: (String) -> Unit,
    bottomText: String,
    onBottomTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = topText,
            onValueChange = onTopTextChange,
            label = { Text("Texto Superior (Estilo Meme)", color = WhatsAppTextSecondary, fontSize = 13.sp) },
            placeholder = { Text("EJ: CUANDO SUENA EL AUDIO...", color = WhatsAppTextSecondary.copy(alpha = 0.5f)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = WhatsAppSurfaceVariant,
                focusedTextColor = WhatsAppTextPrimary,
                unfocusedTextColor = WhatsAppTextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = bottomText,
            onValueChange = onBottomTextChange,
            label = { Text("Texto Inferior (Opcional)", color = WhatsAppTextSecondary, fontSize = 13.sp) },
            placeholder = { Text("EJ: Y TODOS SE QUEDAN CALLADOS", color = WhatsAppTextSecondary.copy(alpha = 0.5f)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = WhatsAppSurfaceVariant,
                focusedTextColor = WhatsAppTextPrimary,
                unfocusedTextColor = WhatsAppTextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
