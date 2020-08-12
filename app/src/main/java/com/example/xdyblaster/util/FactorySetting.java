package com.example.xdyblaster.util;

import android.content.Context;

public class FactorySetting {
    private int currMin;
    private int capMin;
    private int bridgeMin;
    private int freqMin;
    private int subMin;
    private int voltMin;
    private int cdStep;
    private int currMax, capMax, bridgeMax, freqMax, subMax, voltMax, cdTime;
    private int testWhich;
    private int testCount;
    private Context context;

    public FactorySetting(Context context) {
        this.context = context;
        reloadData();

    }

    public void reloadData() {
        currMin = (int) SharedPreferencesUtils.getParam(context, "currMin", 0);
        currMax = (int) SharedPreferencesUtils.getParam(context, "currMax", 10);
        capMin = (int) SharedPreferencesUtils.getParam(context, "capMin", 80);
        capMax = (int) SharedPreferencesUtils.getParam(context, "capMax", 120);
        bridgeMin = (int) SharedPreferencesUtils.getParam(context, "bridgeMin", 1);
        bridgeMax = (int) SharedPreferencesUtils.getParam(context, "bridgeMax", 5);
        freqMin = (int) SharedPreferencesUtils.getParam(context, "freqMin", 480);
        freqMax = (int) SharedPreferencesUtils.getParam(context, "freqMax", 520);
        subMin = (int) SharedPreferencesUtils.getParam(context, "subMin", 30);
        subMax = (int) SharedPreferencesUtils.getParam(context, "subMax", 50);
        voltMin = (int) SharedPreferencesUtils.getParam(context, "voltMin", 20);
        voltMax = (int) SharedPreferencesUtils.getParam(context, "voltMax", 24);
        cdStep = (int) SharedPreferencesUtils.getParam(context, "cdStep", 5);
        cdTime = (int) SharedPreferencesUtils.getParam(context, "cdTime", 20);
        testWhich = (int) SharedPreferencesUtils.getParam(context, "testWhich", 0x07f);
        testCount = (int) SharedPreferencesUtils.getParam(context, "testCount", 20);
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
        SharedPreferencesUtils.setParam(context, "testCount", testCount);
    }

    public void setData(int n, int d1, int d2) {
        switch (n) {
            case 0:
                setCurrMin(d1);
                setCurrMax(d2);
                break;
            case 1:
                setCapMin(d1);
                setCapMax(d2);
                break;
            case 2:
                setBridgeMin(d1);
                setBridgeMax(d2);
                break;
            case 3:
                setFreqMin(d1);
                setFreqMax(d2);
                break;
            case 4:
                setSubMin(d1);
                setSubMax(d2);
                break;
            case 5:
                setVoltMin(d1);
                setVoltMax(d2);
                break;
            case 6:
                setCdStep(d1);
                setCdTime(d2);
                break;
        }
    }

    public int getCurrMin() {
        return currMin;
    }

    public void setCurrMin(int currMin) {
        this.currMin = currMin;
        SharedPreferencesUtils.setParam(context, "currMin", currMin);

    }

    public int getCapMin() {
        return capMin;
    }

    public void setCapMin(int capMin) {
        SharedPreferencesUtils.setParam(context, "capMin", capMin);
        this.capMin = capMin;
    }

    public int getBridgeMin() {
        return bridgeMin;
    }

    public void setBridgeMin(int bridgeMin) {
        SharedPreferencesUtils.setParam(context, "bridgeMin", bridgeMin);
        this.bridgeMin = bridgeMin;
    }

    public int getFreqMin() {
        return freqMin;
    }

    public void setFreqMin(int freqMin) {
        SharedPreferencesUtils.setParam(context, "freqMin", freqMin);
        this.freqMin = freqMin;
    }

    public int getSubMin() {
        return subMin;
    }

    public void setSubMin(int subMin) {
        this.subMin = subMin;
        SharedPreferencesUtils.setParam(context, "subMin", subMin);
    }

    public int getVoltMin() {
        return voltMin;
    }

    public void setVoltMin(int voltMin) {
        this.voltMin = voltMin;
        SharedPreferencesUtils.setParam(context, "voltMin", voltMin);
    }

    public int getCdStep() {
        return cdStep;
    }

    public void setCdStep(int cdStep) {
        this.cdStep = cdStep;
        SharedPreferencesUtils.setParam(context, "cdStep", cdStep);
    }

    public int getCurrMax() {
        return currMax;
    }

    public void setCurrMax(int currMax) {
        this.currMax = currMax;
        SharedPreferencesUtils.setParam(context, "currMax", currMax);
    }

    public int getCapMax() {
        return capMax;
    }

    public void setCapMax(int capMax) {
        this.capMax = capMax;
        SharedPreferencesUtils.setParam(context, "capMax", capMax);
    }

    public int getBridgeMax() {
        return bridgeMax;
    }

    public void setBridgeMax(int bridgeMax) {
        this.bridgeMax = bridgeMax;
        SharedPreferencesUtils.setParam(context, "bridgeMax", bridgeMax);
    }

    public int getFreqMax() {
        return freqMax;
    }

    public void setFreqMax(int freqMax) {
        this.freqMax = freqMax;
        SharedPreferencesUtils.setParam(context, "freqMax", freqMax);
    }

    public int getSubMax() {
        return subMax;
    }

    public void setSubMax(int subMax) {
        this.subMax = subMax;
        SharedPreferencesUtils.setParam(context, "subMax", subMax);
    }

    public int getVoltMax() {
        return voltMax;
    }

    public void setVoltMax(int voltMax) {
        this.voltMax = voltMax;
        SharedPreferencesUtils.setParam(context, "voltMax", voltMax);
    }

    public int getCdTime() {
        return cdTime;
    }

    public void setCdTime(int cdTime) {
        this.cdTime = cdTime;
        SharedPreferencesUtils.setParam(context, "cdTime", cdTime);
    }

    public int getTestWhich() {
        return testWhich;
    }

    public void setTestWhich(int testWhich) {
        this.testWhich = testWhich;
        SharedPreferencesUtils.setParam(context, "testWhich", testWhich);
    }


}
