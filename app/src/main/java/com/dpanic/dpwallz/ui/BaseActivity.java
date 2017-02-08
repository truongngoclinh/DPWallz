package com.dpanic.dpwallz.ui;

import android.support.v7.app.AppCompatActivity;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpanic on 12/29/2016.
 * Project: DPWallz
 */

public class BaseActivity extends AppCompatActivity {

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    protected void onDestroy() {
        if (compositeSubscription != null) {
            compositeSubscription.clear();
        }
        super.onDestroy();
    }

    protected void addSubscription(Subscription subscription) {
        if (subscription == null) {
            return;
        }

        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
        }
        compositeSubscription.add(subscription);
    }
}
