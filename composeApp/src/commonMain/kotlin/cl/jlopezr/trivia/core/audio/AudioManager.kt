package cl.jlopezr.trivia.core.audio

import androidx.compose.runtime.Composable

interface AudioManager {
    fun playBackgroundMusic()
    fun stopBackgroundMusic()
    fun pauseBackgroundMusic()
    fun resumeBackgroundMusic()
    fun playTickSound()
    fun stopTickSound()
    fun setMuted(muted: Boolean)
    fun lowerVolume()
    fun restoreVolume()
}

/**
 * Singleton placeholder or Koin provided instance
 */
expect fun getAudioManager(): AudioManager
