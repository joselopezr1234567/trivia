package cl.jlopezr.trivia.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidAdsManager(private val context: Context) : AdsManager, KoinComponent {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    init {
        loadInterstitial()
        loadRewarded()
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d("AdMob", "Interstitial Ad Loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Log.e("AdMob", "Interstitial Ad Failed to Load: ${error.message}")
            }
        })
    }

    private fun loadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Log.d("AdMob", "Rewarded Ad Loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                Log.e("AdMob", "Rewarded Ad Failed to Load: ${error.message}")
            }
        })
    }

    override fun showInterstitial(onAdClosed: () -> Unit) {
        val activity = context as? Activity
        if (activity != null && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadInterstitial()
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            onAdClosed()
            loadInterstitial()
        }
    }

    override fun showRewarded(onRewardEarned: (Int) -> Unit, onAdClosed: () -> Unit) {
        val activity = context as? Activity
        if (activity != null && rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadRewarded()
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdClosed()
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                onRewardEarned(rewardItem.amount)
            }
        } else {
            onAdClosed()
            loadRewarded()
        }
    }

    @Composable
    override fun BannerAd(modifier: Modifier) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

actual fun getAdsManager(): AdsManager {
    val koin = object : KoinComponent {}
    val adsManager: AdsManager by koin.inject()
    return adsManager
}
