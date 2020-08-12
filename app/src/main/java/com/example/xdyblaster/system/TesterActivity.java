package com.example.xdyblaster.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.fragment.FragmentVolt;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FactorySetting;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.KeyReceiver;
import com.example.xdyblaster.util.ObservVolt;
import com.example.xdyblaster.util.TesterItem;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.DET_CAP;
import static com.example.xdyblaster.util.AppConstants.DET_CAP_VOLT;
import static com.example.xdyblaster.util.AppConstants.DET_ERR_STATUS;
import static com.example.xdyblaster.util.AppConstants.DET_FREQ;
import static com.example.xdyblaster.util.AppConstants.DET_ID;
import static com.example.xdyblaster.util.AppConstants.DET_TEST_FINISH;
import static com.example.xdyblaster.util.AppConstants.ERR_WAIT;
import static com.example.xdyblaster.util.CommDetonator.COMM_ENTER_TEST_MODE;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_TEST_STATUS;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_ON;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_TEST_MODE;

public class TesterActivity extends AppCompatActivity implements CustomAdapt {


    @BindView(R.id.lt_data)
    LinearLayout ltData;
    @BindView(R.id.testerHead)
    TesterItem testerHead;
    @BindView(R.id.btSetting)
    Button btSetting;
    @BindView(R.id.btStartTest)
    Button btStartTest;
    @BindView(R.id.aviTesting)
    AVLoadingIndicatorView aviTesting;
    @BindView(R.id.btStopTest)
    Button btStopTest;
    private DataViewModel dataViewModel;
    private boolean firstBoot, commEnable = true, battEnable = true;
    public boolean checkOnline;
    private SerialPortUtils serialPortUtils;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (battEnable && commEnable) {
                        commTask.running = false;
                        commTask.cancel(true);
                        commTask = new CommTask(TesterActivity.this);
                        commTask.execute(1, COMM_GET_BATT);
                    }
                    break;
                case 131:
                case 132:
                case 133:
//                    if (msg.arg1 == 1)
                    break;

            }
        }
    };


    CommTask commTask;
    FragmentVolt frVolt;
    Timer battTimer = null;
    TimerTask timerTask;
    int detonatorIndex = 0, detonatorDelay = 0, detonatorTime = 40000, detonatorArea = 59, detonatorHole = 999;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    MediaPlayer mMediaPlayer2 = new MediaPlayer();
    long scanID, uuid;
    int password;
    String uuidStr;
    long keyTime = 0;
    int f1f2Mode;
    //KeyReceiver myReceiver;
    InfoDialog infoDialog;
    int delayTimeSelect;
    boolean scanning = false;
    int sortType, sortUpDown;
    boolean isFocus;
    ObservVolt observVolt;
    TesterItem[] testerItems = new TesterItem[20];
    FactorySetting factorySetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tester);
        ButterKnife.bind(this);
        firstBoot = true;
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        // myReceiver = new KeyReceiver(this, handler,dataViewModel);
        dataViewModel.keyHandler = handler;
        FragmentManager fragMe = getSupportFragmentManager();
        frVolt = (FragmentVolt) fragMe.findFragmentById(R.id.frVolt);
        delayTimeSelect = 0;
        loadSetting();
        try {
            AssetFileDescriptor fd = getAssets().openFd("9414.wav");
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AssetFileDescriptor fd = getAssets().openFd("702.wav");
            mMediaPlayer2.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer2.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataViewModel.overCurrent.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == 1000) {
                    dataViewModel.overCurrent.setValue(1);
                    finish();
                }
            }
        });
        observVolt = new ObservVolt(this, frVolt, getSupportFragmentManager());
        dataViewModel.volt.observe(this, observVolt);
        dataViewModel.volt.setValue(0);
        commTask = new CommTask(this);
        commTask.execute(3, COMM_IDLE, COMM_GET_BATT, COMM_POWER_ON);
        testerHead.setText("序号", "电流", "电容", "桥丝", "主频", "副频", "电压", "故障");
        factorySetting = new FactorySetting(this);
        aviTesting.hide();
        btStopTest.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        if (battTimer != null) {
            battTimer.cancel();
            timerTask.cancel();
            battTimer = null;
            timerTask = null;
        }
        commTask.running = false;
        commTask.cancel(true);
        //unregisterReceiver(myReceiver);
        dataViewModel.keyHandler = null;
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && firstBoot) {
            firstBoot = false;
            for (int i = 0; i < 20; i++) {
                testerItems[i] = new TesterItem(this);
                testerItems[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
                testerItems[i].setText(String.valueOf(i), "---", "---", "---", "---", "---", "---", "");
                ltData.addView(testerItems[i]);
            }

        }

        if (hasFocus) {
            isFocus = true;
        } else
            isFocus = false;
        super.onWindowFocusChanged(hasFocus);
    }

//    @OnClick({R.id.btMinus, R.id.btEncode, R.id.btPlus})
//    public void onViewClicked(View view) {
//        if (System.currentTimeMillis() < keyTime + 200)
//            return;
//        keyTime = System.currentTimeMillis();
//        switch (view.getId()) {
//            case R.id.btMinus:
//                break;
//            case R.id.btEncode:
//                break;
//            case R.id.btPlus:
//                break;
//        }
//    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void loadSetting() {
        int i;
        String str = "";
        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
////            SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
////                SharedPreferences.Editor mEdit = mShare.edit();
////                long u = Long.parseLong(stringUuid);
////                u++;
////                mEdit.putString("detUuid", String.valueOf(u));
////                mEdit.apply();

    }

    @OnClick({R.id.btSetting, R.id.btStartTest, R.id.btStopTest})
    public void onViewClicked(View view) {
        if (System.currentTimeMillis() < keyTime + 200)
            return;
        keyTime = System.currentTimeMillis();

        switch (view.getId()) {
            case R.id.btSetting:
                Intent intent = new Intent(TesterActivity.this, FactorySettingActivity.class);
                startActivity(intent);
                break;
            case R.id.btStartTest:
                factorySetting.reloadData();
                for (int i = 0; i < 20; i++) {
                    testerItems[i].setText(String.valueOf(i + 1), "---", "---", "---", "---", "---", "---", "");
                    testerItems[i].setAllNormal();
                    testerItems[i].setItemVisible(factorySetting.getTestWhich());
                }
                testerHead.setItemVisible(factorySetting.getTestWhich());
                commTask.cancel(true);
                commTask = new CommTask(this);
                commTask.setMac((factorySetting.getTestWhich() << 8) + factorySetting.getTestCount());
                commTask.setData00(factorySetting.getCdTime());
                commTask.setData10(factorySetting.getCdStep());
                commTask.execute(1, COMM_ENTER_TEST_MODE, COMM_GET_TEST_STATUS);
                btStartTest.setEnabled(false);
                aviTesting.show();
                btStopTest.setEnabled(true);
                btSetting.setEnabled(false);

                break;
            case R.id.btStopTest:
                aviTesting.hide();
                btStartTest.setEnabled(true);
                btSetting.setEnabled(true);
                btStopTest.setEnabled(false);
                commTask.cancel(true);
                commTask = new CommTask(this);
                commTask.execute(1, COMM_STOP_TEST_MODE);
                break;

        }
    }


    @SuppressLint("StaticFieldLeak")
    private class CommTask extends CommDetonator {

        public CommTask(Context context) {
            this.serialPortUtils = SerialPortUtils.getInstance(context);
            this.dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
            commEnable = false;
        }


        @SuppressLint("DefaultLocale")
        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]) {
                case COMM_GET_TEST_STATUS:
                    if (values[1] == 1) {
                        int mode, n;
                        mode = (values[2] >> 5) & 0x07;
                        n = values[2] & 0x01f;
                        double d, d2;
                        int tmp;
                        switch (mode) {
                            case DET_ID:
                                if (values[5] != 0) {
                                    testerItems[n].setCorrect(0);
                                    d = values[6] & 0x00ffff;
                                    d = (d * 3.3 / 4096 * 7.8 / 60 / 200) * 1000000.0;
                                    testerItems[n].setSingleValus(1, d);
                                    if ((factorySetting.getTestWhich() & 0x1) != 0)
                                        if (d > factorySetting.getCurrMax() || d < factorySetting.getCurrMin()) {
                                            testerItems[n].setError(1);
                                            testerItems[n].setError(0);
                                        }
                                } else {
                                    testerItems[n].setError(0);
                                }
                                break;
                            case DET_CAP:
                                if (values[5] != 0) {
                                    tmp = values[6] & 0x00ffff;
                                    d = tmp;
                                    tmp = (values[6] >> 16) & 0x00ffff;
                                    d2 = tmp;
                                    d2 = d2 / d * 100;
                                    d = d / 300 * 100;
                                    testerItems[n].setSingleValus(2, d);
                                    testerItems[n].setSingleValus(3, d2);
                                    if ((factorySetting.getTestWhich() & 0x2) != 0)
                                        if (d > factorySetting.getCapMax() || d < factorySetting.getCapMin()) {
                                            testerItems[n].setError(2);
                                            testerItems[n].setError(0);
                                        }
                                    if ((factorySetting.getTestWhich() & 0x4) != 0)
                                        if (d2 > factorySetting.getBridgeMax() || d2 < factorySetting.getBridgeMin()) {
                                            testerItems[n].setError(3);
                                            testerItems[n].setError(0);
                                        }

                                }
                                break;
                            case DET_FREQ:
                                d = values[6];
                                d = d / 64;
                                testerItems[n].setSingleValus(4, d);
                                d2 = values[7];
                                d2 = d / (d2 / 4096);
                                testerItems[n].setSingleValus(5, d2);
                                if ((factorySetting.getTestWhich() & 0x8) != 0)
                                    if (d > factorySetting.getFreqMax() || d < factorySetting.getFreqMin()) {
                                        testerItems[n].setError(4);
                                        testerItems[n].setError(0);
                                    }
                                if ((factorySetting.getTestWhich() & 0x10) != 0)
                                    if (d2 > factorySetting.getSubMax() || d2 < factorySetting.getSubMin()) {
                                        testerItems[n].setError(5);
                                        testerItems[n].setError(0);
                                    }

                                break;

                            case DET_ERR_STATUS:
                                if (values[4] == ERR_WAIT)
                                    breaking = true;
                                else {
                                    testerItems[n].setSingleValus(7, values[4]);
                                    testerItems[n].setError(7);
                                    testerItems[n].setError(0);
                                }
                                break;
                            case DET_CAP_VOLT:
                                tmp = values[6];
                                checkVoltCorrect(tmp, n);
                                tmp = (values[6] >> 16) & 0x00ffff;
                                checkVoltCorrect(tmp, n + 1);
                                tmp = values[7] & 0x00ffff;
                                checkVoltCorrect(tmp, n + 2);
                                tmp = (values[7] >> 16) & 0x00ffff;
                                checkVoltCorrect(tmp, n + 3);
                                break;
                            case DET_TEST_FINISH:
                                running = false;
                                aviTesting.hide();
                                btStartTest.setEnabled(true);
                                btSetting.setEnabled(true);
                                btStopTest.setEnabled(false);
                                try {
                                    mMediaPlayer.start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                    waitPublish = false;
                    break;
                default:
                    break;

            }
            commEnable = true;

        }

        private void checkVoltCorrect(int tmp, int n) {
            double d;
            d = (tmp & 0x00ffff);
            d = d / 4096.0 * 3.3 * 11;
            testerItems[n].setSingleValus(6, d);
            if ((factorySetting.getTestWhich() & 0x20) != 0)
                if (d > factorySetting.getVoltMax() || d < factorySetting.getVoltMin()) {
                    testerItems[n].setError(5);
                    testerItems[n].setError(0);
                }
        }

        @Override
        protected void onPostExecute(Integer integer) {
//            super.onPostExecute(integer);
            Log.e("commTask", "exit thread " + String.valueOf(cmdType));
            switch (integer) {
                case 0:
                case -1:
                    break;

            }
            commEnable = true;
            battEnable = true;
            scanning = false;

        }

        @Override
        protected void onCancelled() {
//            super.onCancelled();
            Log.e("commTask", "cancel thread " + String.valueOf(cmdType));
            notSendErr = true;
            commEnable = true;
            battEnable = true;
            scanning = false;
        }

        @Override
        protected void onCancelled(Integer integer) {
//            super.onCancelled(integer);
            Log.e("commTask", "cancel thread " + String.valueOf(cmdType));
            notSendErr = true;
            commEnable = true;
            battEnable = true;
            scanning = false;


        }

    }

    @Override
    public boolean isBaseOnWidth() {
        return true;

    }

    @Override
    public float getSizeInDp() {
        return 500;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
