package com.dpanic.dpwallz.di;

import android.app.Application;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import dagger.Module;
import dagger.Provides;

/**
 * Created by dpanic on 1/9/2017.
 * Project: DPWallz
 */

@Module
class AdModule {

    @Provides
    @ApplicationScope
    AdRequest provideAdRequest() {
        return new AdRequest.Builder().build();
    }
}
