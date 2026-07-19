package cl.jlopezr.trivia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cl.jlopezr.trivia.di.appModule
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.*
import org.koin.core.context.startKoin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalamos la SplashScreen del sistema y capturamos la instancia
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        Log.d("TRIVIA_APP", "Iniciando MainActivity...")

        // Inicializar AdMob
        MobileAds.initialize(this) { status ->
            Log.d("TRIVIA_APP", "AdMob inicializado: $status")
        }

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