package com.example.xdyblaster.util;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.xdyblaster.fragment.FragmentVolt;

import utils.SerialPortUtils;

public class ObservOverCurrent implements Observer<Integer> {
    Context context;
    FragmentVolt frVolt;
    SerialPortUtils serialPortUtils;
    FragmentManager fragmentManager;
    private DataViewModel dataViewModel;
    int commErr, overErr;


    public ObservOverCurrent(Context context, FragmentVolt frVolt, FragmentManager fragmentManager) {
        this.context = context;
        this.frVolt = frVolt;
        serialPortUtils = SerialPortUtils.getInstance(context);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        this.fragmentManager = fragmentManager;
        commErr = 0;
        overErr = 0;
    }

    @Override
    public void onChanged(@Nullable Integer integer) {
        if (integer == 1) {
            if (overErr < 2) {
                InfoDialog infoDialog = new InfoDialog();
                infoDialog.setTitle("电流保护");
                infoDialog.setMessage("请检查总线是否短路！");
                infoDialog.setBtnEnable(true);
                infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, String str) {
                        dataViewModel.exit.postValue(1000);
                    }
                });

                infoDialog.show(fragmentManager, "info");
                overErr = 3;
            }
            dataViewModel.overCurrent.setValue(0);
        }
    }
}
