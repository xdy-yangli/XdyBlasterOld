package com.example.xdyblaster.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static com.example.xdyblaster.MainActivity.actionKeyF3Push;
import static com.example.xdyblaster.MainActivity.actionKeyF3Release;
import static com.example.xdyblaster.MainActivity.actionScan;
import static com.example.xdyblaster.util.FileFunc.getSystemModel;

public class KeyReceiver extends BroadcastReceiver {
    Context mContext;
    private IntentFilter intentFilter;
    Handler handler;
    int keyF1, keyF2, keyF3, ver;
    public boolean converKey = false;
    long keyTime1, keyTime0;
    DataViewModel dataViewModel;


    public KeyReceiver(Context context, Handler handler, DataViewModel dataViewModel) {
        mContext = context;
        this.dataViewModel = dataViewModel;
        this.handler = handler;
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.rfid.FUN_KEY");
        mContext.registerReceiver(this, intentFilter);
        keyF1 = 0;
        keyF2 = 0;
        keyF3 = 0;
        if (getSystemModel().equals("k71v1_64_bsp"))
            ver = 1;
        else
            ver = 0;
        keyTime0 = 0;
        keyTime1 = 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle;
        Message message;
        bundle = intent.getExtras();
        int i = bundle.getInt("keyCode");
        boolean b = bundle.getBoolean("keydown", false);
        Log.e("key", String.valueOf(i));

        message = new Message();
        if (ver == 1) {
            switch (i) {
                case 134:
                    i = 133;
                    break;
                case 133:
                    i = 131;
                    i=0;
                    break;
                case 135:
                    i = 132;
                    i=0;
                    break;
            }
        }

        message.what = i;
        //        if(System.currentTimeMillis()-keyTime<500) {
//            keyTime = System.currentTimeMillis();
//            return;
//        }
//        keyTime = System.currentTimeMillis();
        if (i == 131) {
            if ((keyF1 == 0) && b) {
                message.arg1 = 1;
//                handler.sendMessage(message);
                if (dataViewModel.keyHandler != null)
                    dataViewModel.keyHandler.sendMessage(message);
                keyF1 = 1;
            } else if ((keyF1 == 1) && (!b)) {
                message.arg1 = 0;
                if (dataViewModel.keyHandler != null)
                    dataViewModel.keyHandler.sendMessage(message);
                keyF1 = 0;
            }
//            if (converKey) {
//                intent.putExtra("keyCode", 135);
//                mContext.sendBroadcast(intent);
//            }
        }
        if (i == 132) {
            if ((keyF2 == 0) && b) {
                message.arg1 = 1;
                if (dataViewModel.keyHandler != null)
                    dataViewModel.keyHandler.sendMessage(message);
                keyF2 = 1;
            } else if ((keyF2 == 1) && (!b)) {
                message.arg1 = 0;
                if (dataViewModel.keyHandler != null)
                    dataViewModel.keyHandler.sendMessage(message);
                keyF2 = 0;
            }
//            if (converKey) {
//                intent.putExtra("keyCode", 135);
//                mContext.sendBroadcast(intent);
//            }
        }

        if (i == 133) {
            if ((keyF3 == 0) && b) {
                message.arg1 = 1;
                if ((System.currentTimeMillis() - keyTime1) > 200) {
//                    handler.sendMessage(message);
                    if (dataViewModel.keyHandler != null)
                        dataViewModel.keyHandler.sendMessage(message);
                    dataViewModel.keyF3 = 1;
                    keyF3 = 1;
                    Log.e("keyf3", "1");
                    Intent intent2 = new Intent(actionKeyF3Push);
                    mContext.getApplicationContext().sendBroadcast(intent2);

                }
                keyTime1 = System.currentTimeMillis();
            } else if ((keyF3 == 1) && (!b)) {
                message.arg1 = 0;
                if ((System.currentTimeMillis() - keyTime0) > 200) {
                    if (dataViewModel.keyHandler != null)
                        dataViewModel.keyHandler.sendMessage(message);
                    keyF3 = 0;
                    Log.e("keyf3", "0");
                    Intent intent2 = new Intent(actionKeyF3Release);
                    mContext.getApplicationContext().sendBroadcast(intent2);
                }
                keyTime0 = System.currentTimeMillis();
            }
////            if (converKey) {
////                intent.putExtra("keyCode", 135);
////                mContext.sendBroadcast(intent);
////            }
        }
        if (i == 135) {
            message.arg1 = 1;
            if (dataViewModel.keyHandler != null)
                dataViewModel.keyHandler.sendMessage(message);
            //handler.sendMessage(message);
        }
    }

    public void register() {

    }

    public void unRegister() {
        mContext.registerReceiver(this, intentFilter);
    }
}

