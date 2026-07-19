package cl.jlopezr.trivia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.jlopezr.trivia.core.ads.getAdsManager
import cl.jlopezr.trivia.core.audio.getAudioManager
import cl.jlopezr.trivia.shared.core.data.ProgressStorage
import cl.jlopezr.trivia.navigation.AppNavigation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect

// Eliminamos @Preview y su import para limpiar el archivo
@Composable
fun App() {
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- MANEJO DE CICLO DE VIDA PARA MÚSICA ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    getAudioManager().pauseBackgroundMusic()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!ProgressStorage.isMuted) {
                        getAudioManager().resumeBackgroundMusic()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- MÚSICA DE FONDO GLOBAL ---
    LaunchedEffect(Unit) {
        getAudioManager().setMuted(ProgressStorage.isMuted) // 🔥 Inicializar estado de mute
        getAudioManager().playBackgroundMusic()
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Contenedor principal con espacio para el banner
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 50.dp)) {
                AppNavigation()
            }

            // Banner permanente abajo (aprox 50dp de alto)
            getAdsManager().BannerAd(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}