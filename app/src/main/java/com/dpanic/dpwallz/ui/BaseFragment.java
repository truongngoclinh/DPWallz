package com.dpanic.dpwallz.ui;

import android.support.v4.app.Fragment;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpanic on 12/29/2016.
 * Project: DPWallz
 */

public class BaseFragment extends Fragment {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (compositeSubscription != null) {
            compositeSubscription.clear();
        }
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
