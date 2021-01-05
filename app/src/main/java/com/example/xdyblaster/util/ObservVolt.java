package com.example.xdyblaster.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.fragment.FragmentVolt;

import utils.SerialPortUtils;

import static android.content.Context.BATTERY_SERVICE;

public class ObservVolt implements Observer<Integer> {
    Context context;
    FragmentVolt frVolt;
    SerialPortUtils serialPortUtils;
    FragmentManager fragmentManager;
    private DataViewModel dataViewModel;
    int overErr;


    public ObservVolt(Context context, FragmentVolt frVolt, FragmentManager fragmentManager) {
        this.context = context;
        this.frVolt = frVolt;
        serialPortUtils = SerialPortUtils.getInstance(context);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        this.fragmentManager = fragmentManager;
//        commErr = 0;
        overErr = 0;
    }

    @Override
    public void onChanged(@Nullable Integer integer) {
        switch (integer) {
            case 0:
                float[] vData = dataViewModel.vData;
                setBatteryView(vData[0]);
                setVoltView(vData[1]);
                setCurrentView(vData[2]);
                break;
            case 1:
                if (overErr < 2) {
                    InfoDialog infoDialog = new InfoDialog();
                    infoDialog.setTitle("电流保护");
                    infoDialog.setMessage("请检查总线是否短路！");
                    infoDialog.setBtnEnable(true);
                    infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                        @Override
                        public void onButtonClick(int index, String str) {
                            dataViewModel.overCurrent.postValue(1000);
                        }
                    });
                    infoDialog.show(fragmentManager, "info");
                    overErr = 3;
                }
                break;
            case -1:
                if (dataViewModel.commErr < 5) {
                    dataViewModel.commErr++;
                    if (dataViewModel.commErr >= 5) {
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setTitle("通信错误");
                        //infoDialog.setMessage("请检查设备电源是否打开！");
                        infoDialog.setMessage("请检查总线是否过载！");
                        infoDialog.setBtnEnable(true);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                //serialPortUtils.ResetBlaster();
                            }
                        });
                        thread.start();
                        infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                            @Override
                            public void onButtonClick(int index, String str) {
                                dataViewModel.commErr = 0;
                                dataViewModel.exit.postValue(1000);
                                //dataViewModel.reset=true;
                                //dataViewModel.overCurrent.postValue(1000);
                            }
                        });
                        infoDialog.show(fragmentManager, "info");
                    }
                }
                break;

        }
    }

    private void setBatteryView(float aFloat) {
        int battery;
        String str;
        try {

            if (frVolt != null) {
                frVolt.setLocationIcon();
                if (aFloat >= 1000) {
                    BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
                    int p = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    dataViewModel.battPercent = p;
                    str = String.valueOf(p);
                    if (isPlugged(context))
                        frVolt.tvBatteryPercent.setText("充电");
                    else
                        frVolt.tvBatteryPercent.setText(str + "%");
                    frVolt.horizontalBattery.setPower(p);
                } else if (aFloat >= 500f) {
                    frVolt.tvBatteryPercent.setText("100%");
                    frVolt.horizontalBattery.setPower(100);
                } else if (aFloat > 200f) {
                    frVolt.tvBatteryPercent.setText("充电");
                    battery = Math.round(aFloat - 200f);
                    battery -= 5;
                    if (battery < 0)
                        battery = 0;
                    if (battery >= 100)
                        battery = 99;
                    frVolt.horizontalBattery.setPower(battery);
                } else {
                    battery = Math.round(aFloat);
                    str = String.format("%d%%", battery);
                    frVolt.horizontalBattery.setPower(battery);
                    frVolt.tvBatteryPercent.setText(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVoltView(float aFloat) {
        if (frVolt != null)
            try {
                frVolt.tvVolt.setText(String.format("电压 %.1fv", aFloat));
                dataViewModel.voltFloat=aFloat;
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void setCurrentView(float aFloat) {
        if (frVolt != null)
            try {
                frVolt.tvCurr.setText(String.format("电流 %.1fma", aFloat));
                dataViewModel.currFloat=aFloat;
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void setOverCurrent(int integer) {
        serialPortUtils.uartData.volt = 0;
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("警告！")
                .setMessage("设备过流保护，请检查线路是否短路！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void setRomErr(int integer) {
        if (integer == 1) {
            serialPortUtils.uartData.volt = 0;
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("警告！")
                    .setMessage("设备ROM错误，请升级！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

    /**
     * 是否在充电
     */
    public static boolean isPlugged(Context context) {

        //创建过滤器拦截电量改变广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //通过过滤器来获取电量改变intent 电量改变是系统广播所以无需去设置所以receiver传null即可
        Intent intent = context.registerReceiver(null, intentFilter);
        //获取电量信息
        int isPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        //电源充电
        boolean acPlugged = BatteryManager.BATTERY_PLUGGED_AC == isPlugged;
        //usb充电
        boolean usbPlugged = BatteryManager.BATTERY_PLUGGED_USB == isPlugged;
        //无线充电
        boolean wirePlugged = BatteryManager.BATTERY_PLUGGED_WIRELESS == isPlugged;

        //满足充电即返回true
        return acPlugged || usbPlugged || wirePlugged;

    }
}
