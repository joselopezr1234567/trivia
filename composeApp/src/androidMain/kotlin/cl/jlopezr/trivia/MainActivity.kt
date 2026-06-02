package cl.jlopezr.trivia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cl.jlopezr.trivia.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.*
import org.koin.core.context.startKoin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TRIVIA_APP", "Iniciando MainActivity...")

        try {
            Log.d("TRIVIA_APP", "Intentando iniciar Koin...")
            startKoin {
                androidLogger()
                androidContext(this@MainActivity)
                modules(appModule)
            }
            Log.d("TRIVIA_APP", "Koin iniciado correctamente.")
        } catch (e: Exception) {
            Log.e("TRIVIA_APP", "ERROR CRÍTICO AL INICIAR KOIN: ${e.message}", e)
        }

        setContent {
            Log.d("TRIVIA_APP", "Dibujando contenido (App)...")
            App()
        }
    }
}