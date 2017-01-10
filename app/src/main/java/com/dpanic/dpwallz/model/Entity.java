package com.dpanic.dpwallz.model;


/**
 * Created by dpanic on 9/29/2016.
 * Project: DPWallz
 */

class Entity {
    private long id;

    Entity() {
        this.id = -1;
    }

    Entity(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
