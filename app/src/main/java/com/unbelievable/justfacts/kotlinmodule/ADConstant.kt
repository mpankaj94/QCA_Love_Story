package com.unbelievable.justfacts.kotlinmodule

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.unbelievable.justfacts.BuildConfig
import com.unbelievable.justfacts.R

object ADConstant {


    var mInterstitialAd: InterstitialAd? = null

    lateinit var dCallBack: CallBack

    interface CallBack {
        fun MoveToNext()
    }

    public fun ShowAdmobInsterstitial(activity: Activity, callBack: CallBack) {
        dCallBack = callBack
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    LoadAdmobInsterstitialAd(activity)
                    dCallBack?.MoveToNext()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    dCallBack?.MoveToNext()
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                }
            }
        } else
            dCallBack?.MoveToNext()

    }

    public fun LoadAdmobInsterstitialAd(activity: Activity) {

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, BuildConfig.ADMOB_FULL_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    mInterstitialAd = p0
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }

}