package cl.jlopezr.trivia

import android.os.Build
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val language: String = Locale.getDefault().language
}

actual fun getPlatform(): Platform = AndroidPlatform()