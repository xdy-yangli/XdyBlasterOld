package com.example.xdyblaster;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;
import java.util.logging.Logger;


public class BootReceiver extends BroadcastReceiver {

    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    private final String ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
    private final String ACTION_MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED";
    private final String ACTION_MEDIA_EJECT = "android.intent.action.MEDIA_EJECT";
    private final String ACTION_MEDIA_REMOVED = "android.intent.action.MEDIA_REMOVED";

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent it = new Intent(context, MainActivity.class);
            it.setAction("android.intent.action.MAIN");
            it.addCategory("android.intent.category.LAUNCHER");
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);


            Log.e("boot", "Boot 开机自动启动");
        } else {
            Log.e("boot", "Boot 非开机自动启动");
        }



//        Log.i("logs::", intent.getAction());
//        Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();

//        /**
////         * 如果 系统 启动的消息，则启动 APP 主页活动
////         */
//        if (ACTION_BOOT.equals(intent.getAction()) ||
//                ACTION_MEDIA_MOUNTED.equals(intent.getAction()) ||
//                ACTION_MEDIA_UNMOUNTED.equals(intent.getAction()) ||
//                ACTION_MEDIA_EJECT.equals(intent.getAction()) ||
//                ACTION_MEDIA_REMOVED.equals(intent.getAction())) {
//
////        if (ACTION_BOOT.equals(intent.getAction())) {
//            Intent intentMainActivity = new Intent(context, MainActivity.class);
//            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intentMainActivity);
//            Log.e("logs::", intent.getAction());
////            Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();
//
////            Toast.makeText(context, "开机完毕~", Toast.LENGTH_LONG).show();
//        }
//
    }

}