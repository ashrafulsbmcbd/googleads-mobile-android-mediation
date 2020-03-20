package com.applovin.mediation;

import static com.applovin.mediation.AppLovinNativeAdapter.TAG;

import android.content.Context;
import android.util.Log;
import com.applovin.nativeAds.AppLovinNativeAd;
import com.applovin.nativeAds.AppLovinNativeAdLoadListener;
import com.applovin.nativeAds.AppLovinNativeAdPrecacheListener;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationNativeListener;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import java.lang.ref.WeakReference;
import java.util.List;

class AppLovinNativeAdListener
    implements AppLovinNativeAdLoadListener, AppLovinNativeAdPrecacheListener {

  private final AppLovinNativeAdapter mAdapter;
  private final MediationNativeListener mNativeListener;
  private final AppLovinSdk mSdk;
  private final WeakReference<Context> mContextWeakReference;
  private final NativeMediationAdRequest mMediationAdRequest;

  public AppLovinNativeAdListener(AppLovinNativeAdapter adapter,
      MediationNativeListener nativeListener,
      AppLovinSdk sdk,
      Context context,
      NativeMediationAdRequest mediationAdRequest) {
    mAdapter = adapter;
    mNativeListener = nativeListener;
    mSdk = sdk;
    mContextWeakReference = new WeakReference<>(context);
    mMediationAdRequest = mediationAdRequest;
  }

  @Override
  public void onNativeAdsLoaded(List<AppLovinNativeAd> nativeAds) {
    if (nativeAds.size() > 0 && isValidNativeAd(nativeAds.get(0))) {
      mSdk.getNativeAdService().precacheResources(nativeAds.get(0), this);
    } else {
      Log.e(TAG,
          "Ad from AppLovin doesn't have all assets required for the app install ad format");
      notifyAdFailure(AdRequest.ERROR_CODE_NO_FILL);
    }
  }

  @Override
  public void onNativeAdsFailedToLoad(final int errorCode) {
    Log.e(TAG, "Native ad failed to load " + errorCode);
    notifyAdFailure(AppLovinUtils.toAdMobErrorCode(errorCode));
  }

  @Override
  public void onNativeAdImagesPrecached(AppLovinNativeAd ad) {
    // Create a native ad.
    Context context = mContextWeakReference.get();
    if (context == null) {
      Log.e(TAG, "Failed to create mapper. Context is null.");
      notifyAdFailure(AdRequest.ERROR_CODE_INTERNAL_ERROR);
      return;
    }
    if (mMediationAdRequest.isUnifiedNativeAdRequested()) {
      final AppLovinUnifiedNativeAdMapper mapper = new AppLovinUnifiedNativeAdMapper(context,
          ad);
      Log.d(TAG, "UnifiedNativeAd loaded.");
      AppLovinSdkUtils.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mNativeListener.onAdLoaded(mAdapter, mapper);
        }
      });
    } else if (mMediationAdRequest.isAppInstallAdRequested()) {
      final AppLovinNativeAdMapper mapper = new AppLovinNativeAdMapper(ad, context);
      Log.d(TAG, "AppInstallAd loaded.");
      AppLovinSdkUtils.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mNativeListener.onAdLoaded(mAdapter, mapper);
        }
      });
    }
  }

  @Override
  public void onNativeAdVideoPreceached(AppLovinNativeAd ad) {
    // Do nothing.
  }

  @Override
  public void onNativeAdImagePrecachingFailed(AppLovinNativeAd ad, final int errorCode) {
    Log.e(TAG, "Native ad failed to pre cache images " + errorCode);
    notifyAdFailure(AppLovinUtils.toAdMobErrorCode(errorCode));
  }

  @Override
  public void onNativeAdVideoPrecachingFailed(AppLovinNativeAd ad, final int errorCode) {
    // Do nothing.
  }

  /**
   * Sends a failure callback to {@link #mNativeListener}.
   *
   * @param errorCode AdMob {@link AdRequest} error code.
   */
  private void notifyAdFailure(final int errorCode) {
    AppLovinSdkUtils.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mNativeListener.onAdFailedToLoad(mAdapter, errorCode);
      }
    });
  }

  /**
   * Checks whether or not the {@link AppLovinNativeAd} has all the required assets.
   *
   * @param nativeAd AppLovin native ad.
   * @return {@code true} if the native ad has all the required assets.
   */
  private static boolean isValidNativeAd(AppLovinNativeAd nativeAd) {
    return nativeAd.getImageUrl() != null && nativeAd.getIconUrl() != null &&
        nativeAd.getTitle() != null && nativeAd.getDescriptionText() != null &&
        nativeAd.getCtaText() != null;
  }
}