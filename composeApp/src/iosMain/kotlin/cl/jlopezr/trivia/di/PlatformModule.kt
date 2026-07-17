package cl.jlopezr.trivia.di

import cl.jlopezr.trivia.core.ads.AdsManager
import cl.jlopezr.trivia.core.ads.IosAdsManager
import cl.jlopezr.trivia.core.audio.AudioManager
import cl.jlopezr.trivia.core.audio.IosAudioManager
import org.koin.dsl.module

actual val platformModule = module {
    single<AdsManager> { IosAdsManager() }
    single<AudioManager> { IosAudioManager() }
}
