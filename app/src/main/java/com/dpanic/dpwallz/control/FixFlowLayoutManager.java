package com.dpanic.dpwallz.control;

import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

/**
 * Created by dpanic on 10/7/2016.
 * Project: DPWallz
 */

public class FixFlowLayoutManager extends FlowLayoutManager {

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }
}
