package com.dpanic.dpwallz.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.util.Constants;

/**
 * Created by dpanic on 29/09/2016.
 * Project: DPWallz
 */

class ExploreFragmentAdapter extends FragmentPagerAdapter {

    private String[] tabTitle;

    ExploreFragmentAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);

        tabTitle = new String[] { context.getResources().getString(R.string.string_category),
                context.getResources().getString(R.string.string_recent),
                context.getResources().getString(R.string.string_month_popular),
                context.getResources().getString(R.string.string_all_time_popular) };
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
        case 0:
            fragment = new CategoryFragment();
            break;
        case 1:
            fragment = ImageListFragment.newInstance(Constants.FRAG_TYPE_RECENT, Constants.MAIN_LINK_RECENT);
            break;
        case 2:
            fragment =
                    ImageListFragment.newInstance(Constants.FRAG_TYPE_MONTH_POPULAR, Constants.MAIN_LINK_MONTH_POPULAR);
            break;
        case 3:
            fragment = ImageListFragment
                    .newInstance(Constants.FRAG_TYPE_ALL_TIME_POPULAR, Constants.MAIN_LINK_ALL_TIME_POPULAR);
            break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return tabTitle.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }
}
