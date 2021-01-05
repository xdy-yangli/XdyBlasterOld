package com.example.xdyblaster.system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.DelayPrjActivity;
import com.example.xdyblaster.R;
import com.example.xdyblaster.fragment.FragmentVolt;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.GetKey;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.KeyReceiver;
import com.example.xdyblaster.util.ObservVolt;
import com.example.xdyblaster.util.SharedPreferencesUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.scan.BarcodeReceiver;
import cn.pda.scan.ScanUtil;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

import static android.view.KeyEvent.KEYCODE_UNKNOWN;
import static com.example.xdyblaster.MainActivity.actionScan;
import static com.example.xdyblaster.util.CommDetonator.COMM_CHECK_ONLINE;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_ON;
import static com.example.xdyblaster.util.CommDetonator.COMM_SCAN;
import static com.example.xdyblaster.util.CommDetonator.COMM_WRITE_AREA;
import static com.example.xdyblaster.util.CommDetonator.COMM_WRITE_PASSWORD;
import static com.example.xdyblaster.util.CommDetonator.COMM_WRITE_UUID;

public class EncodeActivity extends AppCompatActivity implements CustomAdapt {


    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.btMinus)
    Button btMinus;
    @BindView(R.id.btEncode)
    Button btEncode;
    @BindView(R.id.btPlus)
    Button btPlus;
    @BindView(R.id.etUuid)
    EditText etUuid;
    @BindView(R.id.et_pass)
    EditText etPass;
    @BindView(R.id.getkey)
    GetKey getkey;
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
                        commTask = new CommTask(EncodeActivity.this);
                        commTask.execute(1, COMM_GET_BATT);
                    }
                    break;
                case 131:
                case 132:
                    getkey.requestFocus();
                    getkey.requestFocusFromTouch();
                    hideSoftKeyboard(EncodeActivity.this);
                    if ((scanUtil != null) && (msg.arg1 == 1))
                        scanUtil.scan();
//                    if (msg.arg1 == 1) {
//                        if (isFocus)
//                            startScan(1);
//                    } else if (threadF1Run)
//                        threadF1Stop = true;
                    break;
//                case 132:
//                    if (msg.arg1 == 1)
//                        if (isFocus)
//                            startScan(2);
//                        else if (threadF1Run)
//                            threadF1Stop = true;
//                    break;
                case 133:
                    if (msg.arg1 == 1)
                        if (isFocus)
                            startScan(0);
                        else if (threadF1Run)
                            threadF1Stop = true;
                    break;
                case 135:
                    getkey.requestFocus();
                    getkey.requestFocusFromTouch();
                    hideSoftKeyboard(EncodeActivity.this);
                    break;
                case 888:
                    String tmp = (String) msg.obj;
                    if (!FileFunc.checkUuidString(tmp))
                        break;
                    etUuid.setText(tmp);
                    scanUtil.stopScan();
                    break;

            }
        }
    };

//    Thread threadF1 = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            int count;
//            Intent intent = new Intent();
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setAction("android.rfid.FUN_KEY");
//            intent.putExtra("keyCode", 137);
//            intent.putExtra("keydown", true);
//            count = 20;
//            threadF1Stop = false;
//            threadF1Run = true;
//            while (count > 0) {
//                sendBroadcast(intent);
//                serialPortUtils.DelayMs(100);
//                count--;
//                if (count == 0 || threadF1Stop) {
//                    intent.putExtra("keydown", false);
//                    sendBroadcast(intent);
//                    break;
//                }
//            }
//            Message message = new Message();
//            message.what = 131;
//            message.arg1 = 0;
//            handler.sendMessage(message);
//            threadF1Run = false;
//        }
//    });

    boolean threadF1Stop, threadF1Run;

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
    BarcodeReceiver barcodeReceiver;
    private ScanUtil scanUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_encode);
        ButterKnife.bind(this);
        firstBoot = true;
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.keyHandler = handler;
        //myReceiver = new KeyReceiver(this, handler,dataViewModel);
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
        commTask.execute(3, COMM_IDLE, COMM_GET_BATT, COMM_GET_BATT, COMM_POWER_ON);

        getkey.setFocusable(true);
        getkey.setFocusableInTouchMode(true);
        getkey.keyMulti = new GetKey.KeyMulti() {
            @Override
            public void onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
                if (keyCode == KEYCODE_UNKNOWN) {
                    String tmp = event.getCharacters();
                    if (FileFunc.checkUuidString(tmp)) {
                        uuidStr = tmp;
                        scanID = 0;
                        etUuid.setText(uuidStr);
                    }
                }
            }
        };
        barcodeReceiver = new BarcodeReceiver(handler);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(barcodeReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        threadF1Stop = true;
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
        unregisterReceiver(barcodeReceiver);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && firstBoot) {
            firstBoot = false;
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
                    if (battEnable && commEnable) {
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                }
            };
            battTimer = new Timer();
            battTimer.schedule(timerTask, 100, 2000);
        } else
            isFocus = false;
        super.onWindowFocusChanged(hasFocus);
    }

    @OnClick({R.id.btMinus, R.id.btEncode, R.id.btPlus})
    public void onViewClicked(View view) {
        if (System.currentTimeMillis() < keyTime + 200)
            return;
        keyTime = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.btMinus:
                uuid--;
                uuidStr = FileFunc.makeUuidString(uuidStr.substring(0,8), (int)(uuid / 100L), (int)(uuid % 100L));
                etUuid.setText(uuidStr);
                break;
            case R.id.btEncode:
                startScan(0);
                break;
            case R.id.btPlus:
                uuid++;
                uuidStr = FileFunc.makeUuidString(uuidStr.substring(0,8), (int)(uuid / 100L), (int)(uuid % 100L));
                etUuid.setText(uuidStr);
                break;
        }
    }


    public void startScan(int n) {
        if (scanning)
            return;
        battEnable = false;
        checkOnline = false;
        commTask.running = false;
        commTask.cancel(true);
        commTask = new CommTask(EncodeActivity.this);
        commTask.setDetonatorValues(detonatorArea, detonatorHole, detonatorDelay, detonatorTime);
        commTask.setPassword(password);
        uuidStr = etUuid.getText().toString();
        if (FileFunc.checkUuidString(uuidStr)) {
            commTask.setUuid(uuidStr);
            commTask.waitPublish = true;
            commTask.execute(4, COMM_SCAN, COMM_WRITE_UUID, COMM_WRITE_AREA, COMM_WRITE_PASSWORD, COMM_CHECK_ONLINE);
            tvStatus.setText("请接入雷管");
        } else {
            tvStatus.setText("雷管UUID错误！");
            try {
                mMediaPlayer2.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


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
        uuidStr = mShare.getString("detUuid", "5320409100001");
        assert uuidStr != null;
        uuid = Long.parseLong(uuidStr.substring((8)));
//      uuid = Long.parseLong(uuidStr);
        etUuid.setText(uuidStr);
        password = ThreadLocalRandom.current().nextInt(0, 99999999);
        etPass.setText(String.valueOf(password));

    }

    public void nextUuidPassword() {
        uuid++;
        uuidStr = FileFunc.makeUuidString(uuidStr.substring(0,8), (int)(uuid / 100L), (int)(uuid % 100L));
        etUuid.setText(uuidStr);
        password = ThreadLocalRandom.current().nextInt(0, 99999999);
        etPass.setText(String.valueOf(password));
        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = mShare.edit();
        mEdit.putString("detUuid", uuidStr);
        mEdit.apply();

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
//            super.onProgressUpdate(values);
            if (values[0] == 0)
                commEnable = true;
//            if (!running) {
//                commEnable = true;
//                return;
//            }
            switch (values[0]) {
                case COMM_SCAN:
                    if (values[1] == 1)
                        scanID = values[2] & 0x0ffffffffL;
                    if (values[1] == -1) {
                        tvStatus.setText("雷管错误");
                        try {
                            mMediaPlayer2.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (values[1] == 2)
                        scanning = true;
                    break;
                case COMM_WRITE_UUID:
                case COMM_WRITE_AREA:
                case COMM_WRITE_PASSWORD:
                    if (values[1] != 1) {
                        tvStatus.setText("雷管错误");
                        try {
                            mMediaPlayer2.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case COMM_CHECK_ONLINE:
                    if (values[1] == 1) {
                        breaking = true;
                        tvStatus.setText("注码成功");
                        try {
                            mMediaPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        SharedPreferencesUtils.setParam(EncodeActivity.this, "detUuid", uuidStr);
                        loadSetting();
                        nextUuidPassword();
                    }
                    break;
            }
            commEnable = true;

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


    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanUtil == null) {
            scanUtil = new ScanUtil(EncodeActivity.this);
            //we must set mode to 0 : BroadcastReceiver mode
        }
        scanUtil.setScanMode(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanUtil != null) {
            scanUtil.setScanMode(1);
            //scanUtil.close();
            //scanUtil = null;
        }
    }
}
