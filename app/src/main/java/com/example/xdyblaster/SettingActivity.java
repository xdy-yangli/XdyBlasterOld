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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
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
                intent = new Intent(SettingActivity.this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_system:
                intent = new Intent(SettingActivity.this, SystemActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_face:
                intent = new Intent(SettingActivity.this, RegisterAndRecognizeActivity.class);
                startActivity(intent);
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


}
