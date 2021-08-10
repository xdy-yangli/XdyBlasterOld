package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.mapapi.SDKInitializer;
import com.example.xdyblaster.fragment.FragmentLoad;
import com.example.xdyblaster.fragment.FragmentResult;
import com.example.xdyblaster.fragment.FragmentVolt;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.KeyReceiver;
import com.example.xdyblaster.util.ObservOverCurrent;
import com.example.xdyblaster.util.ObservVolt;
import com.example.xdyblaster.util.ObservVoltDetonattor;
import com.example.xdyblaster.util.SharedPreferencesUtils;
import com.example.xdyblaster.util.UuidData;
import com.xuexiang.xupdate.utils.UpdateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.BLASTER_TIMER_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_ALL_DISCHARGE;
import static com.example.xdyblaster.util.CommDetonator.COMM_CHECK_NET;
import static com.example.xdyblaster.util.CommDetonator.COMM_COUNT_DOWN;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_DETONATE;
import static com.example.xdyblaster.util.CommDetonator.COMM_DETONATE_PROGRESS;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_ID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_UUID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_ON;
import static com.example.xdyblaster.util.CommDetonator.COMM_PUT_AREA_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_PUT_ID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_DEV_ID;
import static com.example.xdyblaster.util.CommDetonator.COMM_RESET_DETONATOR;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_OUTPUT;
import static com.example.xdyblaster.util.CommDetonator.COMM_USER_DEFINE;
import static com.example.xdyblaster.util.CommDetonator.COMM_WAIT_PUBLISH;
import static com.example.xdyblaster.util.FileFunc.getUuidData;
import static com.example.xdyblaster.util.FileFunc.loadDetonatorFile;

public class DetonateActivity extends AppCompatActivity {

    @BindView(R.id.btStart)
    Button btStart;
    @BindView(R.id.btF1)
    Button btF1;
    @BindView(R.id.btF2)
    Button btF2;
    @BindView(R.id.lt_detonate)
    LinearLayout ltDetonate;
    @BindView(R.id.lt_start)
    LinearLayout ltStart;
    @BindView(R.id.tv_count_down)
    TextView tvCountDown;
    @BindView(R.id.iv_count_down)
    ImageView ivCountDown;
    @BindView(R.id.lt_warning)
    LinearLayout ltWarning;
    @BindView(R.id.tv_count_down_title)
    TextView tvCountDownTitle;
    @BindView(R.id.tv_boom)
    TextView tvBoom;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.lt_exit)
    LinearLayout ltExit;
    @BindView(R.id.lt_note)
    LinearLayout ltNote;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;
    // KeyReceiver myReceiver;
    FragmentVolt frVolt;
    Timer battTimer = null;
    TimerTask timerTask;
    CommTask commTask;
    boolean commEnable, battEnable = true, firstBoot, f1 = false, f2 = false, startEnable = true;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    MediaPlayer mMediaPlayer2 = new MediaPlayer();
    MediaPlayer mMediaPlayer3 = new MediaPlayer();
    MediaPlayer mMediaPlayer4 = new MediaPlayer();
    InfoDialog infoDialog;
    PopupWindow popupErrorReport;
    ListView lsvOpenFile;
    ObservVoltDetonattor observVolt;
    int detonateStep = 0;
    int newDetonator;
    boolean isFocus = false;
    public boolean exiting = false;
    //    LocationClient mLocClient;
//    public MyLocationListenner myListener = new MyLocationListenner();
//    LatLng latLng = null;
    boolean charged = false;
    public boolean counting = false;
    long debounceTime = 0;
    public boolean countDownRunning = false;
    public long delayTime;

//    LocationClientOption option = new LocationClientOption();

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (battEnable && commEnable) {
                        commTask.cancel(true);
                        commTask = new CommTask(DetonateActivity.this);
                        commTask.execute(1, COMM_GET_BATT);
                    }
                    break;
                case 131:
                    if (!isFocus)
                        break;
                    if ((ltStart.getVisibility() == View.VISIBLE) && startEnable)
                        btStart.performClick();

                    if ((ltDetonate.getVisibility() == View.VISIBLE) && !counting && !exiting) {
                        if (msg.arg1 == 1) {
                            btF1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_btn));
                            f1 = true;
                        } else {
                            btF1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_btn));
                            f1 = false;
                        }
                        checkStartCountDown();

                    }
                    break;
                case 132:
                    if (!isFocus)
                        break;
                    if ((ltDetonate.getVisibility() == View.VISIBLE) && !counting && !exiting) {
                        if (msg.arg1 == 1) {
                            btF2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_btn));
                            f2 = true;
                        } else {
                            btF2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_btn));
                            f2 = false;
                        }
                        checkStartCountDown();
                    }
                    break;
                case 300:
                    ltDetonate.setVisibility(View.VISIBLE);
                    tvCountDown.setVisibility(View.VISIBLE);
                    ltStart.setVisibility(View.GONE);
                    dataViewModel.checkVolt = 1;
                    break;
                case 301:
                case 1000:
                    break;
                case 999:
                    int max = 0;
                    for (DetonatorData d : dataViewModel.detonatorDatas) {
                        if (d.getBlasterTime() > max)
                            max = d.getBlasterTime();
                    }
                    if (max > 3000) {
                        InfoDialog dialog = new InfoDialog();
                        dialog.setTitle("起爆");
                        dialog.setChronometerEnable(true);
                        dialog.setProgressEnable(true);
                        dialog.setProgressBarMax(max / 1000);
                        dialog.setChronometerCountDown(true);
                        dialog.setCancelable(false);
                        dialog.show(getSupportFragmentManager(), "info");
                    }
                    break;
                case 1999:
                    dischargeDetonater();
                    break;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_detonate);
        ButterKnife.bind(this);

        firstBoot = true;
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        //FileFunc.loadDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
        // myReceiver = new KeyReceiver(this, handler,dataViewModel);
        dataViewModel.keyHandler = handler;
        dataViewModel.checkVolt = 0;
        FragmentManager fragMe = getSupportFragmentManager();
        frVolt = (FragmentVolt) fragMe.findFragmentById(R.id.fr_volt);
        commTask = new CommTask(this);
        commTask.execute(1, COMM_IDLE, COMM_GET_BATT);
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
        try {
            AssetFileDescriptor fd = getAssets().openFd("01.wav");
            mMediaPlayer3.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer3.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AssetFileDescriptor fd = getAssets().openFd("03.wav");
            mMediaPlayer4.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer4.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //    btF1.setBackground(getResources().getDrawable(R.drawable.red_btn));
        ltDetonate.setVisibility(View.GONE);
        tvCountDown.setVisibility(View.GONE);
        btF1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    btF1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_btn));
                    f1 = true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    btF1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_btn));
                    f1 = false;
                }
                checkStartCountDown();
                return true;
            }
        });
        btF2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    btF2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_btn));
                    f2 = true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    btF2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_btn));
                    f2 = false;
                }
                checkStartCountDown();
                return true;
            }
        });

        observVolt = new ObservVoltDetonattor(this, frVolt, getSupportFragmentManager());
        dataViewModel.volt.observe(this, observVolt);
        dataViewModel.volt.setValue(0);
        dataViewModel.overCurrent.observe(this, new ObservOverCurrent(this, frVolt, getSupportFragmentManager()));
        dataViewModel.exit.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 1000) {
                    dataViewModel.exit.setValue(1);
                    dataViewModel.offline.setValue(1);
                    finish();
                }
                if (integer == 2000) {
                    dataViewModel.checkVolt = 0;
                    ltDetonate.setVisibility(View.GONE);
                    dataViewModel.exit.setValue(1);
                    dataViewModel.offline.setValue(1);
                    // dischargeDetonater();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ltDetonate.setVisibility(View.GONE);
                            charged = false;
                            battEnable = false;
                            commTask.cancel(true);
                            commTask = new CommTask(DetonateActivity.this);
                            detonateStep = 2;
                            infoDialog = new InfoDialog();
                            infoDialog.setProgressEnable(true);
                            infoDialog.setChronometerEnable(true);
                            infoDialog.setTitle("安全退出");
                            infoDialog.setMessage("雷管放电");
                            infoDialog.setCancelable(false);
                            infoDialog.show(getSupportFragmentManager(), "info");
                            commTask.execute(1, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_PUT_ID_BUFFER, COMM_ALL_DISCHARGE, COMM_DETONATE_PROGRESS, COMM_STOP_OUTPUT);
                        }
                    });

                }
            }
        });
        dataViewModel.offline.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == -1000) {
                    InfoDialog info = new InfoDialog();
                    info.setTitle("故障");
                    info.setMessage("总线断开！");
                    info.setBtn2Enable(true);
                    info.setCancelable(false);
                    info.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                        @Override
                        public void onButtonClick(int index, String str) {
                            dataViewModel.exit.setValue(1000);
                        }
                    });
                    info.show(getSupportFragmentManager(), "err");
                    dataViewModel.countDown = -1000;

                }
            }
        });
        // 定位初始化
//        mLocClient = new LocationClient(this);
//        mLocClient.registerLocationListener(myListener);
//
//        option.setOpenGps(true); // 打开gps
//        option.setCoorType("bd09ll"); // 设置坐标类型
//        option.setScanSpan(1000);
//        mLocClient.setLocOption(option);
//        mLocClient.start();

        dataViewModel.offline.setValue(1);
        dataViewModel.countDown = 0;
        dataViewModel.overStop = 0;
        dataViewModel.jd = (String) SharedPreferencesUtils.getParam(this, "lng", "0");
        dataViewModel.wd = (String) SharedPreferencesUtils.getParam(this, "lat", "0");
        dataViewModel.htid = (String) SharedPreferencesUtils.getParam(this, "htid", "0");
        dataViewModel.xmbh = (String) SharedPreferencesUtils.getParam(this, "xmbh", "");
        dataViewModel.userId = (String) SharedPreferencesUtils.getParam(this, "bprysfz", "0");
//
           dataViewModel.jd = (String) SharedPreferencesUtils.getParam(this, "jdEdit", "0");
           dataViewModel.wd = (String) SharedPreferencesUtils.getParam(this, "wdEdit", "0");
           FileFunc.saveDetonateResult(dataViewModel);

        if (UpdateUtils.getVersionCode(DetonateActivity.this) < 100)
            dataViewModel.systemCount = 10;

    }

    @Override
    protected void onDestroy() {
        if (battTimer != null) {
            battTimer.cancel();
            timerTask.cancel();
            battTimer = null;
            timerTask = null;
        }
        commTask.cancel(true);
        //unregisterReceiver(myReceiver);
        dataViewModel.keyHandler = null;
        mMediaPlayer.release();
        mMediaPlayer2.release();
        mMediaPlayer3.release();
        mMediaPlayer4.release();
//        mLocClient.stop();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && firstBoot) {
            isFocus = true;
            firstBoot = false;
            //frVolt.setPowerObserve(dataViewModel);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    FragmentLoad fragmentLoad = new FragmentLoad();
                    fragmentLoad.setCancelable(false);
                    fragmentLoad.show(getSupportFragmentManager(), "load");
                }
            });
        }

        if (battTimer != null) {
            battTimer.cancel();
            timerTask.cancel();
            battTimer = null;
            timerTask = null;
        }
        if (hasFocus) {
            isFocus = true;
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }
            };
            battTimer = new Timer();
            battTimer.schedule(timerTask, 1000, 1500);
            if (firstBoot) {
                firstBoot = false;
            }
        } else
            isFocus = false;

        super.onWindowFocusChanged(hasFocus);
    }


    @OnClick({R.id.btStart, R.id.btF1, R.id.btF2, R.id.btExit})
    public void onViewClicked(View view) {
        if ((System.currentTimeMillis() - debounceTime) > 1000)
            debounceTime = System.currentTimeMillis();
        else
            return;
        switch (view.getId()) {
            case R.id.btStart:
                btStart.setEnabled(false);
                dataViewModel.checkVolt = 0;
//                // String str = (String) SharedPreferencesUtils.getParam(DetonateActivity.this, "volt", "0");
                if ((float) SharedPreferencesUtils.getParam(DetonateActivity.this, "volt", 0.0f) < 20) {
                    infoDialog = new InfoDialog();
                    infoDialog.setTitle("故障");
                    infoDialog.setMessage("电压设定低于23伏！");
                    infoDialog.show(getSupportFragmentManager(), "info");
                    btStart.setEnabled(true);
                    break;
                }

                if (detonateStep == 1) {
                    charged = true;
                    battEnable = false;
                    startEnable = false;
                    infoDialog = new InfoDialog();
                    infoDialog.setVoltEnable(true);
                    infoDialog.setTitle("网络充电");
                    infoDialog.setMessage("传输数据");
                    infoDialog.setCancelable(false);
                    infoDialog.setBtn2Enable(true);
                    infoDialog.setProgressEnable(true);
                    infoDialog.setChronometerEnable(true);
                    infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                        @Override
                        public void onButtonClick(int index, String str) {
                            commTask.cancel(true);
                            commTask = new CommTask(DetonateActivity.this);
                            commTask.execute(1, COMM_IDLE);
                            battEnable = true;
                            startEnable = true;
                        }
                    });
                    infoDialog.show(getSupportFragmentManager(), "info");
                    commTask.cancel(true);
                    commTask = new CommTask(DetonateActivity.this);
                    commTask.setTotalCount(dataViewModel.detonatorDatas.size(), 0);
                    commTask.execute(7, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_PUT_ID_BUFFER, COMM_DETONATE, COMM_DETONATE_PROGRESS, COMM_GET_ID_BUFFER, COMM_IDLE);
                    btStart.setEnabled(true);
                } else {
                    exiting = false;
                    dataViewModel.fileReload = true;
                    for (int i = dataViewModel.detonatorDatas.size() - 1; i >= 0; i--)
                        if (dataViewModel.detonatorDatas.get(i).getColor() == 0x40)
                            dataViewModel.detonatorDatas.remove(i);
                    if (dataViewModel.detonatorDatas.size() == 0) {
                        infoDialog = new InfoDialog();
                        infoDialog.setTitle("故障");
                        infoDialog.setMessage("延时方案无数据");
                        infoDialog.show(getSupportFragmentManager(), "info");
                        btStart.setEnabled(true);
                        break;
                    }

//                    if (serialPortUtils.latLng == null) {
//                        infoDialog = new InfoDialog();
//                        infoDialog.setTitle("故障");
//                        infoDialog.setMessage("定位失败！请稍后");
//                        infoDialog.show(getSupportFragmentManager(), "info");
//                        break;
//                    }
                    infoDialog = new InfoDialog();
                    infoDialog.setTitle("授权检查");
                    infoDialog.setLogoColor(0);
                    infoDialog.setMessage("查询授权数据");
                    infoDialog.setCancelable(false);
                    infoDialog.show(getSupportFragmentManager(), "info");
                    battEnable = false;
                    commTask.cancel(true);
                    commTask = new CommTask(DetonateActivity.this);
                    commTask.setTotalCount(dataViewModel.detonatorDatas.size(), 0);

                    if (UpdateUtils.getVersionCode(DetonateActivity.this) > 100)
                        commTask.execute(8, COMM_IDLE, COMM_USER_DEFINE, COMM_WAIT_PUBLISH, COMM_READ_DEV_ID, COMM_POWER_ON, COMM_DELAY, COMM_RESET_DETONATOR,
                                COMM_PUT_AREA_BUFFER, COMM_CHECK_NET, COMM_DETONATE_PROGRESS, COMM_GET_ID_BUFFER, COMM_WAIT_PUBLISH, COMM_GET_UUID_BUFFER, COMM_WAIT_PUBLISH, COMM_IDLE, COMM_STOP_OUTPUT);
                    else
                        commTask.execute(8, COMM_IDLE, COMM_USER_DEFINE, COMM_WAIT_PUBLISH, COMM_READ_DEV_ID, COMM_POWER_ON, COMM_DELAY, COMM_RESET_DETONATOR,
                                COMM_PUT_AREA_BUFFER, COMM_CHECK_NET, COMM_DETONATE_PROGRESS, COMM_GET_ID_BUFFER, COMM_WAIT_PUBLISH, COMM_GET_UUID_BUFFER, COMM_WAIT_PUBLISH, COMM_IDLE, COMM_POWER_ON);

//                    viewFlag = 0x0ff;
//                    showResult = true;

                }
                break;
            case R.id.btExit:
                finish();
                break;
        }
    }


    public void checkStartCountDown() {
        if (counting || exiting)
            return;
        if (f1 && f2) {
            counting = true;
            battEnable = false;
            commEnable = false;
            dataViewModel.checkVolt = 0;
            commTask.cancel(true);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Animation anim = AnimationUtils.loadAnimation(DetonateActivity.this, R.anim.bigger);
                            anim.setFillAfter(true);
                            battEnable = false;
                            ltStart.setVisibility(View.GONE);
                            ltDetonate.setVisibility(View.GONE);
                            tvCountDown.setVisibility(View.GONE);
                            ltWarning.setVisibility(View.GONE);
                            ivCountDown.setImageDrawable(getDrawable(R.mipmap.numeric_3_circle));
                            ivCountDown.startAnimation(anim);
                            ivCountDown.setVisibility(View.VISIBLE);
                            tvCountDownTitle.setVisibility(View.VISIBLE);
                            try {
                                mMediaPlayer3.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    counting = delayMs(900);
                    if (!counting)
                        return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Animation anim = AnimationUtils.loadAnimation(DetonateActivity.this, R.anim.bigger);
                            anim.setFillAfter(true);
                            ivCountDown.setImageDrawable(getDrawable(R.mipmap.numeric_2_circle));
                            ivCountDown.startAnimation(anim);
                            //commTask.cancel(true);
                            countDownRunning = false;
                            commTask = new CommTask(DetonateActivity.this);
                            dataViewModel.countOK = 0;
                            if (dataViewModel.countDown != -1000)
                                commTask.execute(1, COMM_COUNT_DOWN);
                            try {
                                mMediaPlayer3.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    counting = delayMs(900);
                    if (!counting)
                        return;
                    if (dataViewModel.countOK != 1) {
                        if ((dataViewModel.countOK == -1) || (countDownRunning == false)) {
                            dataViewModel.offline.postValue(-1000);
                            return;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Animation anim = AnimationUtils.loadAnimation(DetonateActivity.this, R.anim.bigger);
                            anim.setFillAfter(true);
                            ivCountDown.setImageDrawable(getDrawable(R.mipmap.numeric_1_circle));
                            ivCountDown.startAnimation(anim);
                            try {
                                mMediaPlayer3.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    counting = delayMs(900);
                    if (!counting)
                        return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivCountDown.setVisibility(View.GONE);
                            tvBoom.setVisibility(View.VISIBLE);
                            ltExit.setVisibility(View.VISIBLE);
                            FileFunc.saveDetonateResult(dataViewModel);
                            //   dataViewModel.detonatorDatas.clear();
                            //   FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                            charged = false;
                            counting = false;
                            try {
                                mMediaPlayer4.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            handler.sendMessageDelayed(handler.obtainMessage(999), 200);
                        }
                    });
                }
            });
            battEnable = false;
            f1 = false;
            f2 = false;
            thread.start();
        }
    }

    public boolean delayMs(int ms) {
        long end;
        try {
            end = System.currentTimeMillis() + ms;
            while (true) {
                Thread.sleep(1);
                if (end < System.currentTimeMillis())
                    return true;
                if (dataViewModel.countDown == -1000) {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("detonate", "run: 异常：" + e.toString());
            return true;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CommTask extends CommDetonator {

        public CommTask(Context context) {
            this.serialPortUtils = SerialPortUtils.getInstance(context);
            this.dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
            this.serialPortUtils.sendStop = true;
            commEnable = false;
        }

        @Override
        public void runInBackground(Integer s) {
            if (s == COMM_COUNT_DOWN)
                countDownRunning = true;
            if (s == COMM_USER_DEFINE) {
                loadDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                for (DetonatorData d : dataViewModel.detonatorDatas) {
                    d.setColor(0);
                }
                int i = FileFunc.checkDetonatorAuth((String) SharedPreferencesUtils.getParam(DetonateActivity.this, "htid file", "1"), dataViewModel, serialPortUtils.lat, serialPortUtils.lng);
                //
                //   i = 0;
                if (dataViewModel.systemCount >= 10)
                    i = 0;
                waitPublish = true;
                publishProgress(s, 1, i);

            } else
                super.runInBackground(s);
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int i;
            float p;
            if (detonateStep == 1)
                switch (values[0]) {
                    case COMM_COUNT_DOWN:
                        dataViewModel.countOK = 1;
                        break;
                    case COMM_PUT_ID_BUFFER:
                        if (values[1] == 1) {
                            if (infoDialog.progressBar != null) {
                                infoDialog.progressBar.setMax(1100.0f);
                                infoDialog.progressBar.setSecondaryProgress(100.0f);
                                p = values[2] * 100;
                                p = p / values[3];
                                infoDialog.progressBar.setProgress(p);
                                infoDialog.setMessageTxt(String.format("传输数据(%d/%d)", values[2], values[3]));
                            }
                        }
                        break;
                    case COMM_DETONATE_PROGRESS:
                        if (values[1] == 1) {
                            if (infoDialog.progressBar != null) {
                                i = values[4];
                                i = i << 16;
                                i = i >>> 16;
                                p = i * 100.0f;
                                p = p / dataViewModel.detonatorDatas.size();
                                switch (values[3]) {
                                    case 0:
                                        infoDialog.setMessageTxt(String.format("检查雷管(%d/%d)", i + 1, dataViewModel.detonatorDatas.size()));
                                        break;
                                    case 1:
                                        p = i * 100.0f;
                                        p = p / 256;
                                        infoDialog.setMessageTxt(String.format("频率校准"));
                                        break;
                                    case 2:
                                        infoDialog.setMessageTxt(String.format("频率校准(%d/%d)", i + 1, dataViewModel.detonatorDatas.size()));
                                        break;
                                    case 3:
                                        p = i * 100.0f;
                                        p = p / 256;
                                        infoDialog.setMessageTxt("设定延时");
                                        break;
                                    case 4:
                                        infoDialog.setMessageTxt(String.format("单发充电(%d/%d)", i + 1, dataViewModel.detonatorDatas.size()));
                                        break;
//                                    case 4:
//                                        p = i * 100.0f;
//                                        p = p / 10;
//                                        infoDialog.setMessageTxt(String.format("充电 %d", values[4]));
//                                        break;
                                    case 5:
                                    case 6:
                                        infoDialog.setMessageTxt(String.format("检查雷管状态(%d/%d)", i + 1, dataViewModel.detonatorDatas.size()));
                                        break;
                                    case 7:
                                        breaking = true;
                                        return;
                                    case 8:
                                        p = i * 100.0f;
                                        p = p / 10;
                                        infoDialog.setMessageTxt(String.format("8.等待电压稳定 %d", values[4]));
                                        break;
                                    case 9:
                                        p = i * 100.0f;
                                        p = p / 256;
                                        infoDialog.setMessageTxt(String.format("9.等待电压稳定 %d", values[4]));
                                        if (UpdateUtils.getVersionCode(DetonateActivity.this) > 100)
                                            delayTime = System.currentTimeMillis() + 20000;
                                        else
                                            delayTime = System.currentTimeMillis() + 2000;
                                        break;
                                    case 10:
                                        if (delayTime > System.currentTimeMillis())
                                            break;
                                        p = 100;
                                        infoDialog.setMessageTxt("10.网络正常，可以起爆");
                                        running = false;
                                        breaking = true;
                                        infoDialog.dismiss();
                                        handler.sendMessage(Message.obtain(handler, 300));
                                        battEnable = true;
                                        break;

                                }
                                if (p > 100)
                                    p = 100;
                                infoDialog.progressBar.setMax(1100.0f);
                                infoDialog.progressBar.setProgress(p + 100.0f * (values[3] + 1));
                                infoDialog.progressBar.setSecondaryProgress((values[3] + 2) * 100.0f);
                            }
                        }
                        break;
                    case COMM_GET_ID_BUFFER:
                        if (values[1] == 1) {
                            if (infoDialog.progressBar != null) {
                                p = values[2] * 100;
                                p = p / values[3];
                                infoDialog.progressBar.setMax(100);
                                infoDialog.progressBar.setProgress(p);
                                infoDialog.setMessageTxt(String.format("读取数据(%d/%d)", values[2], values[3]));
                            }
                            if (values[2].equals(values[3])) {
                                infoDialog.dismiss();
                                for (i = 0; i < values[3]; i++) {
                                    if ((dataViewModel.statusBuffer[i] & 0x000000ff) == 0xff)
                                        dataViewModel.detonatorDatas.get(i).setColor(2);
                                    else
                                        dataViewModel.detonatorDatas.get(i).setColor(1);
                                }
                                showErrorWindow();
                            }
                        }
                        break;
                    default:
                        if (values[1] == -1) {
                            if (infoDialog != null)
                                infoDialog.dismiss();
                            running = false;
                        }
                        break;
                }
            if (detonateStep == 0)
                switch (values[0]) {
                    case COMM_READ_DEV_ID:
                        if (values[1] == 1) {
                            byte[] b = new byte[11];
                            b[0] = (byte) (values[2] & 0x0ff);
                            b[1] = (byte) ((values[2] >> 8) & 0x0ff);
                            b[2] = (byte) ((values[2] >> 16) & 0x0ff);
                            b[3] = (byte) ((values[2] >> 24) & 0x0ff);
                            b[4] = (byte) (values[3] & 0x0ff);
                            b[5] = (byte) ((values[3] >> 8) & 0x0ff);
                            b[6] = (byte) ((values[3] >> 16) & 0x0ff);
                            b[7] = (byte) ((values[3] >> 24) & 0x0ff);
                            b[8] = (byte) (values[4] & 0x0ff);
                            b[9] = (byte) ((values[4] >> 8) & 0x0ff);
                            b[10] = (byte) ((values[4] >> 16) & 0x0ff);
                            dataViewModel.devId = new String(b);
                        }
                        break;
                    case COMM_USER_DEFINE:
                        infoDialog.dismiss();
                        if (values[2] == 2) {
                            infoDialog = new InfoDialog();
                            infoDialog.setTitle("故障");
                            infoDialog.setMessage("授权失败！请选择授权文件");
                            infoDialog.show(getSupportFragmentManager(), "info");
                            running = false;
                            break;
                        }
                        if (values[2] == 1) {
                            infoDialog = new InfoDialog();
                            infoDialog.setTitle("故障");
                            infoDialog.setMessage("不在起爆范围！禁止爆破");
                            infoDialog.show(getSupportFragmentManager(), "info");
                            running = false;
                            break;
                        }
                        //   FileFunc.saveDetonateResult(dataViewModel);
                        //   finish();
                        infoDialog = new InfoDialog();
                        infoDialog.setLogoColor(0);
                        infoDialog.setTitle("检查网络");
                        infoDialog.setMessage("传输数据");
                        infoDialog.setVoltEnable(true);
                        infoDialog.setProgressEnable(true);
                        infoDialog.setCancelable(false);
                        infoDialog.setBtn2Enable(true);
                        infoDialog.setChronometerEnable(true);
                        infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                            @Override
                            public void onButtonClick(int index, String str) {
                                commTask.cancel(true);
                                commTask = new CommTask(DetonateActivity.this);
                                commTask.execute(2, COMM_IDLE, COMM_STOP_OUTPUT);
                                battEnable = true;
                            }
                        });
                        infoDialog.show(getSupportFragmentManager(), "info");
                        waitPublish = false;
                        break;

                    case COMM_PUT_AREA_BUFFER:
                        if (values[1] == 1) {
                            if (infoDialog.progressBar != null) {
                                infoDialog.progressBar.setMax(values[3]);
                                infoDialog.progressBar.setProgress(values[2]);
                                infoDialog.setMessageTxt(String.format("传输数据(%d/%d)", values[2], values[3]));
                            }
                        }
                        break;
                    case COMM_CHECK_NET:
                        infoDialog.progressBar.setMax(dataViewModel.detonatorDatas.size());
                        //infoDialog.progressBar.setMax(17);
                        infoDialog.progressBar.setProgress(0);
                        break;
                    case COMM_DETONATE_PROGRESS:
                        try {

                            i = (values[2] >> 24) & 0x0ff;
                            int j = (values[2] >> 16) & 0x0ff;
                            int d = values[2] & 0x0ffff;
                            int s = values[4] & 0x0ffff;
                            float f = j;

                            switch (i) {
                                case 0:

                                    break;
                                case 1:
                                    infoDialog.progressBar.setMax(100);
                                    infoDialog.progressBar.setProgress((float) (f * 40.0 / 16));
                                    break;
                                case 2:
                                    infoDialog.progressBar.setMax(100);
                                    infoDialog.progressBar.setProgress((float) (f * 20.0 / 16 + 40));
                                    break;
                                case 3:
                                    infoDialog.progressBar.setMax(100);
                                    infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 60));
                                    break;
                                case 4:
                                    infoDialog.progressBar.setMax(100);
                                    infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 70));
                                    break;
                                case 5:
                                    infoDialog.progressBar.setMax(100);
                                    infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 80));
                                    break;
                                case 6:
                                    infoDialog.progressBar.setMax(100);
                                    infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 90));
                                    break;
                                case 7:
                                    infoDialog.progressBar.setProgress(100);
                                    infoDialog.progressBar.setMax(17);
                                    infoDialog.progressBar.setProgress(17);
                                    newDetonator = d;
                                    breaking = true;
                                    break;
                                case 10:
                                    infoDialog.progressBar.setProgress(s);
                                    break;

                            }
                            infoDialog.setMessageTxt(String.format("扫描网络 %d", s + d));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case COMM_GET_ID_BUFFER:
                        if (values[1] == 1) {
                            try {
                                p = values[2] * 100;
                                p = p / values[3];
                                infoDialog.progressBar.setProgress(values[2]);
                                infoDialog.setMessageTxt(String.format("读取数据(%d/%d)", values[2], values[3]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (values[2].equals(values[3]) || values[3].equals(0)) {
                                //               infoDialog.dismiss();
                                int color;
                                for (i = 0; i < values[3]; i++) {
                                    color = 0;
//                                if (dataViewModel.detonatorDatas.get(i).getId() == 0)
//                                    continue;
                                    if ((dataViewModel.statusBuffer[i] & 0x000000ff) != 1)
                                        color = 0x01f;
                                    else {
                                        int a, h, t;
                                        h = dataViewModel.areaBuffer[i] & 0x0ffff;
                                        a = h / 1000;
                                        h = h - a * 1000;
                                        t = (dataViewModel.areaBuffer[i] >> 16) & 0x0ffff;
                                        if (h != dataViewModel.detonatorDatas.get(i).getHoleNum()) {
                                            color = 0x01;
                                            dataViewModel.detonatorDatas.get(i).setHoleNumErr(h);
                                        }
                                        if (a != dataViewModel.detonatorDatas.get(i).getRowNum()) {
                                            color += 0x02;
                                            dataViewModel.detonatorDatas.get(i).setRowNumErr(a);
                                        }
                                        if (t != (dataViewModel.detonatorDatas.get(i).getBlasterTime() + BLASTER_TIMER_DELAY)) {
                                            dataViewModel.detonatorDatas.get(i).setBlasterTimeErr(t - BLASTER_TIMER_DELAY);
                                            color += 0x04;
                                        }
                                    }
                                    if (color == 0)
                                        color = 0x80;
                                    dataViewModel.detonatorDatas.get(i).setColor(color);
                                }
                                setTotalCount(newDetonator, 0);
                                Log.e("new det ", String.valueOf(newDetonator));
                                waitPublish = false;
                            }
                        }
                        break;
                    case COMM_GET_UUID_BUFFER:
                        if (values[1] == 1) {
                            try {
                                infoDialog.progressBar.setMax(values[3]);
                                infoDialog.progressBar.setProgress(values[2]);
                                infoDialog.setMessageTxt(String.format("读取数据(%d/%d)", values[2] / 2, values[3] / 2));
                                Log.e("read ", String.valueOf(values[2]) + " " + String.valueOf(values[3]));
                                if (values[2].equals(values[3])) {
                                    DetonatorData detonatorData;
                                    for (i = 0; i < newDetonator; i++) {
                                        UuidData uuidData = new UuidData();
                                        getUuidData(dataViewModel.uuidBuffer, 16 * i, uuidData);
                                        int color = 0;
                                        int j;
                                        for (j = 0; j < dataViewModel.detonatorDatas.size(); j++) {
                                            if (dataViewModel.detonatorDatas.get(j).getUuid().equals(uuidData.getUuid())) {
                                                dataViewModel.detonatorDatas.get(j).setId(uuidData.getId());
                                                if (uuidData.getNum() != dataViewModel.detonatorDatas.get(j).getHoleNum()) {
                                                    color = 0x01;
                                                    dataViewModel.detonatorDatas.get(j).setHoleNumErr(uuidData.getNum());
                                                }

                                                if (uuidData.getArea() != dataViewModel.detonatorDatas.get(j).getRowNum()) {
                                                    color += 0x02;
                                                    dataViewModel.detonatorDatas.get(j).setRowNumErr(uuidData.getArea());
                                                }
                                                if (uuidData.getDelay() != (dataViewModel.detonatorDatas.get(j).getBlasterTime() + BLASTER_TIMER_DELAY)) {
                                                    dataViewModel.detonatorDatas.get(j).setBlasterTimeErr(uuidData.getDelay() - BLASTER_TIMER_DELAY);
                                                    color += 0x04;
                                                }
                                                if (color == 0)
                                                    color = 0x80;
                                                dataViewModel.detonatorDatas.get(j).setColor(color);
                                                break;
                                            }
                                        }
                                        if (j == dataViewModel.detonatorDatas.size()) {
                                            detonatorData = new DetonatorData();
                                            detonatorData.setId(uuidData.getId());
                                            detonatorData.setRowNum(uuidData.getArea());
                                            detonatorData.setHoleNum(uuidData.getNum());
                                            detonatorData.setBlasterTime(uuidData.getDelay() - BLASTER_TIMER_DELAY);
                                            detonatorData.setUuid(uuidData.getUuid());
                                            detonatorData.setColor(0x40);
                                            dataViewModel.detonatorDatas.add(detonatorData);
                                        }
                                    }
                                    waitPublish = false;
                                    runOnUiThread(new Runnable() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void run() {
                                            infoDialog.dismissAllowingStateLoss();


                                            boolean b = true;
                                            for (DetonatorData d : dataViewModel.detonatorDatas)
                                                if (d.getColor() != 0x80) {
                                                    b = false;
                                                    break;
                                                }
                                            if (b) {
                                                detonateStep = 1;
                                                btStart.setText("F1.网络充电");
                                                infoDialog = new InfoDialog();
                                                infoDialog.setTitle("正常");
                                                infoDialog.setLogoColor(2);
                                                infoDialog.setMessage("网络正常，允许充电！");
                                                infoDialog.setCancelable(true);
                                                infoDialog.show(getSupportFragmentManager(), "info");
                                                btStart.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_btn));
                                                try {
                                                    mMediaPlayer.start();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            } else {
                                                try {
                                                    mMediaPlayer2.start();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                FragmentResult fragmentResult = new FragmentResult();
                                                Bundle bundle = new Bundle();
                                                bundle.putInt("view", 0xff);
                                                fragmentResult.setArguments(bundle);
                                                fragmentResult.show(getSupportFragmentManager(), "result");
                                            }

                                        }
                                    });
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        if (values[1] == -1) {
                            if (infoDialog != null)
                                infoDialog.dismiss();
                            running = false;
                        }

                }
            if (detonateStep == 2)
                switch (values[0]) {

                    case COMM_PUT_ID_BUFFER:
                        if (values[1] == 1) {
                            if (infoDialog.progressBar != null) {
                                infoDialog.progressBar.setMax(values[3]);
                                infoDialog.progressBar.setProgress(values[2]);
                                infoDialog.setMessageTxt(String.format("传输数据(%d/%d)", values[2], values[3]));
                            }
                        }
                        break;
                    case COMM_DETONATE_PROGRESS:
                        if (values[1] == 1) {
                            if (infoDialog.progressBar != null) {
                                infoDialog.progressBar.setMax(dataViewModel.detonatorDatas.size());
                                if (values[3] == 0)
                                    infoDialog.progressBar.setProgress(0);
                                else if (values[3] == 1 || values[3] == 2) {
                                    infoDialog.progressBar.setProgress(values[4]);
                                    infoDialog.setMessageTxt(String.format("雷管放电(%d/%d)", values[4], dataViewModel.detonatorDatas.size()));
                                } else if (values[3] == 3) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    });
                                }
                            }
                        }
                        break;
                    default:
                        if (values[1] == -1) {
                            if (infoDialog != null)
                                infoDialog.dismiss();
                            running = false;
                        }

                        break;
                }

            commEnable = true;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            switch (integer) {
                case 0:
                case -1:
                    break;

            }
            commEnable = true;
            btStart.setEnabled(true);
            if (step != COMM_GET_BATT) {
                battEnable = true;
            }

        }

        @Override
        protected void onCancelled() {
            commEnable = true;
            btStart.setEnabled(true);
            if (step != COMM_GET_BATT) {
                battEnable = true;
            }
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }

        @Override
        protected void onCancelled(Integer integer) {
            commEnable = true;
            btStart.setEnabled(true);
            if (step != COMM_GET_BATT) {
                battEnable = true;
            }
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }

    }

    @SuppressLint("DefaultLocale")
    public void showErrorWindow() {
        if (popupErrorReport != null && popupErrorReport.isShowing()) {
            return;
        }
        List<String> listFileName = new ArrayList<String>();
        for (DetonatorData d : dataViewModel.detonatorDatas) {
            if (d.getColor() == 2) {
                if (d.getId() == 0)
                    listFileName.add(String.format("%d区%d孔 缺少雷管编码", d.getRowNum() + 1, d.getHoleNum() + 1));
                else
                    listFileName.add(String.format("%d区%d孔 %s 故障", d.getRowNum() + 1, d.getHoleNum() + 1, d.getUuid()));
            }
        }
        if (listFileName.isEmpty())
            listFileName.add("所有雷管工作正常!");

        LinearLayout layout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.layout_open_file, null);
        TextView textView = layout.findViewById(R.id.tv_title);
        textView.setText("故障记录");
        lsvOpenFile = (ListView) layout.findViewById(R.id.listView_open);
        lsvOpenFile.setAdapter(new ArrayAdapter<String>(this, R.layout.memu_item, listFileName));
        popupErrorReport = new PopupWindow(layout, 640, 900);
        popupErrorReport.setFocusable(true);
        popupErrorReport.setOutsideTouchable(false);
        popupErrorReport.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(DetonateActivity.this, 1.0f);
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popupErrorReport.dismiss();
                        startEnable = true;
                        handler.sendMessage(handler.obtainMessage(1999));
                    }
                }, 100);
            }
        };
        Button btnConfirm = layout.findViewById(R.id.bt_confirm);
        btnConfirm.setOnClickListener(onClickListener);
        Button btnCancel = layout.findViewById(R.id.bt_cancel);
        btnCancel.setOnClickListener(onClickListener);


        popupErrorReport.setTouchInterceptor(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!popupErrorReport.isOutsideTouchable()) {
                    View mView = popupErrorReport.getContentView();
                    if (null != mView)
                        mView.dispatchTouchEvent(event);
                }
                return popupErrorReport.isFocusable() && !popupErrorReport.isOutsideTouchable();
            }
        });

        popupErrorReport.update();
        popupErrorReport.showAtLocation(findViewById(R.id.lt_note), Gravity.CENTER, 0, 0);
        setBackgroundAlpha(this, 0.5f);
    }

    @Override
    public void onBackPressed() {
        if (counting || f1 || f2)
            return;
        exiting = true;
        if (charged) {
            dischargeDetonater();
        } else
            super.onBackPressed();
    }

    public void dischargeDetonater() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InfoDialog confirm = new InfoDialog();
                confirm.setTitle("请确认");
                confirm.setMessage("是否退出起爆流程？");
                confirm.setCancelable(false);
                confirm.setBtnEnable(true);
                confirm.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, String str) {
                        if (index == 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    charged = false;
                                    battEnable = false;
                                    commTask.cancel(true);
                                    commTask = new CommTask(DetonateActivity.this);
                                    detonateStep = 2;
                                    infoDialog = new InfoDialog();
                                    infoDialog.setProgressEnable(true);
                                    infoDialog.setChronometerEnable(true);
                                    infoDialog.setTitle("安全退出");
                                    infoDialog.setMessage("雷管放电");
                                    infoDialog.setCancelable(false);
                                    infoDialog.show(getSupportFragmentManager(), "info");
                                    commTask.execute(1, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_PUT_ID_BUFFER, COMM_ALL_DISCHARGE, COMM_DETONATE_PROGRESS, COMM_STOP_OUTPUT);
                                }
                            });
                        } else
                            exiting = false;
                    }
                });
                confirm.show(getSupportFragmentManager(), "info");
            }
        });
    }

    public static void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

//    public class MyLocationListenner extends BDAbstractLocationListener {
//
//        @SuppressLint("DefaultLocale")
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            // map view 销毁后不在处理新接收的位置
//            if (location == null) {
//                return;
//            }
//            latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            dataViewModel.latLng = latLng;
//        }
//
//        public void onReceivePoi(BDLocation poiLocation) {
//        }
//    }
}
