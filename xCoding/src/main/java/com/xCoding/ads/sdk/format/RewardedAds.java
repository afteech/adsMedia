package com.xCoding.ads.sdk.format;

import static com.xCoding.ads.sdk.util.Constant.ADCOLONY;
import static com.xCoding.ads.sdk.util.Constant.ADMOB;
import static com.xCoding.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.xCoding.ads.sdk.util.Constant.APPLOVIN;
import static com.xCoding.ads.sdk.util.Constant.MOPUB;
import static com.xCoding.ads.sdk.util.Constant.NONE;
import static com.xCoding.ads.sdk.util.Constant.STARTAPP;
import static com.xCoding.ads.sdk.util.Constant.UNITY;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.mopub.mobileads.MoPubRewardedAds;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;
import com.xCoding.ads.sdk.util.Tools;
import com.startapp.sdk.adsbase.StartAppAd;
import com.unity3d.ads.UnityAds;

import java.util.concurrent.TimeUnit;

public class RewardedAds {

    public static class Builder {

        private static final String TAG = "AdNetwork";
        private final Activity activity;
        private RewardedAd mRewardedAd;
        private StartAppAd startAppAd;
        private MaxRewardedAd maxRewardedAd;
        private AdColonyInterstitial rewardAdColony;
        private AdColonyInterstitialListener rewardListener;
        private AdColonyAdOptions rewardAdOptions;
        private static boolean isRewardLoaded;
        private int retryAttempt;
        private int counter = 1;

        private String adStatus = "";
        private String adNetwork = "";
        private String backupAdNetwork = "";
        private String adMobRewardedId = "";
        private String unityRewardedId = "";
        private String appLovinRewardedId = "";
        private String mopubRewardId = "";
        public static String adColonyRewardedId = "";
        private int placementStatus = 1;
        private int interval = 3;

        private boolean legacyGDPR = false;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public RewardedAds.Builder build() {
            loadRewardedAd();
            return this;
        }

        public void show() {
            showRewardedAd();
        }

        public RewardedAds.Builder setAdStatus(String adStatus) {
            this.adStatus = adStatus;
            return this;
        }

        public RewardedAds.Builder setAdNetwork(String adNetwork) {
            this.adNetwork = adNetwork;
            return this;
        }

        public RewardedAds.Builder setBackupAdNetwork(String backupAdNetwork) {
            this.backupAdNetwork = backupAdNetwork;
            return this;
        }

        public RewardedAds.Builder setAdMobRewardedId(String adMobRewardedId) {
            this.adMobRewardedId = adMobRewardedId;
            return this;
        }

        public RewardedAds.Builder setUnityRewardedId(String unityRewardedId) {
            this.unityRewardedId = unityRewardedId;
            return this;
        }

        public RewardedAds.Builder setAppLovinRewardedId(String appLovinRewardedId) {
            this.appLovinRewardedId = appLovinRewardedId;
            return this;
        }

        public RewardedAds.Builder setMopubRewardId(String mopubRewardId) {
            this.mopubRewardId = mopubRewardId;
            return this;
        }

        public RewardedAds.Builder setPlacementStatus(int placementStatus) {
            this.placementStatus = placementStatus;
            return this;
        }

        public RewardedAds.Builder setAdColonyRewardedId(String adColony_RewardedId) {
            adColonyRewardedId = adColony_RewardedId;
            return this;
        }

        public RewardedAds.Builder setInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public RewardedAds.Builder setLegacyGDPR(boolean legacyGDPR) {
            this.legacyGDPR = legacyGDPR;
            return this;
        }

        public void loadRewardedAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                switch (adNetwork) {
                    case ADMOB:
                        RewardedAd.load(activity, adMobRewardedId, Tools.getAdRequest(activity, legacyGDPR), new RewardedAdLoadCallback() {
                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        Log.d(TAG, loadAdError.getMessage());
                                        mRewardedAd = null;
                                        loadBackupInterstitialAd();
                                        Log.d(TAG, "Failed load AdMob Rewarded Ad");

                                    }
                                    @Override
                                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                        mRewardedAd = rewardedAd;
                                        Log.d(TAG, "Ad was loaded.");
                                    }
                                });
                        break;

                    case STARTAPP:
                        startAppAd = new StartAppAd(activity);
                        break;
                    case APPLOVIN:
                        maxRewardedAd =  MaxRewardedAd.getInstance(appLovinRewardedId, activity);
                        maxRewardedAd.setListener(new MaxRewardedAdListener() {
                            @Override
                            public void onAdLoaded(final MaxAd maxAd) {
                                retryAttempt = 0;
                                Log.d(TAG, "AppLovin Reward Ad loaded...");
                            }

                            @Override
                            public void onAdLoadFailed(final String adUnitId, final MaxError error) {

                                retryAttempt++;
                                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                                new Handler().postDelayed(() -> maxRewardedAd.loadAd(), delayMillis);
                                Log.d(TAG, "failed to load AppLovin Interstitial");
                                loadBackupInterstitialAd();

                            }

                            @Override
                            public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {

                                maxRewardedAd.loadAd();
                            }

                            @Override
                            public void onAdDisplayed( MaxAd maxAd) {}

                            @Override
                            public void onAdClicked( MaxAd maxAd) {}

                            @Override
                            public void onAdHidden( MaxAd maxAd) {

                                maxRewardedAd.loadAd();
                            }

                            @Override
                            public void onRewardedVideoStarted( MaxAd maxAd) {}

                            @Override
                            public void onRewardedVideoCompleted( MaxAd maxAd) {}

                            @Override
                            public void onUserRewarded( MaxAd maxAd, MaxReward maxReward) {}

                        });

                        maxRewardedAd.loadAd();
                        break;

                    case MOPUB:
                        MoPubRewardedAds.loadRewardedAd(mopubRewardId);
                        break;
                    case ADCOLONY:
                        AdColony.setRewardListener(new AdColonyRewardListener() {
                            @Override
                            public void onReward(AdColonyReward reward) {
                                // Query reward object for info here
                                Log.d(TAG, "onReward");
                            }
                        });
                        rewardListener = new AdColonyInterstitialListener() {
                            @Override
                            public void onRequestFilled(AdColonyInterstitial adReward) {
                                rewardAdColony = adReward;
                                isRewardLoaded = true;

                                Log.d(TAG, "onRequestFilled");
                            }

                            @Override
                            public void onRequestNotFilled(AdColonyZone zone) {
                                Log.d(TAG, "onRequestNotFilled");
                                loadBackupInterstitialAd();

                            }

                            @Override
                            public void onOpened(AdColonyInterstitial ad) {
                                super.onOpened(ad);
                                Log.d(TAG, "onOpened");
                            }

                            @Override
                            public void onClosed(AdColonyInterstitial ad) {
                                super.onClosed(ad);
                                rewardAdColony = ad;
                                isRewardLoaded = true;
                                AdColony.requestInterstitial(adColonyRewardedId, rewardListener, rewardAdOptions);
                            }

                            @Override
                            public void onClicked(AdColonyInterstitial ad) {
                                super.onClicked(ad);
                            }

                            @Override
                            public void onLeftApplication(AdColonyInterstitial ad) {
                                super.onLeftApplication(ad);
                            }

                            @Override
                            public void onExpiring(AdColonyInterstitial ad) {
                                super.onExpiring(ad);
                                AdColony.requestInterstitial(adColonyRewardedId, rewardListener, rewardAdOptions);
                                Log.d(TAG, "onExpiring");
                            }
                        };
                        rewardAdOptions = new AdColonyAdOptions().enableConfirmationDialog(false).enableResultsDialog(false);
                        AdColony.requestInterstitial(adColonyRewardedId, rewardListener, rewardAdOptions);
                        break;
                }
            }
        }

        public void loadBackupInterstitialAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                switch (backupAdNetwork) {
                    case ADMOB:
                        RewardedAd.load(activity, adMobRewardedId, Tools.getAdRequest(activity, legacyGDPR), new RewardedAdLoadCallback() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.d(TAG, loadAdError.getMessage());
                                mRewardedAd = null;
                                Log.d(TAG, "Failed load AdMob Rewarded Ad");

                            }
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                mRewardedAd = rewardedAd;
                                Log.d(TAG, "Ad was loaded.");
                            }
                        });
                        break;

                    case STARTAPP:
                        startAppAd = new StartAppAd(activity);
                        break;
                    case APPLOVIN:
                        maxRewardedAd =  MaxRewardedAd.getInstance(appLovinRewardedId, activity);
                        maxRewardedAd.setListener(new MaxRewardedAdListener() {
                            @Override
                            public void onAdLoaded(final MaxAd maxAd) {
                                retryAttempt = 0;
                                Log.d(TAG, "AppLovin Reward Ad loaded...");
                            }

                            @Override
                            public void onAdLoadFailed(final String adUnitId, final MaxError error) {

                                retryAttempt++;
                                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                                new Handler().postDelayed(() -> maxRewardedAd.loadAd(), delayMillis);
                                Log.d(TAG, "failed to load AppLovin Interstitial");

                            }

                            @Override
                            public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {

                                maxRewardedAd.loadAd();
                            }

                            @Override
                            public void onAdDisplayed( MaxAd maxAd) {}

                            @Override
                            public void onAdClicked( MaxAd maxAd) {}

                            @Override
                            public void onAdHidden( MaxAd maxAd) {

                                maxRewardedAd.loadAd();
                            }

                            @Override
                            public void onRewardedVideoStarted( MaxAd maxAd) {}

                            @Override
                            public void onRewardedVideoCompleted( MaxAd maxAd) {}

                            @Override
                            public void onUserRewarded( MaxAd maxAd, MaxReward maxReward) {}

                        });

                        maxRewardedAd.loadAd();
                        break;

                    case MOPUB:
                        MoPubRewardedAds.loadRewardedAd(mopubRewardId);
                        break;
                    case ADCOLONY:
                        AdColony.setRewardListener(new AdColonyRewardListener() {
                            @Override
                            public void onReward(AdColonyReward reward) {
                                // Query reward object for info here
                                Log.d(TAG, "onReward");
                            }
                        });
                        rewardListener = new AdColonyInterstitialListener() {
                            @Override
                            public void onRequestFilled(AdColonyInterstitial adReward) {
                                rewardAdColony = adReward;
                                isRewardLoaded = true;

                                Log.d(TAG, "onRequestFilled");
                            }

                            @Override
                            public void onRequestNotFilled(AdColonyZone zone) {
                                Log.d(TAG, "onRequestNotFilled");

                            }

                            @Override
                            public void onOpened(AdColonyInterstitial ad) {
                                super.onOpened(ad);
                                Log.d(TAG, "onOpened");
                            }

                            @Override
                            public void onClosed(AdColonyInterstitial ad) {
                                super.onClosed(ad);
                                rewardAdColony = ad;
                                isRewardLoaded = true;
                                AdColony.requestInterstitial(adColonyRewardedId, rewardListener, rewardAdOptions);
                            }

                            @Override
                            public void onClicked(AdColonyInterstitial ad) {
                                super.onClicked(ad);
                            }

                            @Override
                            public void onLeftApplication(AdColonyInterstitial ad) {
                                super.onLeftApplication(ad);
                            }

                            @Override
                            public void onExpiring(AdColonyInterstitial ad) {
                                super.onExpiring(ad);
                                AdColony.requestInterstitial(adColonyRewardedId, rewardListener, rewardAdOptions);
                                Log.d(TAG, "onExpiring");
                            }
                        };
                        rewardAdOptions = new AdColonyAdOptions().enableConfirmationDialog(false).enableResultsDialog(false);
                        AdColony.requestInterstitial(adColonyRewardedId, rewardListener, rewardAdOptions);
                        break;
                    case NONE:
                        //do nothing
                        break;
                }
            }
        }

        public void showRewardedAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                if (counter == interval) {
                    switch (adNetwork) {
                    case ADMOB:
                        if (mRewardedAd != null) {
                            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    Log.d(TAG, "admob show");
                                }
                            });
                            Log.d(TAG, "admob reward not null");
                        } else {
                            showBackupInterstitialAd();
                            Log.d(TAG, "admob reward null");
                        }
                        break;

                    case STARTAPP:
                        if (startAppAd != null) {
                            startAppAd.showAd();
                            Log.d(TAG, "startapp reward not null [counter] : " + counter);
                        } else {
                            showBackupInterstitialAd();
                            Log.d(TAG, "startapp reward null");
                        }
                        break;

                    case UNITY:
                        if (UnityAds.isReady(unityRewardedId)) {
                            UnityAds.show(activity, unityRewardedId);
                            Log.d(TAG, "unity reward not null [counter] : " + counter);
                        } else {
                            showBackupInterstitialAd();
                            Log.d(TAG, "unity reward null");
                        }
                        break;

                    case APPLOVIN:
                        if (maxRewardedAd.isReady()) {
                            maxRewardedAd.showAd();
                            Log.d(TAG, "applovin reward not null [counter] : " + counter);
                        } else {
                            showBackupInterstitialAd();
                            Log.d(TAG, "applovin reward null");
                        }
                        break;
                    case MOPUB:
                        MoPubRewardedAds.loadRewardedAd(mopubRewardId);
                        MoPubRewardedAds.showRewardedAd(mopubRewardId);

                        if (counter == interval) {
                            MoPubRewardedAds.loadRewardedAd(mopubRewardId);
                            MoPubRewardedAds.showRewardedAd(mopubRewardId);
                            Log.d(TAG, "mopub reward not null [counter] : " + counter);
                            counter = 1;
                        } else {
                            counter++;
                            showBackupInterstitialAd();
                            Log.d(TAG, "mopub reward null");
                        }
                        break;
                    case ADCOLONY:
                        if (rewardAdColony != null && isRewardLoaded) {
                            rewardAdColony.show();
                            isRewardLoaded = false;
                            Log.d(TAG, "adcolony reward not null [counter] : " + counter);
                        } else {
                            showBackupInterstitialAd();
                            Log.d(TAG, "adcolony reward null");
                        }
                        break;
                }
                counter = 1;
            } else {
                counter++;
            }
            Log.d(TAG, "Current counter : " + counter);
            }
        }

        public void showBackupInterstitialAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                Log.d(TAG, "Show Backup Interstitial Ad [" + backupAdNetwork.toUpperCase() + "]");
                switch (backupAdNetwork) {
                        case ADMOB:
                            if (mRewardedAd != null) {
                                mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                        Log.d(TAG, "admob show");
                                    }
                                });
                            }
                            break;

                        case STARTAPP:
                            if (startAppAd != null) {
                                startAppAd.showAd();
                            }
                            break;

                        case UNITY:
                            if (UnityAds.isReady(unityRewardedId)) {
                                UnityAds.show(activity, unityRewardedId);
                            }
                            break;

                        case APPLOVIN:
                            if (maxRewardedAd.isReady()) {
                                maxRewardedAd.showAd();
                            }
                            break;
                        case MOPUB:
                            MoPubRewardedAds.loadRewardedAd(mopubRewardId);
                            MoPubRewardedAds.showRewardedAd(mopubRewardId);
                            break;
                        case ADCOLONY:
                            if (rewardAdColony != null && isRewardLoaded) {
                                rewardAdColony.show();
                                isRewardLoaded = false;
                            }
                            break;
                    case NONE:
                        //do nothing
                        break;
                    }
            }
        }


    }

}
