package com.example.xdyblaster.util;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DataViewModel extends AndroidViewModel {
    public DetonatorSetting detonatorSetting = new DetonatorSetting();
    public List<DetonatorData> detonatorDatas = new ArrayList<>();
    public List<List<DetonatorData>> detonatorList = new ArrayList<>();

    public List<MutableLiveData<Integer>> updateList;
    //public Drawable drawableG, drawableR, drawableB, drawableGray;
    //public Drawable icon4, icon4s, icon5, icon5s, icon6, icon6s;
    public String fileName;
    public boolean ok, dataChanged = false;
    public boolean enMonitorVolt;
    public boolean fileLoaded, fileReload = false;
    public int battStatus;
    public float[] vData = new float[4];
    public int ver;
    // public LatLng latLng;

    public MutableLiveData<Integer> volt = new MutableLiveData<>();
    public MutableLiveData<Integer> offline = new MutableLiveData<>();
    public MutableLiveData<Float> batt = new MutableLiveData<>();
    public MutableLiveData<Integer> overCurrent = new MutableLiveData<>();
    public MutableLiveData<Integer> exit = new MutableLiveData<>();
    public MutableLiveData<Integer> keyF1 = new MutableLiveData<>();
    public MutableLiveData<Integer> keyF2 = new MutableLiveData<>();
    public int keyF3;

    public MutableLiveData<Integer> romErr = new MutableLiveData<>();
    public MutableLiveData<Integer> updateStep = new MutableLiveData<>();
    public MutableLiveData<Integer> totalCount = new MutableLiveData<>();

    public Handler keyHandler = null;

    public int[] idBuffer = new int[2048];
    public int[] timerBuffer = new int[2048];
    public int[] areaBuffer = new int[2048];
    public int[] statusBuffer = new int[2048];
    public byte[] uuidBuffer = new byte[1024 * 16];

    public String devId, userId = "123456789012345678";
    public String htid, xmbh, dwdm, jd, wd, bpsj;
    public int commErr = 0, countDown = 0, overStop = 0;
    public int defaultVolt;
    public int systemCount = 0;
    public  int battPercent;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public void InitData(String fileName) {

        updateList = new ArrayList<>();
        if (!fileLoaded)
            FileFunc.loadDetonatorSetting(fileName, detonatorSetting);
        int count = detonatorSetting.getRow();
        if (count == 0)
            count = 1;
        resetDataList();
//        int r = 0;
//        List<DetonatorData> tmp = new ArrayList<>();
//        for (DetonatorData d : detonatorDatas) {
//            if (d.getRowNum() != r) {
//                {
//                    detonatorList.add(tmp);
//                    tmp = new ArrayList<>();
//                    r = d.getRowNum();
//                }
//            }
//            tmp.add(d);
//        }
//        detonatorList.add(tmp);

        MutableLiveData<Integer> data;
        for (int i = 0; i < count; i++) {
            data = new MutableLiveData<>();
            data.setValue(-1);
            updateList.add(data);
        }
    }

    public void resetDataList() {
        List<DetonatorData> tmp;
        detonatorList.clear();
        int count = detonatorSetting.getRow();
        if (count == 0)
            count = 1;
        for (int i = 0; i < count; i++) {
            tmp = new ArrayList<>();
            detonatorList.add(tmp);
        }
    }

    public void addDetonatorArea() {
        List<DetonatorData> tmp;
        tmp = new ArrayList<>();
        detonatorList.add(tmp);
        MutableLiveData<Integer> data;
        data = new MutableLiveData<>();
        data.setValue(-1);
        updateList.add(data);
        detonatorSetting.setRow(detonatorList.size());
    }

    public void setDataList() {
        int count = detonatorSetting.getRow();
        int r = 0;
        for (DetonatorData d : detonatorDatas) {
            if (d.getRowNum() != r) {
                {
                    updateList.get(r).postValue(-1);
                    r = d.getRowNum();
                }
            }
            detonatorList.get(r).add(d);
        }
        //updateList.get(r).postValue("1");

    }

    public DataViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }


}
