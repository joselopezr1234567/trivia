package cl.jlopezr.trivia.di

import cl.jlopezr.trivia.core.ads.AdsManager
import cl.jlopezr.trivia.core.ads.AndroidAdsManager
import org.koin.dsl.module

actual val platformModule = module {
    single<AdsManager> { AndroidAdsManager(get()) }
}
