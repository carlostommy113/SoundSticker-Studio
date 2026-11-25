# 🎵 SoundSticker Studio

[![Android CI & Release Build](https://github.com/carlostommy113/SoundSticker-Studio/actions/workflows/build-apk.yml/badge.svg)](https://github.com/carlostommy113/SoundSticker-Studio/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![compileSdk](https://img.shields.io/badge/compileSdk-35-orange.svg)](https://developer.android.com/about/versions/15)
[![WhatsApp Compatible](https://img.shields.io/badge/WhatsApp-Stickers%20Ready-25D366.svg)](https://www.whatsapp.com)

**SoundSticker Studio** (`com.soundsticker.studio`) es una aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose (Material 3)**, diseñada para preparar, componer y exportar **stickers con sonido** compatibles con las especificaciones de WhatsApp y WhatsApp Business.

La aplicación está preparada para compilarse y distribuirse automáticamente mediante **GitHub Actions**, sin necesidad de contar con Android Studio instalado en tu máquina local.

---

## 📱 Capturas y Modos de Creación

### 1. Modo Foto + Audio (Hasta 10 Segundos)
* **Recorte Inteligente 1:1**: Selecciona cualquier imagen de tu galería y se ajustará automáticamente a **512x512 píxeles** en formato WebP (< 100 KB).
* **Texto con Estilo Meme**: Generador de texto superior e inferior con tipografía meme clásica (relleno blanco con trazo grueso negro de alto contraste) para máxima legibilidad sobre cualquier fondo.
* **Sonido Personalizado**:
  * **Micrófono en vivo**: Grabación directa con indicador visual y temporizador de hasta 10 segundos.
  * **Importar audio**: Carga cualquier archivo de audio (`.mp3`, `.m4a`, `.wav`, etc.) desde tu almacenamiento.
* **Recortador Visual**: Ajuste de punto de inicio y fin en tiempo real con forma de onda animada y botón de escucha interactivo.

### 2. Modo Video a Sticker Animado (Hasta 5 Segundos)
* **Fragmento de 5 Segundos**: Selecciona cualquier video de tu galería y recórtalo al fragmento exacto que deseas.
* **Bucle x3 de WhatsApp**: Previsualización con reproducción en 3 repeticiones idéntica al comportamiento nativo de WhatsApp.
* **Control de Audio**: Conserva la pista de sonido original del video o asóciala con otro audio.

### 3. Mis Packs y Exportación WhatsApp
* **ContentProvider Oficial**: Implementación completa de `com.whatsapp.sticker.READ` (`content://com.soundsticker.studio.stickercontentprovider`) que permite exportar tus paquetes directamente a WhatsApp y WhatsApp Business mediante el intent `com.whatsapp.intent.action.ENABLE_STICKER_PACK`.
* **Guardar kit para Sticker con Sonido**: Guarda el clip de 5s o la imagen + audio de 10s en la carpeta `Movies/SoundStickers/` (usando MediaStore nativo), mostrándote una guía visual paso a paso y un botón de un toque para abrir WhatsApp.
* **Vista previa interactiva**: Toca el ícono de altavoz en cualquier sticker para escuchar su sonido al instante.

---

## 🚀 Cómo Descargar el APK desde GitHub Actions

No necesitas compilar el proyecto en tu computadora ni instalar Android Studio:

### Opción A: Desde GitHub Actions (En cada Push a `main`)
1. Ingresa a la pestaña **[Actions](https://github.com/carlostommy113/SoundSticker-Studio/actions)** de este repositorio.
2. Haz clic en la última ejecución del workflow **Build SoundSticker Studio APK**.
3. En la sección inferior **Artifacts**, haz clic en **SoundStickerStudio-Debug-APK** para descargar el archivo ZIP con el `.apk` listo.
4. Descomprime el archivo y pásalo a tu celular Android para instalarlo.

### Opción B: Desde GitHub Releases (Etiquetas `v*`)
1. Ve a la sección **[Releases](https://github.com/carlostommy113/SoundSticker-Studio/releases)** del repositorio.
2. En la versión más reciente, descarga directamente el archivo `SoundStickerStudio-debug.apk`.
3. Instálalo en tu dispositivo Android (recuerda habilitar "Instalar apps de fuentes desconocidas" si tu sistema lo solicita).

---

## 🛠️ Arquitectura y Tecnologías

| Componente | Especificación |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.0.21 |
| **UI Toolkit** | Jetpack Compose con Material 3 (Tema Oscuro por defecto `#121B22` y acentos verde esmeralda `#25D366`) |
| **SDK** | `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` |
| **Java** | Java 17 (Temurin) |
| **Gradle** | Gradle 8.11.1 con Kotlin DSL (`.kts`) y Version Catalogs (`libs.versions.toml`) |
| **Multimedia** | APIs nativas `MediaExtractor`, `MediaMuxer`, `MediaRecorder`, `MediaPlayer` y compresión WebP sin dependencias pesadas de FFmpeg |
| **Almacenamiento** | `MediaStore` (`Movies/SoundStickers/`), almacenamiento interno aislado y persistencia en JSON |
| **Permisos** | Gestión en tiempo de ejecución para `RECORD_AUDIO`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` y compatibilidad retroactiva con Android 12 e inferior |

---

## 📂 Estructura del Proyecto

```
SoundStickerStudio/
├── .github/
│   └── workflows/
│       └── build-apk.yml            # Automatización CI/CD en GitHub Actions
├── app/
│   ├── build.gradle.kts             # Configuración del módulo Android
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml  # Permisos, Queries y ContentProvider de WhatsApp
│           ├── java/com/soundsticker/studio/
│           │   ├── SoundStickerApp.kt
│           │   ├── MainActivity.kt
│           │   ├── data/
│           │   │   ├── model/       # Modelos StickerItem, StickerPack, CreationMode
│           │   │   └── local/       # StickerStorageManager y persistencia de packs
│           │   ├── media/           # Helpers de Audio, Video, Memes y MediaStore
│           │   ├── provider/        # WhatsAppStickerContentProvider oficial
│           │   └── ui/              # Pantallas Compose, Componentes y Tema Oscuro
│           └── res/                 # Vectores XML, launcher icons y rutas
├── gradle/
│   ├── libs.versions.toml           # Catálogo centralizado de dependencias
│   └── wrapper/                     # Gradle Wrapper 8.11.1
├── build.gradle.kts                 # Configuración raíz de plugins
├── settings.gradle.kts
└── README.md
```

---

## 💬 Cómo usar tus Stickers en WhatsApp

1. Abre **SoundSticker Studio** en tu teléfono.
2. Selecciona **Foto + Audio** o **Video a Sticker**.
3. Personaliza tu imagen o video y ajusta el sonido deseado (hasta 10s para foto, 5s para video).
4. Elige una de las dos vías de exportación:
   * **Vía ContentProvider**: Presiona *Añadir a Pack de WhatsApp* para importar el pack completo en tu sección de stickers de WhatsApp.
   * **Vía Sound Kit**: Presiona *Guardar kit para Sticker con Sonido* para enviar el clip con sonido directamente desde la carpeta `Movies/SoundStickers`.
5. ¡Listo! Comparte tus stickers con sonido con tus amigos y grupos.
