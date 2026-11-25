package com.soundsticker.studio.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppDarkBg
import com.soundsticker.studio.ui.theme.WhatsAppSurface
import com.soundsticker.studio.ui.theme.WhatsAppTextPrimary
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WhatsAppWebScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
    ) {
        // Banner de ayuda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = "Vincular sin cámara: Toca 'Vincular con número de teléfono' abajo del QR",
                    color = EmeraldGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Te llegará una notificación con un código de 8 dígitos para vincularlo en tu celular.",
                    color = WhatsAppTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = EmeraldGreen,
                trackColor = WhatsAppSurface
            )
        }

        // WhatsApp Web en modo Desktop
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT

                            // Forzar User-Agent de Escritorio para que cargue WhatsApp Web
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                        }

                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                return true
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                // Inyectar scripts del motor de stickers con sonido cuando cargue
                                injectSoundStickerScripts(view)
                            }
                        }

                        loadUrl("https://web.whatsapp.com")
                        webViewInstance = this
                    }
                }
            )
        }

        // Barra inferior para abrir el panel de envío
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppSurface)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    webViewInstance?.evaluateJavascript("window.postMessage({ __waas: 'togglePanel' }, '*');", null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "🎵 Abrir Creador y Enviar con Sonido",
                    color = WhatsAppDarkBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun injectSoundStickerScripts(view: WebView?) {
    if (view == null) return
    val context = view.context

    val scripts = listOf("bridge.js", "logger.js", "convert.js", "wa.js", "ui.js", "main.js")
    for (name in scripts) {
        try {
            val content = context.assets.open(name).bufferedReader().use { it.readText() }
            view.evaluateJavascript(content, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
