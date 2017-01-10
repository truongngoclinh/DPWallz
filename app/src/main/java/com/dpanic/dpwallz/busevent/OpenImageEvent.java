package com.dpanic.dpwallz.busevent;

import com.dpanic.dpwallz.model.Image;

/**
 * Created by dpanic on 9/30/2016.
 * Project: DPWallz
 */

public class OpenImageEvent {
    private Image img;

    public OpenImageEvent(Image img) {
        this.img = img;
    }

    public Image getImage() {
        return img;
    }
}
