package cl.jlopezr.trivia.core.audio

import androidx.compose.runtime.Composable

interface AudioManager {
    fun playBackgroundMusic()
    fun stopBackgroundMusic()
    fun playTickSound()
    fun setMuted(muted: Boolean)
    fun lowerVolume()
    fun restoreVolume()
}

/**
 * Singleton placeholder or Koin provided instance
 */
expect fun getAudioManager(): AudioManager
