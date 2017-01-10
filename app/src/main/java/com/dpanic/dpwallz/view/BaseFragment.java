package com.dpanic.dpwallz.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.dpanic.dpwallz.di.DPWallzComponent;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpanic on 12/29/2016.
 * Project: DPWallz
 */

public class BaseFragment extends Fragment {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
        super.onDestroy();
    }
}
