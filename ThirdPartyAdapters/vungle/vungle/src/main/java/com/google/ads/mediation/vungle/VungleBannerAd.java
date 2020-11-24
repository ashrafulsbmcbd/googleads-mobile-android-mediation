package com.google.ads.mediation.vungle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.vungle.mediation.VungleBannerAdapter;
import com.vungle.warren.VungleBanner;
import com.vungle.warren.VungleNativeAd;
import java.lang.ref.WeakReference;

/**
 * This class is used to represent a Vungle Banner ad.
 */
public class VungleBannerAd {

  private static final String TAG = VungleBannerAd.class.getSimpleName();

  /**
   * Weak reference to the adapter owning this Vungle banner ad.
   */
  private WeakReference<VungleBannerAdapter> adapter;

  /**
   * Vungle banner placement ID.
   */
  private String placementId;

  /**
   * Vungle ad object for non-MREC banner ads.
   */
  private VungleBanner vungleBanner;

  /**
   * Vungle ad object for MREC banner ads.
   */
  private VungleNativeAd vungleMRECBanner;

  public VungleBannerAd(@NonNull String placementId, @NonNull VungleBannerAdapter adapter) {
    this.placementId = placementId;
    this.adapter = new WeakReference<>(adapter);
  }

  @Nullable
  public VungleBannerAdapter getAdapter() {
    return this.adapter.get();
  }

  public void setVungleBanner(@NonNull VungleBanner vungleBanner) {
    this.vungleBanner = vungleBanner;
  }

  public void setVungleMRECBanner(@NonNull VungleNativeAd vungleMRECBanner) {
    this.vungleMRECBanner = vungleMRECBanner;
  }

  @Nullable
  public VungleBanner getVungleBanner() {
    return vungleBanner;
  }

  @Nullable
  public VungleNativeAd getVungleMRECBanner() {
    return vungleMRECBanner;
  }

  public void attach() {
    VungleBannerAdapter bannerAdapter = adapter.get();
    if (bannerAdapter == null) {
      return;
    }

    RelativeLayout layout = bannerAdapter.getAdLayout();
    if (layout == null) {
      return;
    }

    if (vungleBanner != null && vungleBanner.getParent() == null) {
      layout.addView(vungleBanner);
    }

    if (vungleMRECBanner != null) {
      View adView = vungleMRECBanner.renderNativeView();
      if (adView != null && adView.getParent() == null) {
        layout.addView(adView);
      }
    }
  }

  public void detach() {
    if (vungleBanner != null) {
      if (vungleBanner.getParent() != null) {
        ((ViewGroup) vungleBanner.getParent()).removeView(vungleBanner);
      }

      Log.d(TAG, "Vungle banner adapter cleanUp: destroyAd # " + vungleBanner.hashCode());
      vungleBanner.destroyAd();
      vungleBanner = null;
    }

    if (vungleMRECBanner != null) {
      View adView = vungleMRECBanner.renderNativeView();
      if (adView != null && adView.getParent() != null) {
        ((ViewGroup) adView.getParent()).removeView(adView);
      }

      Log.d(TAG,
          "Vungle banner adapter cleanUp: finishDisplayingAd # " + vungleMRECBanner.hashCode());
      vungleMRECBanner.finishDisplayingAd();
      vungleMRECBanner = null;
    }
  }
}
