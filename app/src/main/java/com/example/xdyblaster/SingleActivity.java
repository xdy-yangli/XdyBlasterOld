package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.fragment.FragmentVolt;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.KeyReceiver;
import com.example.xdyblaster.util.ObservOverCurrent;
import com.example.xdyblaster.util.ObservVolt;
import com.example.xdyblaster.util.SharedPreferencesUtils;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.BLASTER_TIMER_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_CALIBRATE;
import static com.example.xdyblaster.util.CommDetonator.COMM_CHECK_ONLINE;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_CURRENT;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_9V;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_AREA;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_UID;
import static com.example.xdyblaster.util.CommDetonator.COMM_SCAN;
import static com.example.xdyblaster.util.CommDetonator.COMM_TEST_BRIDGE;
import static com.example.xdyblaster.util.CommDetonator.COMM_TEST_FREQ;
import static com.example.xdyblaster.util.CommDetonator.COMM_WAIT_PUBLISH;

public class SingleActivity extends AppCompatActivity {

    @BindView(R.id.avi1)
    AVLoadingIndicatorView avi1;


    @BindView(R.id.tvTest1)
    TextView tvTest1;
    @BindView(R.id.tvResult1)
    TextView tvResult1;
    @BindView(R.id.ivok1)
    ImageView ivok1;
    @BindView(R.id.ivError1)
    ImageView ivError1;
    @BindView(R.id.ltTest1)
    RelativeLayout ltTest1;
    @BindView(R.id.avi2)
    AVLoadingIndicatorView avi2;
    @BindView(R.id.tvTest2)
    TextView tvTest2;
    @BindView(R.id.tvResult2)
    TextView tvResult2;
    @BindView(R.id.ivok2)
    ImageView ivok2;
    @BindView(R.id.ivError2)
    ImageView ivError2;
    @BindView(R.id.ltTest2)
    RelativeLayout ltTest2;
    @BindView(R.id.avi3)
    AVLoadingIndicatorView avi3;
    @BindView(R.id.tvTest3)
    TextView tvTest3;
    @BindView(R.id.tvResult3)
    TextView tvResult3;
    @BindView(R.id.ivok3)
    ImageView ivok3;
    @BindView(R.id.ivError3)
    ImageView ivError3;
    @BindView(R.id.ltTest3)
    RelativeLayout ltTest3;
    @BindView(R.id.avi4)
    AVLoadingIndicatorView avi4;
    @BindView(R.id.tvTest4)
    TextView tvTest4;
    @BindView(R.id.tvResult4)
    TextView tvResult4;
    @BindView(R.id.ivok4)
    ImageView ivok4;
    @BindView(R.id.ivError4)
    ImageView ivError4;
    @BindView(R.id.ltTest4)
    RelativeLayout ltTest4;
    @BindView(R.id.avi5)
    AVLoadingIndicatorView avi5;
    @BindView(R.id.tvTest5)
    TextView tvTest5;
    @BindView(R.id.tvResult5)
    TextView tvResult5;
    @BindView(R.id.ivok5)
    ImageView ivok5;
    @BindView(R.id.ivError5)
    ImageView ivError5;
    @BindView(R.id.ltTest5)
    RelativeLayout ltTest5;
    @BindView(R.id.avi6)
    AVLoadingIndicatorView avi6;
    @BindView(R.id.tvTest6)
    TextView tvTest6;
    @BindView(R.id.tvResult6)
    TextView tvResult6;
    @BindView(R.id.ivok6)
    ImageView ivok6;
    @BindView(R.id.ivError6)
    ImageView ivError6;
    @BindView(R.id.ltTest6)
    RelativeLayout ltTest6;
    @BindView(R.id.avi7)
    AVLoadingIndicatorView avi7;
    @BindView(R.id.tvTest7)
    TextView tvTest7;
    @BindView(R.id.tvResult7)
    TextView tvResult7;
    @BindView(R.id.ivok7)
    ImageView ivok7;
    @BindView(R.id.ivError7)
    ImageView ivError7;
    @BindView(R.id.ltTest7)
    RelativeLayout ltTest7;
//    @BindView(R.id.ivSetTimer)
//    ImageView ivSetTimer;
//    @BindView(R.id.ivCharge)
//    ImageView ivCharge;


    @BindView(R.id.tvStep)
    TextView tvStep;
    @BindView(R.id.aviWait)
    AVLoadingIndicatorView aviWait;

    FragmentVolt frVolt;
    //    @BindView(R.id.btF1)
//    Button btF1;
//    @BindView(R.id.btF1Stop)
//    Button btF1Stop;
//    @BindView(R.id.btF2)
//    Button btF2;
//    @BindView(R.id.btF2Stop)
//    Button btF2Stop;
//    @BindView(R.id.lt_f1)
//    LinearLayout ltF1;
//    @BindView(R.id.lt_f2)
//    LinearLayout ltF2;
//    @BindView(R.id.lt_f2_stop)
//    LinearLayout ltF2Stop;
//    @BindView(R.id.lt_f1_stop)
//    LinearLayout ltF1Stop;
    @BindView(R.id.avi8)
    AVLoadingIndicatorView avi8;
    @BindView(R.id.tvTest8)
    TextView tvTest8;
    @BindView(R.id.tvResult8)
    TextView tvResult8;
    @BindView(R.id.ivok8)
    ImageView ivok8;
    @BindView(R.id.ivError8)
    ImageView ivError8;
    @BindView(R.id.ltTest8)
    RelativeLayout ltTest8;
    @BindView(R.id.iv_speed)
    ImageView ivSpeed;
    @BindView(R.id.layout_speed_Test)
    FrameLayout layoutSpeedTest;
    @BindView(R.id.iv_full)
    ImageView ivFull;
    @BindView(R.id.layout_full_test)
    FrameLayout layoutFullTest;
    @BindView(R.id.tvTestCount)
    TextView tvTestCount;
    private SerialPortUtils serialPortUtils;
    private DataViewModel dataViewModel;
    public float volt = 0, current = 0;
    ImageView[] ivOks = new ImageView[8];
    ImageView[] ivErrors = new ImageView[8];
    AVLoadingIndicatorView[] avis = new AVLoadingIndicatorView[8];
    TextView[] tvResults = new TextView[8];
    MediaPlayer mMediaPlayer = new MediaPlayer();
    MediaPlayer mMediaPlayer2 = new MediaPlayer();
    Timer battTimer = null;
    TimerTask timerTask;
    CommTask commTask;
    int scanID;
    String uuidStr;
    boolean scanErr, scanning = false;
    int scanMode;
    long keyTime = 0;
    ObservVolt observVolt;
    int testCount;
    KeyReceiver myReceiver;
    Realm mRealm = Realm.getDefaultInstance();

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (commTask.running)
                        return;
                    commTask.running = false;
                    commTask.cancel(true);
                    commTask = new CommTask(SingleActivity.this);
                    commTask.execute(1, COMM_GET_BATT);
                    break;
                case 131:
                    if (msg.arg1 == 1) {
                        layoutSpeedTest.performClick();
                    }
                    break;
                case 132:
                    if (msg.arg1 == 1) {
                        layoutFullTest.performClick();
                    }
                    break;
                case 133:
                    if (msg.arg1 == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ivFull.getVisibility() == View.VISIBLE)
                                    layoutFullTest.performClick();
                                else
                                    layoutSpeedTest.performClick();
                            }
                        });

                    }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_single);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.enMonitorVolt = false;
        ltTest1.setVisibility(View.VISIBLE);
        ltTest2.setVisibility(View.VISIBLE);
        ltTest3.setVisibility(View.VISIBLE);
        ltTest4.setVisibility(View.VISIBLE);
        ltTest5.setVisibility(View.GONE);
        ltTest6.setVisibility(View.GONE);
        ltTest7.setVisibility(View.GONE);
        ltTest8.setVisibility(View.GONE);
        aviWait.setVisibility(View.VISIBLE);

        ivOks[0] = ivok1;
        ivOks[1] = ivok2;
        ivOks[2] = ivok3;
        ivOks[3] = ivok4;
        ivOks[4] = ivok5;
        ivOks[5] = ivok6;
        ivOks[6] = ivok7;
        ivOks[7] = ivok8;
        ivErrors[0] = ivError1;
        ivErrors[1] = ivError2;
        ivErrors[2] = ivError3;
        ivErrors[3] = ivError4;
        ivErrors[4] = ivError5;
        ivErrors[5] = ivError6;
        ivErrors[6] = ivError7;
        ivErrors[7] = ivError8;
        avis[0] = avi1;
        avis[1] = avi2;
        avis[2] = avi3;
        avis[3] = avi4;
        avis[4] = avi5;
        avis[5] = avi6;
        avis[6] = avi7;
        avis[7] = avi8;
        tvResults[0] = tvResult1;
        tvResults[1] = tvResult2;
        tvResults[2] = tvResult3;
        tvResults[3] = tvResult4;
        tvResults[4] = tvResult5;
        tvResults[5] = tvResult6;
        tvResults[6] = tvResult7;
        tvResults[7] = tvResult8;
        for (int i = 0; i < 8; i++)
            showTest(i, 0);
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

        FragmentManager fragMe = getSupportFragmentManager();
        frVolt = (FragmentVolt) fragMe.findFragmentById(R.id.fr_volt);

        //aviWait.show();
        //commTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        ltF1.setVisibility(View.GONE);
//        ltF2.setVisibility(View.GONE);
//        ltF1Stop.setVisibility(View.GONE);
//        ltF2Stop.setVisibility(View.GONE);


        ivSpeed.setVisibility(View.VISIBLE);
        observVolt = new ObservVolt(this, frVolt, getSupportFragmentManager());
        dataViewModel.volt.observe(this, observVolt);
        dataViewModel.volt.setValue(0);

        dataViewModel.overCurrent.observe(this, new ObservOverCurrent(this, frVolt, getSupportFragmentManager()));
        dataViewModel.exit.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == 1000) {
                    dataViewModel.exit.setValue(1);
                    finish();
                }
            }
        });
        testCount = (int) SharedPreferencesUtils.getParam(this, "test count", 0);
        tvTestCount.setText("总数：" + testCount);
        commTask = new CommTask(this);
        scanMode = 0;
//        dataViewModel.keyF3.observe(this, new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer integer) {
//                if (integer == 1) {
//                    if (ivFull.getVisibility() == View.VISIBLE)
//                        layoutFullTest.performClick();
//                    else
//                        layoutSpeedTest.performClick();
//                }
//
//            }
//        });
        dataViewModel.keyHandler = handler;
        //    myReceiver = new KeyReceiver(this, handler, dataViewModel);
        if (dataViewModel.systemCount < 10)
            layoutFullTest.setVisibility(View.GONE);

        commTask.execute(5, COMM_IDLE, COMM_GET_BATT, COMM_POWER_9V, COMM_DELAY, COMM_CALIBRATE);
    }

    void showTest(int cnt, int type) {
        if (type == 0) {
            ivOks[cnt].setVisibility(View.INVISIBLE);
            ivErrors[cnt].setVisibility(View.INVISIBLE);
            avis[cnt].setVisibility(View.INVISIBLE);
            tvResults[cnt].setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (battTimer != null) {
            battTimer.cancel();
            timerTask.cancel();
            battTimer = null;
            timerTask = null;
        }
        if (hasFocus) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }
            };
            battTimer = new Timer();
            battTimer.schedule(timerTask, 1000, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (battTimer != null) {
            battTimer.cancel();
            timerTask.cancel();
            battTimer = null;
            timerTask = null;
        }
        commTask.running = false;
        commTask.cancel(true);
        mMediaPlayer.release();
        mMediaPlayer2.release();
        dataViewModel.keyHandler = null;
        //unregisterReceiver(myReceiver);
        mRealm.close();

    }

    @OnClick({R.id.layout_speed_Test, R.id.layout_full_test, R.id.tvTestCount})
    public void onViewClicked(View view) {
        if (scanning)
            return;
        if (commTask.running)
            return;
        switch (view.getId()) {
            case R.id.layout_speed_Test:
                if (ivFull.getVisibility() == View.GONE)
                    scanSpeed();
                ivFull.setVisibility(View.GONE);
                ivSpeed.setVisibility(View.VISIBLE);
                ltTest5.setVisibility(View.GONE);
                ltTest6.setVisibility(View.GONE);
                ltTest7.setVisibility(View.GONE);
                ltTest8.setVisibility(View.GONE);
                break;
            case R.id.layout_full_test:
                if (ivSpeed.getVisibility() == View.GONE)
                    scanFull();
                ivFull.setVisibility(View.VISIBLE);
                ivSpeed.setVisibility(View.GONE);
                ltTest5.setVisibility(View.VISIBLE);
                ltTest6.setVisibility(View.VISIBLE);
                ltTest7.setVisibility(View.VISIBLE);
                ltTest8.setVisibility(View.VISIBLE);
                break;
            case R.id.tvTestCount:
                InfoDialog dialog = new InfoDialog();
                dialog.setTitle("清除计数");
                dialog.setMessage("是否清除测试计数？");
                dialog.setBtnEnable(true);
                dialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, String str) {
                        if (index == 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    testCount = 0;
                                    SharedPreferencesUtils.setParam(SingleActivity.this, "test count", 0);
                                    tvTestCount.setText("总数：" + testCount);
                                }
                            });
                        }
                    }
                });
                dialog.show(getSupportFragmentManager(), "info");
                break;
        }
    }


    private class CommTask extends CommDetonator {
        boolean scanFail = false;

        public CommTask(Context context) {
            this.serialPortUtils = SerialPortUtils.getInstance(context);
            this.dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);

        }


        @SuppressLint("DefaultLocale")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            String str;
            switch (values[0]) {
                case COMM_CALIBRATE:
                    aviWait.hide();
                    str = "请选择测试模式";
                    tvStep.setText(str);
//                    ltF1.setVisibility(View.VISIBLE);
//                    ltF2.setVisibility(View.VISIBLE);
                    break;
                case COMM_SCAN:
                    if (values[1] == 1 && !scanFail) {
//                        for (int i = 0; i < 8; i++)
//                            showTest(i, 0);
//                        long d = values[2] & 0xffffffffL;
//                        tvResult1.setText(String.format("%012d", d));
//                        tvResult1.setVisibility(View.VISIBLE);
//                        ivok1.setVisibility(View.VISIBLE);
//                        avi2.show();
                        scanID = values[2];
                        scanMode = 1;
                        scanErr = false;
                        scanning = true;
//                        tvStep.setText("开始测试雷管");
//                        tvStep.setTextColor(getResources().getColor(R.color.main3));
//                        aviWait.setVisibility(View.VISIBLE);
//                        aviWait.setIndicatorColor(getResources().getColor(R.color.main3));
                    }
                    if (values[1] == -1) {
                        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        tvStep.setText("未扫描到雷管！");
                        aviWait.setVisibility(View.GONE);
                        scanning = false;
                        scanFail = true;
                        try {
                            mMediaPlayer2.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (values[1] == 2)
                        scanning = true;
                    break;
                case COMM_READ_UID:
                    if (values[1] == 1 || values[1] == 3) {
                        for (int i = 0; i < 8; i++)
                            showTest(i, 0);
                        uuidStr = stringUuid;
                        tvResult1.setText(uuidStr);
                        tvResult1.setVisibility(View.VISIBLE);
                        ivok1.setVisibility(View.VISIBLE);
                        avi2.show();
                        tvStep.setText("开始测试雷管");
                        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main3));
                        aviWait.setVisibility(View.VISIBLE);
                        aviWait.setIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.main3));
                        if (values[1] == 3) {
                            SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
                            SharedPreferences.Editor mEdit = mShare.edit();
                            long u = Long.parseLong(stringUuid.substring(8));
                            u++;
//                            mEdit.putString("detUuid", String.valueOf(u));
                            mEdit.putString("detUuid", stringUuid);
                            mEdit.apply();
                        }
                    }
                    if (values[1] == -1) {
                        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        tvStep.setText("未扫描到雷管！");
                        aviWait.setVisibility(View.GONE);
                        scanning = false;
                        scanFail = true;
                        try {
                            mMediaPlayer2.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;


                case COMM_READ_AREA:
                    int a, h;
                    if (values[1] == 1) {
                        h = values[3] & 0x00ffff;
                        a = h / 1000;
                        h = h - a * 1000;
                        tvResult2.setText(String.format("%d区%d孔", a + 1, h + 1));
                        tvResult2.setVisibility(View.VISIBLE);
                        avi2.hide();
                        ivok2.setVisibility(View.VISIBLE);
//                      avi3.show();
//                    } else {
//                        avi2.hide();
//                        ivError2.setVisibility(View.VISIBLE);
//                        scanErr = true;
//                        showDetnatorError();
//                    }
//                    break;
//
//
//                case COMM_GET_TIMER:
//                    if (values[1] == 1) {
                        avi3.hide();
                        a = (values[3] >> 16) & 0x00ffff;
                        if (a == 0x00ffff)
                            tvResult3.setText("未设定");
                        else
                            tvResult3.setText(String.format("%d 毫秒", a - BLASTER_TIMER_DELAY));
                        tvResult3.setVisibility(View.VISIBLE);
                        ivok3.setVisibility(View.VISIBLE);
                        avi4.show();
                    } else {
                        avi3.hide();
                        ivError3.setVisibility(View.VISIBLE);
                        scanErr = true;
                        showDetnatorError();
                    }
                    break;
                case COMM_GET_CURRENT:
                    if (values[1] == 1) {
                        double d = (values[3] & 0x0000ffff);
                        d = (d * 3.3 / 4096 * 7.8 / 60 / 200) * 1000000.0;
                        str = String.format("%2.1fua (%d)", d, values[4]);
                        //str = String.format("%d ma", values[3] & 0x0000ffff);
                        avi4.hide();
                        tvResult4.setVisibility(View.VISIBLE);
                        tvResult4.setText(str);
                        if ((d < 10) && (values[4] > 10)) {
                            ivok4.setVisibility(View.VISIBLE);
                        } else {
                            ivError4.setVisibility(View.VISIBLE);
                            scanErr = true;
                            showDetnatorError();
                            if (d > 10)
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        InfoDialog err = new InfoDialog();
                                        err.setTitle("电流故障");
                                        err.setMessage("电流异常，禁止完整测试！");
                                        err.show(getSupportFragmentManager(), "info");
                                        err.setCancelable(true);
                                    }
                                });
                            running = false;
                            waitPublish = false;
                        }
                        if (ltTest5.getVisibility() == View.VISIBLE) {
                            avi5.show();
                            avi6.show();
                            waitPublish = false;
                        }
                    } else {
                        avi4.hide();
                        ivError4.setVisibility(View.VISIBLE);
                        scanErr = true;
                        showDetnatorError();

                    }
                    break;
                case COMM_TEST_BRIDGE:
                    if (values[1] == 1) {
                        float r;
                        int d0, d1, u;
                        d0 = values[3] & 0x00ffff;
                        d1 = (values[3] >>> 16);
                        u = d0 * 100 / 280;
                        str = String.format("%du", u);
                        avi5.hide();
                        avi6.hide();
                        tvResult5.setVisibility(View.VISIBLE);
                        tvResult5.setText(str);
                        if (u > 200 || u < 60) {
                            ivError5.setVisibility(View.VISIBLE);
                            scanErr = true;
                        } else
                            ivok5.setVisibility(View.VISIBLE);

                        r = d1;
                        r = r / d0 * 100.0f;
                        str = String.format("%.1f 欧姆", r);
                        tvResult6.setVisibility(View.VISIBLE);
                        tvResult6.setText(str);
                        if (r < 1.0 || r > 5.0) {
                            ivError6.setVisibility(View.VISIBLE);
                            scanErr = true;
                        } else
                            ivok6.setVisibility(View.VISIBLE);
                        if (scanErr)
                            showDetnatorError();
                        avi7.show();
                        avi8.show();
                    } else {
                        avi5.hide();
                        avi6.hide();
                        ivError5.setVisibility(View.VISIBLE);
                        ivError6.setVisibility(View.VISIBLE);
                        scanErr = true;
                        showDetnatorError();
                    }
                    break;
                case COMM_TEST_FREQ:
                    if (values[1] == 1) {
                        float f, s;
                        f = values[3];
                        f = f / 64;
                        str = String.format("%.1f kHz", f);
                        avi7.hide();
                        avi8.hide();
                        tvResult7.setVisibility(View.VISIBLE);
                        tvResult7.setText(str);
                        if ((f > 560.0) || (f < 450.0))
                            ivError7.setVisibility(View.VISIBLE);
                        else
                            ivok7.setVisibility(View.VISIBLE);
                        s = values[4];
                        s = f / (s / 4096);
                        str = String.format("%.1f kHz", s);
                        tvResult8.setVisibility(View.VISIBLE);
                        tvResult8.setText(str);
                        if (s > 48 || s < 29)
                            ivError8.setVisibility(View.VISIBLE);
                        else
                            ivok8.setVisibility(View.VISIBLE);
                    } else {
                        avi7.hide();
                        avi8.hide();
                        ivError7.setVisibility(View.VISIBLE);
                        ivError8.setVisibility(View.VISIBLE);
                        scanErr = true;
                        showDetnatorError();
                    }
                    break;
                case COMM_CHECK_ONLINE:
                    if (values[1] == -1) {
                        breaking = true;
                        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        tvStep.setText("请插入测试雷管");
                        aviWait.setVisibility(View.VISIBLE);
                        aviWait.setIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        scanning = false;
                    } else {
                        if (scanning)
                            try {
                                mMediaPlayer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        scanning = false;
                        if (scanErr) {
                            showDetnatorError();
                        } else {
                            tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                            tvStep.setText("雷管工作正常");
                        }
                        aviWait.setVisibility(View.GONE);
//                        if(!mRealm.isInTransaction()) {
//                            mRealm.executeTransaction(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    DailyData dailyData = realm.createObject(DailyData.class, FileFunc.getDate());
//                                    dailyData.setAct("单发");
//                                    dailyData.setMemo("成功 " + uuidStr + " 总数 " + testCount);
//                                }
//                            });

//                            mRealm.beginTransaction();//开启事务
//                            DailyData dailyData = mRealm.createObject(DailyData.class, FileFunc.getDate());
//                            dailyData.setAct("单发");
//                            dailyData.setMemo("成功 " + uuidStr + " 总数 " + testCount);
//                            mRealm.commitTransaction();//提交事务
//                        }
                        testCount += 1;
                        tvTestCount.setText("总数：" + testCount);
                        SharedPreferencesUtils.setParam(SingleActivity.this, "test count", testCount);
                        breaking = true;
                    }
                    break;
            }

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch (integer) {
                case 0:
                case -1:
//                    if (scanMode == 0) {
//                        tvStep.setText("请选择测试模式");
//                        aviWait.setVisibility(View.GONE);
//                    }
                    break;


            }
        }

    }


    public void restartScanDetonator() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                commTask.running = false;
                commTask.cancel(true);
                commTask = new CommTask(SingleActivity.this);
                scanMode = 1;
//                if (ltF2.getVisibility() == View.VISIBLE)
//                    commTask.execute(6, COMM_IDLE,COMM_SCAN, COMM_READ_AREA, COMM_GET_TIMER, COMM_GET_CURRENT, COMM_CHECK_ONLINE);
//                else
//                    commTask.execute(8, COMM_IDLE,COMM_SCAN, COMM_READ_AREA, COMM_GET_TIMER, COMM_GET_CURRENT, COMM_TEST_BRIDGE, COMM_TEST_FREQ, COMM_CHECK_ONLINE);

            }
        }, 200);
    }

    public void showDetnatorError() {
        tvStep.setText("雷管故障，请重测！");
        playErrorSound();

    }

    public void playErrorSound() {
        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        aviWait.setVisibility(View.GONE);
        if (scanning)
            try {
                mMediaPlayer2.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        scanning = false;
    }


    public void scanSpeed() {
        ltTest5.setVisibility(View.GONE);
        ltTest6.setVisibility(View.GONE);
        ltTest7.setVisibility(View.GONE);
        ltTest8.setVisibility(View.GONE);
        for (int i = 0; i < 8; i++)
            showTest(i, 0);
        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        tvStep.setText("请插入测试雷管");
        aviWait.setVisibility(View.VISIBLE);
        aviWait.setIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        commTask.running = false;
        commTask.cancel(true);
        commTask = new CommTask(this);
        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        commTask.setUuid(Objects.requireNonNull(mShare.getString("detUuid", "53200328000001")));
        commTask.execute(6, COMM_IDLE, COMM_SCAN, COMM_READ_UID, COMM_READ_AREA,COMM_GET_CURRENT, COMM_CHECK_ONLINE);
    }

    public void scanFull() {
        ltTest5.setVisibility(View.VISIBLE);
        ltTest6.setVisibility(View.VISIBLE);
        ltTest7.setVisibility(View.VISIBLE);
        ltTest8.setVisibility(View.VISIBLE);

        for (int i = 0; i < 8; i++)
            showTest(i, 0);
        tvStep.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        tvStep.setText("请插入测试雷管");
        aviWait.setVisibility(View.VISIBLE);
        aviWait.setIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        commTask.running = false;
        commTask.cancel(true);
        commTask = new CommTask(this);
        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        commTask.setUuid(Objects.requireNonNull(mShare.getString("detUuid", "53200328000001")));
        commTask.execute(8, COMM_IDLE, COMM_SCAN, COMM_READ_UID, COMM_READ_AREA, COMM_GET_CURRENT, COMM_WAIT_PUBLISH, COMM_TEST_BRIDGE, COMM_TEST_FREQ, COMM_CHECK_ONLINE);

    }


}
