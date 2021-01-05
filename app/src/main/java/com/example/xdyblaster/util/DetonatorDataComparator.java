package com.example.xdyblaster.util;

import java.util.Comparator;

public class DetonatorDataComparator implements Comparator<DetonatorData> {
    private int sortType;
    private int sortUpDown;

    public DetonatorDataComparator(int sortType, int sortUpDown) {
        this.sortType = sortType;
        this.sortUpDown = sortUpDown;

    }

    @Override
    public int compare(DetonatorData o1, DetonatorData o2) {
        int s = 0;
        long l1, l2;
        switch (sortType) {
            case 0:
                if (o1.getBlasterTime() != o2.getBlasterTime()) {
                    s = o1.getBlasterTime() - o2.getBlasterTime();
                    break;
                }
                if (o1.getRowNum() != o2.getRowNum()) {
                    s = o1.getRowNum() - o2.getRowNum();
                    break;
                }
                if (o1.getHoleNum() != o2.getHoleNum()) {
                    s = o1.getHoleNum() - o2.getHoleNum();
                    break;
                }
                s = o1.getUuid().compareTo(o2.getUuid());
//                l1 = Long.parseLong(o1.getUuid().substring(8));
//                l2 = Long.parseLong(o2.getUuid().substring(8));
//                s = Long.compare(l1, l2);
                break;
            case 1:
//                l1 = Long.parseLong(o1.getUuid().substring(8));
//                l2 = Long.parseLong(o2.getUuid().substring(8));
//                s = Long.compare(l1, l2);
                s = o1.getUuid().compareTo(o2.getUuid());
                break;
            case 2:
                if (o1.getRowNum() != o2.getRowNum()) {
                    s = o1.getRowNum() - o2.getRowNum();
                    break;
                }
                if (o1.getHoleNum() != o2.getHoleNum()) {
                    s = o1.getHoleNum() - o2.getHoleNum();
                    break;
                }
                if (o1.getBlasterTime() != o2.getBlasterTime()) {
                    s = o1.getBlasterTime() - o2.getBlasterTime();
                    break;
                }
                s = o1.getUuid().compareTo(o2.getUuid());
//                l1 = Long.parseLong(o1.getUuid().substring(8));
//                l2 = Long.parseLong(o2.getUuid().substring(8));
//                s = Long.compare(l1, l2);
                break;
            case 3:
                if (o1.getHoleNum() != o2.getHoleNum()) {
                    s = o1.getHoleNum() - o2.getHoleNum();
                    break;
                }
                if (o1.getRowNum() != o2.getRowNum()) {
                    s = o1.getRowNum() - o2.getRowNum();
                    break;
                }
                if (o1.getBlasterTime() != o2.getBlasterTime()) {
                    s = o1.getBlasterTime() - o2.getBlasterTime();
                    break;
                }
                s = o1.getUuid().compareTo(o2.getUuid());
//                l1 = Long.parseLong(o1.getUuid().substring(8));
//                l2 = Long.parseLong(o2.getUuid().substring(8));
//                s = Long.compare(l1, l2);
                break;
        }
        if (sortUpDown == 0)
            return s;
        else
            return s * -1;

    }
}