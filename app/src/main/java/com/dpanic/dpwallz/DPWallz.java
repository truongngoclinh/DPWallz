package com.dpanic.dpwallz;

import android.app.Activity;
import android.app.Application;
import com.dpanic.dpwallz.di.AppModule;
import com.dpanic.dpwallz.di.DPWallzComponent;
import com.dpanic.dpwallz.di.DaggerDPWallzComponent;
import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.LeakCanary;

//import com.facebook.stetho.Stetho;
//import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

//import io.realm.Realm;
//import io.realm.RealmConfiguration;

/**
 * Created by dpanic on 10/6/2016.
 * Project: DPWallz
 */

public class DPWallz extends Application {

    private DPWallzComponent component;

    public static DPWallz get(Activity activity) {
        return (DPWallz) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

//        Realm.init(this);
//        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
//        Realm.setDefaultConfiguration(realmConfig);
//        if (BuildConfig.DEBUG) {
//            Stetho.initialize(
//                    Stetho.newInitializerBuilder(this)
//                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                            .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build()).build());
//        }

        component = DaggerDPWallzComponent.builder().appModule(new AppModule(this)).build();
    }

    public DPWallzComponent component() {
        return component;
    }
}
