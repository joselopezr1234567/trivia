package cl.jlopezr.trivia.core.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class IosAdsManager : AdsManager {
    override fun showInterstitial(onAdClosed: () -> Unit) {
        onAdClosed()
    }

    override fun showRewarded(onRewardEarned: (Int) -> Unit, onAdClosed: () -> Unit) {
        onAdClosed()
    }

    @Composable
    override fun BannerAd(modifier: Modifier) {
        // iOS implementation placeholder
    }
}

actual fun getAdsManager(): AdsManager {
    return IosAdsManager()
}
