package cl.jlopezr.trivia.core.util

class IosShareManager : ShareManager {
    override fun shareText(text: String) {
        // iOS sharing implementation placeholder
    }
}

actual fun getShareManager(): ShareManager {
    return IosShareManager()
}
