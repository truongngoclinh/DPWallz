package com.dpanic.dpwallz.di;

import org.greenrobot.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;

/**
 * Created by dpanic on 1/9/2017.
 * Project: DPWallz
 */

@Module
class EventBusModule {

    @Provides
    @ApplicationScope
    EventBus provideEventBus() {
        return EventBus.getDefault();
    }
}
