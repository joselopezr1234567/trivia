package cl.jlopezr.trivia.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.AudioAttributes
import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import myapplication.composeapp.generated.resources.Res

class AndroidAudioManager(private val context: Context) : AudioManager, KoinComponent {

    private var mediaPlayer: MediaPlayer? = null
    private val soundPool: SoundPool
    private var tickSoundId: Int = 0
    private var isMuted: Boolean = false

    init {
        Log.d("AUDIO_DEBUG", "Iniciando AndroidAudioManager...")
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Intentar cargar sonido de tick desde assets/raw si existiera
        try {
            // Buscamos en la ruta interna donde KMP guarda los archivos de composeResources
            val assetPath = "composeResources/myapplication.composeapp.generated.resources/files/tick.mp3"
            Log.d("AUDIO_DEBUG", "Intentando cargar tick desde: $assetPath")
            val assetDescriptor = context.assets.openFd(assetPath)
            tickSoundId = soundPool.load(assetDescriptor, 1)
            Log.d("AUDIO_DEBUG", "Tick sound ID asignado: $tickSoundId")
        } catch (e: Exception) {
            Log.e("AUDIO_DEBUG", "ERROR al cargar tick.mp3: ${e.message}")
        }
    }

    override fun setMuted(muted: Boolean) {
        Log.d("AUDIO_DEBUG", "Cambiando Mute a: $muted")
        isMuted = muted
        if (muted) {
            mediaPlayer?.setVolume(0f, 0f)
        } else {
            mediaPlayer?.setVolume(1f, 1f)
        }
    }

    override fun lowerVolume() {
        if (!isMuted) {
            Log.d("AUDIO_DEBUG", "Bajando volumen de música de fondo...")
            mediaPlayer?.setVolume(0.2f, 0.2f) // 20% de volumen
        }
    }

    override fun restoreVolume() {
        if (!isMuted) {
            Log.d("AUDIO_DEBUG", "Restaurando volumen de música de fondo...")
            mediaPlayer?.setVolume(1.0f, 1.0f) // 100% de volumen
        }
    }

    override fun playBackgroundMusic() {
        Log.d("AUDIO_DEBUG", "playBackgroundMusic() llamado")
        try {
            if (mediaPlayer == null) {
                val assetPath = "composeResources/myapplication.composeapp.generated.resources/files/bg_music.mp3"
                Log.d("AUDIO_DEBUG", "Intentando cargar musica desde: $assetPath")
                val assetDescriptor = context.assets.openFd(assetPath)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(assetDescriptor.fileDescriptor, assetDescriptor.startOffset, assetDescriptor.length)
                    isLooping = true
                    // Listener para asegurar que el loop no se rompa
                    setOnCompletionListener { 
                        Log.d("AUDIO_DEBUG", "Música completada, reiniciando (Loop)...")
                        it.start() 
                    }
                    prepare()
                    if (isMuted) setVolume(0f, 0f) else setVolume(1f, 1f)
                    start()
                }
                Log.d("AUDIO_DEBUG", "✅ Musica de fondo iniciada")
            } else if (!mediaPlayer!!.isPlaying) {
                Log.d("AUDIO_DEBUG", "Resumiendo musica...")
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("AUDIO_DEBUG", "❌ ERROR al reproducir bg_music.mp3: ${e.message}")
            mediaPlayer = null // Limpiar para reintento
        }
    }

    override fun stopBackgroundMusic() {
        Log.d("AUDIO_DEBUG", "stopBackgroundMusic() llamado")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun pauseBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) {
            Log.d("AUDIO_DEBUG", "Pausando música para anuncio...")
            mediaPlayer?.pause()
        }
    }

    override fun resumeBackgroundMusic() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying && !isMuted) {
            Log.d("AUDIO_DEBUG", "Resumiendo música tras anuncio...")
            mediaPlayer?.start()
        }
    }

    override fun playTickSound() {
        if (tickSoundId != 0 && !isMuted) {
            Log.d("AUDIO_DEBUG", "Reproduciendo TICK...")
            val result = soundPool.play(tickSoundId, 1f, 1f, 1, 0, 1f)
            if (result == 0) Log.e("AUDIO_DEBUG", "❌ Fallo soundPool.play()")
        } else {
            if (tickSoundId == 0) Log.w("AUDIO_DEBUG", "Ignorando tick: tickSoundId es 0")
        }
    }

    override fun stopTickSound() {
        Log.d("AUDIO_DEBUG", "Deteniendo sonidos de TICK")
        soundPool.autoPause() // Detiene todos los sonidos activos en el pool
    }
}

actual fun getAudioManager(): AudioManager {
    val koin = object : KoinComponent {}
    val audioManager: AudioManager by koin.inject()
    return audioManager
}
