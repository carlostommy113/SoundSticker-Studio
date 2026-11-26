package com.soundsticker.studio.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppDarkBg
import com.soundsticker.studio.ui.theme.WhatsAppSurface
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WhatsAppWebScreen(
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // Selector de archivos nativo de Android para imágenes, audios y videos de la web
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, intent)
            filePathCallback?.onReceiveValue(uris)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

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
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "Vinculación rápida: Toca 'Vincular con número de teléfono' abajo del QR",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ingresa tu número y confirma el código en la notificación de WhatsApp.",
                    color = WhatsAppTextSecondary,
                    fontSize = 10.sp
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

        // WhatsApp Web adaptado a la pantalla del móvil
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

                            // Escala ajustada para pantalla de teléfono
                            textZoom = 80

                            // User-Agent Desktop moderno
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                        }

                        // Zoom ajustado al 60% para que toda la pantalla de WhatsApp Web quepa en el celular
                        setInitialScale(60)

                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                return true
                            }

                            // Permite que los botones de "Elegir archivo/imagen/audio/video" abran la galería de Android
                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallbackIn: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                filePathCallback?.onReceiveValue(null)
                                filePathCallback = filePathCallbackIn

                                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                }
                                try {
                                    fileChooserLauncher.launch(intent)
                                } catch (e: Exception) {
                                    filePathCallback?.onReceiveValue(null)
                                    filePathCallback = null
                                    return false
                                }
                                return true
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                injectSoundStickerScripts(view)
                                // Inyectar CSS responsivo para que el creador de stickers se adapte al ancho del móvil
                                injectMobileResponsiveCss(view)
                            }
                        }

                        loadUrl("https://web.whatsapp.com")
                        webViewInstance = this
                    }
                }
            )
        }

        // Botón inferior para abrir el Creador de stickers con sonido
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppSurface)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    webViewInstance?.evaluateJavascript("window.postMessage({ __waas: 'togglePanel' }, '*');", null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
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

private fun injectMobileResponsiveCss(view: WebView?) {
    val mobileCss = """
        (function() {
            var css = `
                #waas-root .panel {
                    width: 95vw !important;
                    max-height: 85vh !important;
                    padding: 12px !important;
                    font-size: 13px !important;
                }
                #waas-root input[type=text] {
                    width: 140px !important;
                }
                #waas-root .tabs button {
                    padding: 6px 8px !important;
                    font-size: 12px !important;
                }
            `;
            var style = document.createElement('style');
            style.type = 'text/css';
            style.appendChild(document.createTextNode(css));
            document.head.appendChild(style);
        })();
    """.trimIndent()
    view?.evaluateJavascript(mobileCss, null)
}
