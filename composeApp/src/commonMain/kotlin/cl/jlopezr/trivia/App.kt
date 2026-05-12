package cl.jlopezr.trivia

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cl.jlopezr.trivia.navigation.AppNavigation

// Eliminamos @Preview y su import para limpiar el archivo
@Composable
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}