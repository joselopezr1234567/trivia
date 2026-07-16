package cl.jlopezr.trivia.core.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface AdsManager {
    fun showInterstitial(onAdClosed: () -> Unit)
    fun showRewarded(onRewardEarned: (Int) -> Unit, onAdClosed: () -> Unit)
    
    @Composable
    fun BannerAd(modifier: Modifier)
}

/**
 * Singleton placeholder or Koin provided instance
 */
expect fun getAdsManager(): AdsManager
