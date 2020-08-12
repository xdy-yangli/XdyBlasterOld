package com.example.xdyblaster.util;

import java.util.Comparator;

public class ResultDataComparator implements Comparator<ResultData> {


    @Override
    public int compare(ResultData o1, ResultData o2) {
        return o2.name.compareTo(o1.name);
    }
}
