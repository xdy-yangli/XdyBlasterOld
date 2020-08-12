package com.example.xdyblaster.util;

public class UuidData {
    public String uuid;
    public int area;
    public int num;
    public int delay;
    public long id;

    public UuidData() {
        this.uuid = "";
        this.area = 0;
        this.num = 0;
        this.delay = 0;
        this.id = 0;
    }

    public UuidData(String uuid, int area, int num, int delay, long id) {
        this.uuid = uuid;
        this.area = area;
        this.num = num;
        this.delay = delay;
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public int getArea() {
        return area;
    }

    public int getNum() {
        return num;
    }

    public int getDelay() {
        return delay;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
