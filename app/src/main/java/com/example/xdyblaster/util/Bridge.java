package com.example.xdyblaster.util;

public class Bridge {
    private String uid;
    private long id;
    private float cap;
    private float bridge;

    public Bridge() {
    }

    public Bridge(String uid, long id, float cap, float bridge) {
        this.uid = uid;
        this.id = id;
        this.cap = cap;
        this.bridge = bridge;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getCap() {
        return cap;
    }

    public void setCap(float cap) {
        this.cap = cap;
    }

    public float getBridge() {
        return bridge;
    }

    public void setBridge(float bridge) {
        this.bridge = bridge;
    }
}
