package com.example.xdyblaster.util;

public class DetonatorSetting {
    int row = 1;
    int hole;
    int cnt;
    int rowDelay;
    int holeDelay;
    boolean rowSequence;

    public boolean isRowSequence() {
        return rowSequence;
    }

    public void setRowSequence(boolean rowSequence) {
        this.rowSequence = rowSequence;
    }


    public int getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = getInt(cnt);
    }


    public int getHole() {
        return hole;
    }

    public int getRow() {
        return 1;
    }

    public int getHoleDelay() {
        return holeDelay;
    }

    public int getRowDelay() {
        return rowDelay;
    }

    public void setHole(String hole) {
        this.hole = getInt(hole);
    }

    public void setHoleDelay(String holeDelay) {
        this.holeDelay = getInt(holeDelay);
    }

    public void setRow(String row) {
        this.row = 1;//getInt(row);
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setRowDelay(String rowDelay) {
        this.rowDelay = getInt(rowDelay);
    }


    public int getInt(String str) {
        byte[] bytes = str.getBytes();
        int i = 0, d;
        for (byte b : bytes) {
            if ((b > '9') || (b < '0'))
                break;
            d = (b - '0');
            i = i * 10 + d;
        }
        return i;
    }
}
