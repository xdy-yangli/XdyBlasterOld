package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.entity.VersionEntity;
import com.example.xdyblaster.retrofit2.ApiService;
import com.example.xdyblaster.retrofit2.LoadingDialog;
import com.example.xdyblaster.retrofit2.LoadingDialogObserver;
import com.example.xdyblaster.retrofit2.retrofit.CustomHttpClient;
import com.example.xdyblaster.retrofit2.retrofit.CustomRetrofit;
import com.example.xdyblaster.system.SystemActivity;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DownloadUtil;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import constant.UiType;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import listener.Md5CheckResultListener;
import listener.UpdateDownloadListener;
import me.jessyan.autosize.internal.CustomAdapt;
import model.UiConfig;
import model.UpdateConfig;
import okhttp3.OkHttpClient;
import update.UpdateAppUtils;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.CommDetonator.COMM_READ_DEV_ID;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_OUTPUT;

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
    private SerialPortUtils serialPortUtils;
    private DataViewModel dataViewModel;

    private String apkUrl ="http://192.168.0.111:12321/app-debug.apk";
    private String updateTitle = "发现新版本V2.0.0";
    private String updateContent = "1、Kotlin重构版\n2、支持自定义UI\n3、增加md5校验\n4、更多功能等你探索";


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

        UpdateAppUtils.init(this);

    }

    @Override
    protected void onDestroy() {
        dataViewModel.keyHandler = null;
        super.onDestroy();
    }

    @OnClick({R.id.layout_wifi, R.id.layout_display, R.id.layout_sound, R.id.layout_position, R.id.layout_system, R.id.layout_face})
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
                new DownloadUtil(SettingActivity.this, apkUrl, "123.apk");
  //              loadVersionInfo();
    //            testDownload();
//                intent = new Intent(SettingActivity.this, MapActivity.class);
//                startActivity(intent);
                break;
            case R.id.layout_system:
                intent = new Intent(SettingActivity.this, SystemActivity.class);
                startActivity(intent);
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

    public void testDownload()
    {
        UpdateConfig updateConfig = new UpdateConfig();
        updateConfig.setCheckWifi(true);
        updateConfig.setNeedCheckMd5(false);
        updateConfig.setNotifyImgRes(R.mipmap.ic_add_location_white_48dp);

        UiConfig uiConfig = new UiConfig();
        uiConfig.setUiType(UiType.PLENTIFUL);

        UpdateAppUtils
                .getInstance()
                .apkUrl(apkUrl)
                .updateTitle(updateTitle)
                .updateContent(updateContent)
                .uiConfig(uiConfig)
                .updateConfig(updateConfig)
                .setMd5CheckResultListener(new Md5CheckResultListener() {
                    @Override
                    public void onResult(boolean result) {

                    }
                })
                .setUpdateDownloadListener(new UpdateDownloadListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onDownload(int progress) {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onError(@NotNull Throwable e) {

                    }
                })
                .update();

    }


    private void loadVersionInfo() {
        OkHttpClient client = new CustomHttpClient()
                .setConnectTimeout(5_000)
                .setShowLog(true)
                .createOkHttpClient();
        new CustomRetrofit()
                //我在app的build.gradle文件的defaultConfig标签里定义了BASE_URL
                .setBaseUrl("https://raw.githubusercontent.com/")
                .setOkHttpClient(client)
                .createRetrofit()
                .create(ApiService.class)
                .getVersionInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoadingDialogObserver<String>(createLoadingDialog()) {
                    @Override
                    public void onStart(Disposable disposable) {

                    }

                    @Override
                    public void onResult(String s) {
                        s = s.substring(1, s.length() - 1);
                        VersionEntity entity = VersionEntity.fromJson(s);
//                        showUpdateTipsDialog(entity);
                    }

                    @Override
                    public void onException(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleteOrCancel(Disposable disposable) {

                    }
                });
    }
    protected Dialog createLoadingDialog() {
        return createLoadingDialog("Loading…");
    }
    protected Dialog createLoadingDialog(CharSequence txt) {
        LoadingDialog dialog = new LoadingDialog(this);
        dialog.setMessage(txt);
        dialog.showMessageView(!TextUtils.isEmpty(txt));
        return dialog;
    }
}
