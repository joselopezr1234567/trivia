package cl.jlopezr.trivia.core.util

import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidShareManager(private val context: Context) : ShareManager {
    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir invitación").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

actual fun getShareManager(): ShareManager {
    val koin = object : KoinComponent {}
    val shareManager: ShareManager by koin.inject()
    return shareManager
}
