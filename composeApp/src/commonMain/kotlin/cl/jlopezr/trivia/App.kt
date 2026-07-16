package cl.jlopezr.trivia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.jlopezr.trivia.core.ads.getAdsManager
import cl.jlopezr.trivia.navigation.AppNavigation

// Eliminamos @Preview y su import para limpiar el archivo
@Composable
fun App() {
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