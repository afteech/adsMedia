package com.xCoding.ads.sdk.format;

import static com.xCoding.ads.sdk.format.BannerAd.Builder.adColonyBannerId;
import static com.xCoding.ads.sdk.format.InterstitialAd.Builder.adColonyInterstitialId;
import static com.xCoding.ads.sdk.format.RewardedAds.Builder.adColonyRewardedId;
import static com.xCoding.ads.sdk.util.Constant.ADCOLONY;
import static com.xCoding.ads.sdk.util.Constant.ADMOB;
import static com.xCoding.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.xCoding.ads.sdk.util.Constant.APPLOVIN;
import static com.xCoding.ads.sdk.util.Constant.MOPUB;
import static com.xCoding.ads.sdk.util.Constant.STARTAPP;
import static com.xCoding.ads.sdk.util.Constant.UNITY;

import android.app.Activity;
import android.util.Log;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.FacebookBanner;
import com.xCoding.ads.sdk.helper.AudienceNetworkInitializeHelper;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdNetwork {

    public static class Initialize {

        private static final String TAG = "AdNetwork";
        Activity activity;
        private String adStatus = "";
        private String adNetwork = "";
        private String adMobAppId = "";
        private String startappAppId = "0";
        private String unityGameId = "";
        private String appLovinSdkKey = "";
        private String mopubBannerId = "";
        private String adColonyId = "";
        private boolean debug = true;

        public Initialize(Activity activity) {
            this.activity = activity;
        }

        public AdNetwork.Initialize build() {
            initAds();
            return this;
        }

        public AdNetwork.Initialize setAdStatus(String adStatus) {
            this.adStatus = adStatus;
            return this;
        }

        public AdNetwork.Initialize setAdNetwork(String adNetwork) {
            this.adNetwork = adNetwork;
            return this;
        }

        public AdNetwork.Initialize setAdMobAppId(String adMobAppId) {
            this.adMobAppId = adMobAppId;
            return this;
        }

        public AdNetwork.Initialize setStartappAppId(String startappAppId) {
            this.startappAppId = startappAppId;
            return this;
        }

        public AdNetwork.Initialize setUnityGameId(String unityGameId) {
            this.unityGameId = unityGameId;
            return this;
        }

        public AdNetwork.Initialize setAdColonyId(String adColonyId) {
            this.adColonyId = adColonyId;
            return this;
        }

        public AdNetwork.Initialize setAppLovinSdkKey(String appLovinSdkKey) {
            this.appLovinSdkKey = appLovinSdkKey;
            return this;
        }

        public AdNetwork.Initialize setMopubBannerId(String mopubBannerId) {
            this.mopubBannerId = mopubBannerId;
            return this;
        }

        public AdNetwork.Initialize setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public void initAds() {
            if (adStatus.equals(AD_STATUS_ON)) {
                switch (adNetwork) {
                    case ADMOB:
                        MobileAds.initialize(activity, initializationStatus -> {
                            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                            for (String adapterClass : statusMap.keySet()) {
                                AdapterStatus adapterStatus = statusMap.get(adapterClass);
                                assert adapterStatus != null;
                                Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, adapterStatus.getDescription(), adapterStatus.getLatency()));
                                Log.d(TAG, "FAN open bidding with AdMob as mediation partner selected");
                            }
                        });
                        AudienceNetworkInitializeHelper.initialize(activity);
                        Log.d(TAG, "AdMob App ID is : " + adMobAppId);
                        break;
                    case STARTAPP:
                        StartAppSDK.init(activity, startappAppId, false);
                        StartAppSDK.setTestAdsEnabled(debug);
                        StartAppAd.disableSplash();
                        StartAppSDK.setUserConsent(activity, "pas", System.currentTimeMillis(), true);
                        break;
                    case UNITY:
                        UnityAds.addListener(new IUnityAdsListener() {
                            @Override
                            public void onUnityAdsReady(String adUnitId) {
                                // Implement functionality for an ad being ready to show.
                                Log.d(TAG, "Unity Ads Placement ID : " + adUnitId);
                            }

                            @Override
                            public void onUnityAdsStart(String adUnitId) {
                                // Implement functionality for a user starting to watch an ad.
                            }

                            @Override
                            public void onUnityAdsFinish(String adUnitId, UnityAds.FinishState finishState) {
                                // Implement functionality for a user finishing an ad.
                            }

                            @Override
                            public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
                                // Implement functionality for a Unity Ads service error occurring.
                            }
                        });

                        UnityAds.initialize(activity.getApplicationContext(), unityGameId, debug, new IUnityAdsInitializationListener() {
                            @Override
                            public void onInitializationComplete() {
                                Log.d(TAG, "Unity Ads Initialization Complete");
                                Log.d(TAG, "Unity Ads Game ID : " + unityGameId);
                            }

                            @Override
                            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                                Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                            }
                        });
                        break;
                    case APPLOVIN:
                        AppLovinSdk.getInstance(activity).setMediationProvider(AppLovinMediationProvider.MAX);
                        AppLovinSdk.getInstance(activity).initializeSdk(config -> {
                        });
                        AudienceNetworkInitializeHelper.initialize(activity);
                        final String sdkKey = AppLovinSdk.getInstance(activity).getSdkKey();
                        if (!sdkKey.equals(appLovinSdkKey)) {
                            Log.e(TAG, "ERROR : Please update your applovin sdk key in the manifest file.");
                        }
                        AppLovinSdk.getInstance(activity).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("1cce5ded-cd2f-489e-97be-cca078ea751b"));

                        Log.d(TAG, "AppLovin SDK Key is : " + sdkKey);
                        break;

                    case MOPUB:
                        Map<String, String> facebookBanner = new HashMap<>();
                        facebookBanner.put("native_banner", "true");
                        SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder(mopubBannerId);
                        configBuilder.withMediatedNetworkConfiguration(FacebookBanner.class.getName(), facebookBanner);
                        MoPub.initializeSdk(activity, configBuilder.build(), initSdkListener());
                        break;
                    case ADCOLONY:
                        AdColonyAppOptions appOptions = new AdColonyAppOptions().setKeepScreenOn(true);
                        AdColony.configure(activity, appOptions, adColonyId, adColonyBannerId, adColonyInterstitialId, adColonyRewardedId);
                        break;
                }
            }
        }

        private static SdkInitializationListener initSdkListener() {
            return () -> {
            };
        }

    }

}
