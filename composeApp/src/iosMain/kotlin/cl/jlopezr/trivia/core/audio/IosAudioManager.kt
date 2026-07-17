package cl.jlopezr.trivia.core.audio

class IosAudioManager : AudioManager {
    override fun playBackgroundMusic() {
        // Implementación iOS con AVAudioPlayer
    }

    override fun stopBackgroundMusic() {
    }

    override fun playTickSound() {
    }

    override fun setMuted(muted: Boolean) {
    }

    override fun lowerVolume() {
    }

    override fun restoreVolume() {
    }
}

actual fun getAudioManager(): AudioManager {
    return IosAudioManager()
}
