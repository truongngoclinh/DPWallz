package com.dpanic.dpwallz.busevent;

import com.dpanic.dpwallz.model.Category;

/**
 * Created by dpanic on 10/12/2016.
 * Project: DPWallz
 */

public class OpenCategoryEvent {
    private Category category;
    private boolean isColor;

    public OpenCategoryEvent(Category category, boolean isColor) {
        this.category = category;
        this.isColor = isColor;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isColor() {
        return isColor;
    }
}
