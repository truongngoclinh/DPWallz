package com.dpanic.dpwallz.di;

import android.app.Application;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import dagger.Module;
import dagger.Provides;

/**
 * Created by dpanic on 1/9/2017.
 * Project: DPWallz
 */

@Module
class FirebaseModule {

    @Provides
    @ApplicationScope
    FirebaseAnalytics provideAnalytics(Application application) {
        return FirebaseAnalytics.getInstance(application);
    }

    @Provides
    @ApplicationScope
    FirebaseMessaging provideMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
