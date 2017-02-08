package com.dpanic.dpwallz.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.dpanic.dpwallz.model.DataManager;
import dagger.Module;
import dagger.Provides;

/**
 * Created by dpanic on 1/9/2017.
 * Project: DPWallz
 */

@Module
class LocalDataModule {

    @Provides
    @ApplicationScope
    DataManager provideDataManager(Application application) {
        return DataManager.getInstance(application);
    }

    @Provides
    @ApplicationScope
    SharedPreferences provideSharedPref(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }
}
