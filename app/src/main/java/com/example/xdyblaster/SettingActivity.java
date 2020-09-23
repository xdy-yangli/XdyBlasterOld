package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.system.SystemActivity;
import com.example.xdyblaster.util.DataViewModel;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.utils.UpdateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

//import constant.UiType;
//import listener.Md5CheckResultListener;
//import listener.UpdateDownloadListener;
//import me.jessyan.autosize.internal.CustomAdapt;
//import model.UiConfig;
//import model.UpdateConfig;
//import update.UpdateAppUtils;

public class SettingActivity extends AppCompatActivity implements CustomAdapt {

    @BindView(R.id.layout_wifi)
    FrameLayout layoutWifi;
    @BindView(R.id.layout_display)
    FrameLayout layoutDisplay;
    @BindView(R.id.layout_sound)
    FrameLayout layoutSound;
    @BindView(R.id.layout_position)
    FrameLayout layoutPosition;
    @BindView(R.id.layout_system)
    FrameLayout layoutSystem;
    @BindView(R.id.layout_face)
    FrameLayout layoutFace;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 133:
                    if (msg.arg1 == 1) {
                        dataViewModel.systemCount++;
                        if (dataViewModel.systemCount > 10)
                            layoutSystem.setVisibility(View.VISIBLE);

                    }
                    break;
            }
        }
    };
    @BindView(R.id.layout_update)
    FrameLayout layoutUpdate;
    private SerialPortUtils serialPortUtils;
    private DataViewModel dataViewModel;

    //    private String apkUrl = "http://gzyte.com.cn/download/app-debug.apk";
//    private String updateTitle = "发现新版本V2.0.0";
//    private String updateContent = "1、Kotlin重构版\n2、支持自定义UI\n3、增加md5校验\n4、更多功能等你探索";
    private String mUpdateUrl = "http://gzyte.com.cn/download/update_test.json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        layoutSystem.setVisibility(View.GONE);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.systemCount = 0;
        dataViewModel.keyHandler = handler;
        if (UpdateUtils.getVersionCode(this) > 100)
            mUpdateUrl = "http://gzyte.com.cn/download/update_test.json";
        else
            mUpdateUrl = "http://gzyte.com.cn/download/test/update_test.json";


        //  UpdateAppUtils.init(this);

    }

    @Override
    protected void onDestroy() {
        dataViewModel.keyHandler = null;
        super.onDestroy();
    }

    @SuppressLint("ResourceAsColor")
    @OnClick({R.id.layout_wifi, R.id.layout_display, R.id.layout_sound, R.id.layout_position, R.id.layout_system, R.id.layout_face, R.id.layout_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_wifi:
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
                break;
            case R.id.layout_display:
                intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(intent);
                break;
            case R.id.layout_sound:
                intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                startActivity(intent);
                break;
            case R.id.layout_position:
                intent = new Intent(SettingActivity.this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_system:
                intent = new Intent(SettingActivity.this, SystemActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_update:
                XUpdate.newBuild(SettingActivity.this)
                        .updateUrl(mUpdateUrl)
//                        .promptThemeColor(R.color.blue)
//                        .promptWidthRatio(2.4F)
//                        .promptHeightRatio(2.3F)
                        .update();
                break;
            case R.id.layout_face:
//                intent = new Intent(SettingActivity.this, RegisterAndRecognizeActivity.class);
//                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 500;
    }


//    public void testDownload()
//    {
//        UpdateConfig updateConfig = new UpdateConfig();
//        updateConfig.setCheckWifi(true);
//        updateConfig.setNeedCheckMd5(false);
//        updateConfig.setNotifyImgRes(R.mipmap.ic_add_location_white_48dp);
//
//        UiConfig uiConfig = new UiConfig();
//        uiConfig.setUiType(UiType.PLENTIFUL);
//
//        UpdateAppUtils
//                .getInstance()
//                .apkUrl(apkUrl)
//                .updateTitle(updateTitle)
//                .updateContent(updateContent)
//                .uiConfig(uiConfig)
//                .updateConfig(updateConfig)
//                .setMd5CheckResultListener(new Md5CheckResultListener() {
//                    @Override
//                    public void onResult(boolean result) {
//
//                    }
//                })
//                .setUpdateDownloadListener(new UpdateDownloadListener() {
//                    @Override
//                    public void onStart() {
//
//                    }
//
//                    @Override
//                    public void onDownload(int progress) {
//
//                    }
//
//                    @Override
//                    public void onFinish() {
//
//                    }
//
//                    @Override
//                    public void onError(@NotNull Throwable e) {
//
//                    }
//                })
//                .update();
//
//    }


}
