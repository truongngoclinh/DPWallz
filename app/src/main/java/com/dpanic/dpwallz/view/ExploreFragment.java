package com.dpanic.dpwallz.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dpanic.dpwallz.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dpanic on 9/23/2016.
 * Project: DPWallz
 */

public class ExploreFragment extends BaseFragment {

    @BindView(R.id.explore_viewpager)
    ViewPager exploreViewPager;

    private MainActivity mMainActivity;
    private TabLayout mTabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_explore_layout, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        exploreViewPager.setAdapter(new ExploreFragmentAdapter(mMainActivity, getChildFragmentManager()));
        exploreViewPager.setOffscreenPageLimit(3);
        exploreViewPager.setCurrentItem(1);

        mTabLayout = (TabLayout) mMainActivity.findViewById(R.id.main_tab_layout);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(exploreViewPager);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(exploreViewPager);
        }
    }
}
