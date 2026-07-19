package cl.jlopezr.trivia.core.util

interface ShareManager {
    fun shareText(text: String)
}

expect fun getShareManager(): ShareManager
