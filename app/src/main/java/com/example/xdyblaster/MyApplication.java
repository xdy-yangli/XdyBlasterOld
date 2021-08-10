package com.example.xdyblaster;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;


//import io.realm.Realm;
//import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
//        Realm.init(this);
//        RealmConfiguration config = new RealmConfiguration.Builder().name("daily.realm").build();
//        Realm.setDefaultConfiguration(config);

    }
}