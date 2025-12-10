package com.unbelievable.justfacts.kotlinmodule

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.unbelievable.justfacts.BuildConfig
import com.unbelievable.justfacts.R
import java.util.Date

class StoryApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    var count =0
    override fun onCreate() {
        registerActivityLifecycleCallbacks(this)
        super<Application>.onCreate()

        MobileAds.initialize(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            // Show the ad (if available) when the app moves to foreground.
            appOpenAdManager.showAdIfAvailable(it)
        }
    }

    // [END lifecycle_observer_events]

    /** ActivityLifecycleCallback methods. */
    // [START activity_lifecycle_callbacks]
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    // [END activity_lifecycle_callbacks]

    /**
     * Shows an app open ad.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }

    /**
     * Load an app open ad.
     *
     * @param activity the activity that shows the app open ad
     */
    fun loadAd(activity: Activity) {
        // We wrap the loadAd to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager.loadAd(activity)
    }

    /**
     * Interface definition for a callback to be invoked when an app open ad is complete (i.e.
     * dismissed or fails to show).
     */
    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    /** Inner class that loads and shows app open ads. */
    // [START manager_class]
    private inner class AppOpenAdManager {

        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
        private var loadTime: Long = 0

        // [END manager_class]

        /**
         * Load an ad.
         *
         * @param context the context of the activity that loads the ad
         */
        fun loadAd(context: Context) {

            if(count == 1)
            {
                // Do not load ad if there is an unused ad or one is already loading.
                if (isLoadingAd || isAdAvailable()) {
                    return
                }
                isLoadingAd = true
                // [START load_ad]
                AppOpenAd.load(
                    context,
                    BuildConfig.ADMOB_OPENAD_ID,
                    AdRequest.Builder().build(),
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            // Called when an app open ad has loaded.

                            appOpenAd = ad
                            isLoadingAd = false
                            loadTime = Date().time
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            // Called when an app open ad has failed to load.

                            isLoadingAd = false
                        }
                    },
                )
                // [END load_ad]
            }

        }

        // [START ad_expiration]
        /** Check if ad was loaded more than n hours ago. */
        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        /** Check if ad exists and can be shown. */
        private fun isAdAvailable(): Boolean {
            // For time interval details, see: https://support.google.com/admob/answer/9341964
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        // [END ad_expiration]

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(
                activity,
                object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        // Empty because the user will go back to the activity that shows the ad.
                    }
                },
            )
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
         */
        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                return
            }

            // If the app open ad is not available yet, invoke the callback.
            if (!isAdAvailable()) {
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }


            appOpenAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    /** Called when full screen content is dismissed. */
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.

                        count =1
                        appOpenAd = null
                        isShowingAd = false

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity)
                    }

                    /** Called when fullscreen content failed to show. */
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity)
                    }

                    /** Called when fullscreen content is shown. */
                    override fun onAdShowedFullScreenContent() {
                    }
                }
            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }

}