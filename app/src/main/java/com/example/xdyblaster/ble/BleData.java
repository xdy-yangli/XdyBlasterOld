package com.example.xdyblaster.ble;

import java.io.Serializable;

public class BleData implements Serializable {
    private String name;
    private String mac;
    private String rssi;

    public BleData(String name,String mac,String rssi)
    {
        this.name=name;
        this.mac=mac;
        this.rssi=rssi;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public String getRssi() {
        return rssi;
    }
}
