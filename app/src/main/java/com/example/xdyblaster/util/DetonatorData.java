package com.example.xdyblaster.util;

public class DetonatorData {
    private int rowNum;
    private int holeNum;
    private int cntNum;
    private long id;
    private int blasterTime;
    private int delay;
    private int mainFrequency;
    private int subFrequency;
    private boolean selection;
    private int color;
    private String uuid;
    private int rowNumErr;
    private int holeNumErr;
    private int blasterTimeErr;
    private int cap;
    private float bridge;

    public int getCap() {
        return cap;
    }

    public void setCap(int cap) {
        this.cap = cap;
    }

    public float getBridge() {
        return bridge;
    }

    public void setBridge(float bridge) {
        this.bridge = bridge;
    }

    public int getRowNumErr() {
        return rowNumErr;
    }

    public void setRowNumErr(int rowNumErr) {
        this.rowNumErr = rowNumErr;
    }

    public int getHoleNumErr() {
        return holeNumErr;
    }

    public void setHoleNumErr(int holeNumErr) {
        this.holeNumErr = holeNumErr;
    }

    public int getBlasterTimeErr() {
        return blasterTimeErr;
    }

    public void setBlasterTimeErr(int blasterTimeErr) {
        this.blasterTimeErr = blasterTimeErr;
    }

    public DetonatorData() {
        this.rowNum = 0;
        this.holeNum = 0;
        this.cntNum = 0;
        this.id = 0;
        this.blasterTime = 0;
        this.delay = 0;
        this.mainFrequency = 0;
        this.subFrequency = 0;
        this.selection = false;
        this.color = 0;
        uuid = "00000000000000";

    }

    public DetonatorData(int rowNum) {
        this.rowNum = rowNum;
        this.holeNum = 0;
        this.cntNum = 0;
        this.id = 0;
        this.blasterTime = 0;
        this.delay = 0;
        this.mainFrequency = 0;
        this.subFrequency = 0;
        this.selection = false;
        this.color = 0;
        uuid = "00000000000000";
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean getSelection() {
        return selection;
    }

    public void setSelection(boolean selection) {
        this.selection = selection;
    }


    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getHoleNum() {
        return holeNum;
    }

    public void setHoleNum(int holeNum) {
        this.holeNum = holeNum;
    }

    public int getCntNum() {
        return cntNum;
    }

    public void setCntNum(int cntNum) {
        this.cntNum = cntNum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getBlasterTime() {
        return blasterTime;
    }

    public void setBlasterTime(int blasterTime) {
        this.blasterTime = blasterTime;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getMainFrequency() {
        return mainFrequency;
    }

    public void setMainFrequency(int mainFrequency) {
        this.mainFrequency = mainFrequency;
    }

    public int getSubFrequency() {
        return subFrequency;
    }

    public void setSubFrequency(int subFrequency) {
        this.subFrequency = subFrequency;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }
//    public Long getUuidLong() {
//
//        return Long.parseLong(this.uuid);
//    }

    public void convertDataToBuffer(byte[] buffer, int pos) {
        byte[] uuidHex = new byte[5];
        byte[] tmp;
        int d;
        long l;
        tmp = uuid.getBytes();
        uuidHex[4] = 0;
        for (int i = 0; i < 4; i++) {
            d = (tmp[i * 3 + 2] - 0x30) & 0x00ff;
            d = d * 10 + ((tmp[i * 3 + 3] - 0x30) & 0x00ff);
            d = d * 10 + ((tmp[i * 3 + 4] - 0x30) & 0x00ff);
            uuidHex[i] = (byte) (d & 0xff);
            uuidHex[4] = (byte) ((uuidHex[4] << 2) | ((d >> 8) & 0x03));
        }
        buffer[pos++] = tmp[0];
        buffer[pos++] = tmp[1];
        buffer[pos++] = uuidHex[0];
        buffer[pos++] = uuidHex[1];
        buffer[pos++] = uuidHex[2];
        buffer[pos++] = uuidHex[3];
        buffer[pos++] = uuidHex[4];
        buffer[pos++] = 0;
        d = rowNum * 1000 + holeNum;
        buffer[pos++] = (byte) (d & 0x00ff);
        buffer[pos++] = (byte) ((d >> 8) & 0x00ff);
        buffer[pos++] = (byte) (blasterTime & 0x00ff);
        buffer[pos++] = (byte) ((blasterTime >> 8) & 0x00ff);
    }
}

