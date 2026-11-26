package com.soundsticker.studio.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var currentZoom by remember { mutableFloatStateOf(0.55f) } // 55% de tamaño: se ve chiquito y completo

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

    fun applyZoom(newZoom: Float) {
        currentZoom = newZoom.coerceIn(0.35f, 1.0f)
        webViewInstance?.evaluateJavascript(
            "document.body.style.zoom = '$currentZoom';",
            null
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
    ) {
        // Barra superior con controles de zoom manual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WhatsApp Web Compacto",
                    color = EmeraldGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca 'Vincular con número' abajo del QR",
                    color = WhatsAppTextSecondary,
                    fontSize = 10.sp
                )
            }

            // Botones para achicar aún más o agrandar
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = { applyZoom(currentZoom - 0.08f) },
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${(currentZoom * 100).toInt()}%",
                    color = WhatsAppTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(6.dp))

                FilledTonalButton(
                    onClick = { applyZoom(currentZoom + 0.08f) },
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = EmeraldGreen,
                trackColor = WhatsAppSurface
            )
        }

        // WhatsApp Web con tamaño compacto
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        setLayerType(View.LAYER_TYPE_HARDWARE, null)

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

                            // User-Agent Desktop
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        }

                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true

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
                                // Forzar tamaño chiquito (55%) al terminar de cargar
                                view?.evaluateJavascript("document.body.style.zoom = '$currentZoom';", null)
                                injectSoundStickerScripts(view)
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
                /* Panel de stickers proporcional y compacto para el celular */
                #waas-root .overlay {
                    padding: 0px !important;
                }
                #waas-root .panel {
                    width: 95vw !important;
                    max-width: 95vw !important;
                    max-height: 85vh !important;
                    padding: 10px !important;
                    font-size: 13px !important;
                    border-radius: 12px !important;
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
