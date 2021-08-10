package com.example.xdyblaster;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.xdyblaster.Adapter.DetonatorAdapter;
import com.example.xdyblaster.Adapter.SectionsPagerAdapter;
import com.example.xdyblaster.fragment.FragmentData;
import com.example.xdyblaster.fragment.FragmentDelete;
import com.example.xdyblaster.fragment.FragmentEdit;
import com.example.xdyblaster.fragment.FragmentFixdelay;
import com.example.xdyblaster.fragment.FragmentLoad;
import com.example.xdyblaster.fragment.FragmentSetDelay;
import com.example.xdyblaster.fragment.FragmentSort;
import com.example.xdyblaster.fragment.FragmentVolt;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;
import com.example.xdyblaster.util.DetonatorDataComparator;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.ObservOverCurrent;
import com.example.xdyblaster.util.ObservVolt;
import com.example.xdyblaster.util.SharedPreferencesUtils;
import com.example.xdyblaster.util.UuidData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.scan.BarcodeReceiver;
import cn.pda.scan.ScanUtil;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

import static android.view.KeyEvent.KEYCODE_POWER;
import static com.example.xdyblaster.MainActivity.actionScan;
import static com.example.xdyblaster.MainActivity.actionStartScan;
import static com.example.xdyblaster.util.AppConstants.ACTION_SCAN_INIT;
import static com.example.xdyblaster.util.AppConstants.BLASTER_TIMER_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_BLIND_SCAN;
import static com.example.xdyblaster.util.CommDetonator.COMM_BLIND_SCAN_PROGRESS;
import static com.example.xdyblaster.util.CommDetonator.COMM_CHECK_ONLINE;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_UUID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_12V;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_9V;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_ON;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_UID;
import static com.example.xdyblaster.util.CommDetonator.COMM_RESET_DETONATOR;
import static com.example.xdyblaster.util.CommDetonator.COMM_SCAN;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_OUTPUT;
import static com.example.xdyblaster.util.CommDetonator.COMM_WAIT_PUBLISH;
import static com.example.xdyblaster.util.CommDetonator.COMM_WRITE_AREA;
import static com.example.xdyblaster.util.FileFunc.getSDPath;
import static com.example.xdyblaster.util.FileFunc.getUuidData;

public class DelayPrjActivity extends AppCompatActivity implements CustomAdapt, DetonatorAdapter.OnItemSelectedListener {
    private static final String[] popupTxtFile = {"1.新建方案", "2.另存方案", "3.打开方案", "4.删除方案", "5.扫描网络"};
    private static final String[] popupTxtArea = {"1.删除雷管", "2.插入雷管", "3.修改参数"};
    private static final String[] popupTxtDetonator = {"1.保存方案", "2.载入方案", "3.历史方案", "4.清除方案", "5.删除雷管", "6.添加雷管", "7.修改延时", "8.扫描网络", "9.排序方式"};


    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvType)
    TextView tvType;
    @BindView(R.id.tvCount)
    TextView tvCount;
    @BindView(R.id.btSetting)
    Button btSetting;
    @BindView(R.id.btEdit)
    Button btEdit;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvArea)
    TextView tvArea;
    @BindView(R.id.btArea)
    Button btArea;
    @BindView(R.id.btDetonator)
    Button btDetonator;
    @BindView(R.id.tvDelay)
    TextView tvDelay;
    @BindView(R.id.tvID)
    TextView tvID;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tvRow)
    TextView tvRow;
    @BindView(R.id.tvHole)
    TextView tvHole;


    private DataViewModel dataViewModel;
    private List<FragmentData> fragments = new ArrayList<>();
    private SectionsPagerAdapter sectionsPagerAdapter;
    private int vpCount;
    private int vpNum;
    private boolean firstBoot, commEnable = true, battEnable = true;
    public boolean checkOnline;
    public boolean powerOn = false;
    private SerialPortUtils serialPortUtils;
    private final String[] recordName = {"R1.net", "R2.net", "R3.net", "R4.net", "R5.net"};
    private final String[] historyName = {"h0.net", "h1.net", "h2.net", "h3.net", "h4.net", "h5.net", "h6.net", "h7.net", "h8.net", "h9.net"};
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (battEnable && commEnable && (!commTask.running)) {
                        commTask.running = false;
                        commTask.cancel(true);
                        commTask = new CommTask(DelayPrjActivity.this);
                        commTask.execute(1, COMM_GET_BATT);
                    }
                    break;
                case 131:
                    if (msg.arg1 == 1) {
                        if (isFocus)
                            startScan(1);
                        else
                            broadCastStartScan();
                    } else if (threadF1Run)
                        threadF1Stop = true;
                    break;
                case 132:
                    if (msg.arg1 == 1)
                        if (isFocus)
                            startScan(2);
                        else
                            broadCastStartScan();
                    else if (threadF1Run)
                        threadF1Stop = true;
                    break;
                case 133:
                    if (msg.arg1 == 1)
                        if (isFocus)
                            startScan(0);
                        else
                            broadCastStartScan();
                    else if (threadF1Run)
                        threadF1Stop = true;
                    break;
                case 888:
                    String tmp = (String) msg.obj;
                    if (!FileFunc.checkUuidString(tmp))
                        break;
                    uuidStr = tmp;
                    scanID = 0;
                    if (isFocus) {
                        if (!checkSameDetonalor(uuidStr, -1)) {
                            addDetonatorData();
                            tvStatus.setText("添加雷管 " + uuidStr);
                        }
                    } else {
                        Intent intent = new Intent(actionScan);
                        intent.putExtra("barcode", uuidStr);
                        sendBroadcast(intent);
                    }
                    scanUtil.stopScan();
                    break;


            }
        }
    };
    boolean threadF1Stop, threadF1Run;
    CommTask commTask;
    FragmentVolt frVolt;
    Timer battTimer = null;
    TimerTask timerTask;
    int detonatorIndex = 0, detonatorDelay = 0, detonatorTime = 0, detonatorArea = 0, detonatorHole = 0;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    MediaPlayer mMediaPlayer2 = new MediaPlayer();
    long scanID;
    String uuidStr;
    long keyTime = 0;
    int f1f2Mode;
    PopupWindow popupMenuOther, popupOpenFile;
    ListView lsvOpenFile;
    //KeyReceiver myReceiver;
    InfoDialog infoDialog;
    int maxCount;
    int itemSelect = -1;
    String[] delayTimeString = new String[5];
    int delayTimeSelect;
    boolean scanning = false;
    int sortType, sortUpDown;
    boolean isFocus;
    FragmentLoad.OnExitListener onExitListener;
    ObservVolt observVolt;
    int oldIndex = -1;
    //Realm mRealm = Realm.getDefaultInstance();
    BarcodeReceiver barcodeReceiver;
    private ScanUtil scanUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_delay_prj);
        ButterKnife.bind(this);

        vpNum = 0;
        firstBoot = true;

        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        if (dataViewModel.fileReload) {
            dataViewModel.fileLoaded = false;
            dataViewModel.fileReload = false;
        }
        //dataViewModel.InitData(fileName);
        //dataViewModel.resetDataList();
        initFragment();
        // myReceiver = new KeyReceiver(this, handler,dataViewModel);
        dataViewModel.keyHandler = handler;
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), fragments, vpCount);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(vpCount - 1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                vpNum = i;
                setPageText();
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
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
        dataViewModel.totalCount.observe(this, new Observer<Integer>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onChanged(@Nullable Integer integer) {
                tvCount.setText(String.format("总数：%d", integer));
            }
        });
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


        commTask = new CommTask(this);
        commTask.execute(3, COMM_IDLE, COMM_GET_BATT, COMM_STOP_OUTPUT);
        powerOn = false;

        barcodeReceiver = new BarcodeReceiver(handler);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(barcodeReceiver, filter);

        Intent intent = new Intent();
        intent.setAction(ACTION_SCAN_INIT);
        sendBroadcast(intent);

        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homePressReceiver, homeFilter);

        //电源键监听
        final IntentFilter batFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBatInfoReceiver, batFilter);
    }


//    private void setTabViewSelected(View view) {
//        TextView textView = (TextView) view.findViewById(R.id.tab_item_textview);
//        textView.setTextSize(24);
//        textView.setTextColor(getResources().getColor(R.color.colorBlack0));
//    }
//
//    private void setTabViewUnSelected(View view) {
//        TextView textView = (TextView) view.findViewById(R.id.tab_item_textview);
//        textView.setTextSize(18);
//        textView.setTextColor(getResources().getColor(R.color.colorBlack3));
//    }
//
//
//    private View getTabView(int currentPosition) {
//        View view = LayoutInflater.from(this).inflate(R.layout.layout_tab, null);
//        TextView textView = (TextView) view.findViewById(R.id.tab_item_textview);
//        textView.setText(String.format("第%d区", currentPosition + 1));
//        textView.setTextSize(18);
//        textView.setTextColor(getResources().getColor(R.color.colorBlack3));
//        view.setBackgroundColor(getResources().getColor(R.color.white));
//        return view;
//    }


    public void setPageText() {

//        String str = String.format("第%d/%d区", vpNum + 1, vpCount);
//        tvArea.setText(str);
    }

    private void initFragment() {
//        dataViewModel.resetDataList();
        dataViewModel.InitData(dataViewModel.fileName);
        vpCount = dataViewModel.detonatorSetting.getRow();
        //vpCount = dataViewModel.detonatorList.size();
        FragmentData[] fragmentData = new FragmentData[vpCount];
        Bundle bundle;
        for (int i = 0; i < vpCount; i++) {
            fragmentData[i] = new FragmentData();
            bundle = new Bundle();
            bundle.putInt("rowNum", i);
            bundle.putBoolean("click", true);
            bundle.putInt("view", 0x03f);
            fragmentData[i].setArguments(bundle);
            fragmentData[i].setItemClickListener(this);
            fragments.add(fragmentData[i]);
        }
    }

    public void broadCastStartScan() {
        Intent intent = new Intent(actionStartScan);
        sendBroadcast(intent);
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
//        dataViewModel.keyHandler = null;
//        mRealm.close();
        unregisterReceiver(barcodeReceiver);
        unregisterReceiver(mBatInfoReceiver);
        unregisterReceiver(homePressReceiver);
        mMediaPlayer.release();
        mMediaPlayer2.release();

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && firstBoot) {
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
            dataViewModel.keyHandler = handler;
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

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btSetting, R.id.btEdit, R.id.btArea, R.id.btDetonator, R.id.tvID, R.id.tvTime, R.id.tvRow, R.id.tvHole})
    public void onViewClicked(View view) {
        if (System.currentTimeMillis() < keyTime + 1000)
            return;
        keyTime = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.btSetting:
                showSettingDialog();
                break;
            case R.id.btEdit:
                startScan(0);
                break;
            case R.id.btArea:
                getDelayString();
                String[] popupString = new String[5];
                popupString[0] = "F1时差" + delayTimeString[0];
                popupString[1] = "F2时差" + delayTimeString[1];
                popupString[2] = "F1时差" + delayTimeString[2];
                popupString[3] = "F2时差" + delayTimeString[3];
                popupString[4] = "固定延时";
                showPopupMenuOther(view, popupString, new AdapterView.OnItemClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        popupMenuOther.dismiss();
                        if(delayTimeSelect != position) {
                            delayTimeSelect = position;
                            SharedPreferencesUtils.setParam(DelayPrjActivity.this,"delayTimeSelect",position);
                        }
                        if(delayTimeSelect==4)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    FragmentFixdelay fragmentFixdelay=new FragmentFixdelay();
                                    fragmentFixdelay.setCancelable(false);
                                    fragmentFixdelay.onFixdelayExitListener=new FragmentFixdelay.OnFixdelayExitListener() {
                                        @Override
                                        public void onFixdelayExit(int index, String str) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    SharedPreferencesUtils.setParam(DelayPrjActivity.this,"fixDelay",Integer.parseInt(str));
                                                    SharedPreferencesUtils.setParam(DelayPrjActivity.this,"fixArea",index);
                                                    loadSetting();
                                                }
                                            });
                                        }
                                    };
                                    fragmentFixdelay.setDefaultCheck((int)SharedPreferencesUtils.getParam(DelayPrjActivity.this,"fixArea",0));
                                    fragmentFixdelay.show(getSupportFragmentManager(),"delay");
                                }
                            });
                        else
                            loadSetting();
                    }
                });
                break;
            case R.id.btDetonator:
                showPopupMenuOther(view, popupTxtDetonator, new AdapterView.OnItemClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        popupMenuOther.dismiss();
                        int i, j;
                        switch (position) {
                            case 0:
                                showOpenFileWindow(position, "保存方案", ListView.CHOICE_MODE_SINGLE, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String tmp;
                                        int i, s;
                                        s = lsvOpenFile.getCheckedItemPosition();
                                        if (s != -1) {
                                            tmp = String.format("R%s.net", s + 1);
                                            Log.d("file", tmp);
                                            FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                                            copySdcardFile(getSDPath() + "//xdyBlaster//default.net", getSDPath() + "//xdyBlaster//record//" + tmp);
                                            popupOpenFile.dismiss();
                                        }
                                    }
                                });
                                break;
                            case 1:
                                showOpenFileWindow(position, "读取方案", ListView.CHOICE_MODE_SINGLE, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String tmp;
                                        int i, s;
                                        s = lsvOpenFile.getCheckedItemPosition();
                                        if (s != -1) {
                                            tmp = lsvOpenFile.getAdapter().getItem(s).toString();
                                            if (!tmp.equals("空")) {
                                                shiftRecord();
                                                tmp = String.format("R%s.net", s + 1);
                                                Log.d("file", tmp);
                                                copySdcardFile(getSDPath() + "//xdyBlaster//record//" + tmp, getSDPath() + "//xdyBlaster//default.net");
                                                dataViewModel.fileLoaded = false;
                                                popupOpenFile.dismiss();
                                                finish();
                                                overridePendingTransition(0, 0);
                                                startActivity(getIntent());
                                                overridePendingTransition(0, 0);
                                            }
                                        }
                                    }
                                });
                                break;
                            case 2:
                                showOpenFileWindow(2, "历史方案", ListView.CHOICE_MODE_SINGLE, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String tmp;
                                        int i, s;
                                        s = lsvOpenFile.getCheckedItemPosition();
                                        if (s != -1) {
                                            tmp = lsvOpenFile.getAdapter().getItem(s).toString();
                                            Log.d("file", tmp);
                                            if (!tmp.equals("空")) {
                                                tmp = String.format("h%s.net", s);
                                                copySdcardFile(getSDPath() + "//xdyBlaster//history//" + tmp, getSDPath() + "//xdyBlaster//default.net");
                                                dataViewModel.fileLoaded = false;
                                                popupOpenFile.dismiss();
                                                finish();
                                                overridePendingTransition(0, 0);
                                                startActivity(getIntent());
                                                overridePendingTransition(0, 0);
                                            }
                                        }
                                    }
                                });
                                break;
                            case 3:
                                InfoDialog dialog = new InfoDialog();
                                dialog.setTitle("清除方案");
                                dialog.setMessage("是否清除全部数据？");
                                dialog.setBtnEnable(true);
                                dialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                                    @Override
                                    public void onButtonClick(int index, String str) {
                                        if (index == 1 && dataViewModel.detonatorDatas.size() != 0) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    shiftRecord();
//                                                    if (dataViewModel.dataChanged) {
//                                                        FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
//                                                        shiftRecord();
//                                                    }
                                                    dataViewModel.detonatorDatas.clear();
                                                    FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                                                    reStart();
                                                }
                                            });
                                        }
                                    }
                                });
                                dialog.show(getSupportFragmentManager(), "info");
                                break;
                            case 4:
                                showEditDialog(0, vpNum);
                                break;
                            case 5:
                                showEditDialog(2, vpNum);
                                break;
                            case 6:
                                showEditDialog(3, vpNum);
                                break;
                            case 7:
                                if (dataViewModel.detonatorDatas.size() == 0) {
                                    infoDialog = new InfoDialog();
                                    infoDialog.setTitle("扫描网络");
                                    infoDialog.setVoltEnable(true);
                                    infoDialog.setLogoColor(0);
                                    infoDialog.setProgressEnable(true);
                                    infoDialog.setMessage(String.format("发现%d发雷管", 0));
                                    infoDialog.setCancelable(false);
                                    infoDialog.setBtn2Enable(true);
                                    infoDialog.setChronometerEnable(true);
                                    infoDialog.setProgressEnable(true);
                                    infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                                        @Override
                                        public void onButtonClick(int index, String str) {
                                            commTask.running = false;
                                            commTask.cancel(true);
                                            commTask = new CommTask(DelayPrjActivity.this);
                                            commTask.execute(1, COMM_IDLE, COMM_STOP_OUTPUT);
                                            powerOn = false;
                                            battEnable = true;
                                        }
                                    });
                                    infoDialog.show(getSupportFragmentManager(), "info");
                                    battEnable = false;
                                    commTask.running = false;
                                    commTask.cancel(true);
                                    commTask = new CommTask(DelayPrjActivity.this);
                                    int h = (int) SharedPreferencesUtils.getParam(DelayPrjActivity.this, "high_scan", 0);
                                    if (h == 1)
                                        commTask.execute(10, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_RESET_DETONATOR,
                                                COMM_BLIND_SCAN, COMM_BLIND_SCAN_PROGRESS, COMM_IDLE,
                                                COMM_GET_UUID_BUFFER, COMM_IDLE, COMM_STOP_OUTPUT);
                                    else
                                        commTask.execute(10, COMM_IDLE, COMM_POWER_12V, COMM_DELAY, COMM_RESET_DETONATOR,
                                                COMM_BLIND_SCAN, COMM_BLIND_SCAN_PROGRESS, COMM_IDLE,
                                                COMM_GET_UUID_BUFFER, COMM_IDLE, COMM_STOP_OUTPUT);
                                    powerOn = false;
                                } else {
                                    infoDialog = new InfoDialog();
                                    infoDialog.setTitle("提示");
                                    infoDialog.setMessage("请先清空网络！");
                                    infoDialog.setBtnEnable(true);
                                    infoDialog.setAutoExit(true);
                                    infoDialog.show(getSupportFragmentManager(), "info");
                                }
                                break;
                            case 8:
                                Bundle bundle = new Bundle();
                                bundle.putInt("sort type", sortType);
                                bundle.putInt("sort up down", sortUpDown);
                                FragmentSort fragmentSort = new FragmentSort();
                                fragmentSort.setArguments(bundle);
                                fragmentSort.onButtonClickListener = new FragmentSort.OnButtonClickListener() {
                                    @Override
                                    public void onButtonClick(int index, int sortType, int sortUpDown) {
                                        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor mEdit = mShare.edit();
                                        mEdit.putInt("sort type", sortType);
                                        mEdit.putInt("sort up down", sortUpDown);
                                        mEdit.apply();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Collections.sort(dataViewModel.detonatorDatas, new DetonatorDataComparator(sortType, sortUpDown));
                                                dataViewModel.dataChanged = true;
                                                reStart();

                                            }
                                        });
                                    }
                                };
                                fragmentSort.show(getSupportFragmentManager(), "sort");
                                break;


                        }


                    }
                });
                break;
            case R.id.tvID:
                resetSortType(1);
                break;
            case R.id.tvTime:
                resetSortType(0);
                break;
            case R.id.tvRow:
                resetSortType(2);
                break;
            case R.id.tvHole:
                resetSortType(3);
                break;

        }
    }

    public void resetSortType(int t) {
        sortUpDown = (int) SharedPreferencesUtils.getParam(this, "sort up down", 0);
        if (t == (int) SharedPreferencesUtils.getParam(this, "sort type", 0)) {
            if (sortUpDown == 0)
                sortUpDown = 1;
            else
                sortUpDown = 0;
            SharedPreferencesUtils.setParam(this, "sort up down", sortUpDown);
        }
        sortType = t;
        SharedPreferencesUtils.setParam(this, "sort type", sortType);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(dataViewModel.detonatorDatas, new DetonatorDataComparator(sortType, sortUpDown));
                dataViewModel.dataChanged = true;
                reStart();
            }
        });
    }


    public void showPopupMenuOther(View view, String[] popupStr, AdapterView.OnItemClickListener onItemClickListener) {
        if (popupMenuOther != null && popupMenuOther.isShowing()) {
            return;
        }
        RelativeLayout layout = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.popup_window, null);
        ListView lsvMore = (ListView) layout.findViewById(R.id.lsvMore);
        lsvMore.setAdapter(new ArrayAdapter<String>(this, R.layout.memu_item, popupStr));
        lsvMore.setOnItemClickListener(onItemClickListener);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);


        popupMenuOther = new PopupWindow(layout, dm.widthPixels / 2, dm.heightPixels / 4 * 3);
        //window.setAnimationStyle(R.style.popup_window_anim);
        //popupMenuOther.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
        //ColorDrawable dw = new ColorDrawable(0xffffffff);
        //popupMenuOther.setBackgroundDrawable(dw);
        popupMenuOther.setFocusable(true);
        popupMenuOther.setOutsideTouchable(true);
//        popupMenuOther.setOnDismissListener(this);
        popupMenuOther.update();
        popupMenuOther.showAsDropDown(view, 0, 0);
        //window.showAtLocation(findViewById(R.id.imageViewAction), Gravity.TOP+Gravity.LEFT,200,0);
    }

    @SuppressLint("DefaultLocale")
    public void showOpenFileWindow(int type, String str, int s, View.OnClickListener onClickListener) {
        if (popupOpenFile != null && popupOpenFile.isShowing()) {
            return;
        }
        int i, j, k, len;
        String tmp0, tmp1;
        List<String> listFileName = new ArrayList<String>();
        File file;
        if (type == 2)
            file = new File(getSDPath() + "//xdyBlaster//history//");
        else
            file = new File(getSDPath() + "//xdyBlaster//record//");
        if (!file.exists()) {
            file.mkdir();
        }
        File[] files = file.listFiles();
        String viewData;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (type == 2) {
//            if (files.length != 0) {
//                for (i = 0; i < files.length; i++) {
//                    if (files[i].isFile()) {
//                        tmp0 = files[i].getName().toString();
//                        len = tmp0.lastIndexOf(".");
//                        if (len != -1) {
//                            tmp1 = tmp0.substring(len + 1, tmp0.length());
//                            if (tmp1.equals("net") && !tmp0.equals("default.net")) {
//                                //tmp1 = tmp0.substring(0, len);
//                                listFileName.add(tmp0);
//                            }
//                        }
//                    }
//                }
//                Collections.sort(listFileName);
            for (j = 0; j < 10; j++) {
                k = 0;
                for (i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        tmp0 = files[i].getName().toString();
                        if (tmp0.equals(historyName[j])) {
                            FileFunc.loadDetonatorSetting("//history//" + tmp0, dataViewModel.detonatorSetting);
                            if (dataViewModel.detonatorSetting.getDate() != 0) {
                                viewData = String.format("%d发 %s", dataViewModel.detonatorSetting.getTotal(), formatter.format(dataViewModel.detonatorSetting.getDate()));
                            } else {
                                viewData = formatter.format(new Date(files[i].lastModified()));
                            }

                            listFileName.add(viewData);
                            k = 1;
                            break;
                        }
                    }
                }
                if (k == 0)
                    listFileName.add("空");
            }
        } else {
            for (j = 0; j < 5; j++) {
                k = 0;
                for (i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        tmp0 = files[i].getName().toString();
                        if (tmp0.equals(recordName[j])) {
                            FileFunc.loadDetonatorSetting("//record//" + tmp0, dataViewModel.detonatorSetting);
                            if (dataViewModel.detonatorSetting.getDate() != 0) {
                                viewData = String.format("%d发 %s", dataViewModel.detonatorSetting.getTotal(), formatter.format(dataViewModel.detonatorSetting.getDate()));
                            } else {
                                viewData = formatter.format(new Date(files[i].lastModified()));
                            }

                            listFileName.add(viewData);
                            k = 1;
                            break;
                        }
                    }
                }
                if (k == 0)
                    listFileName.add("空");
            }
        }


        LinearLayout layout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.layout_open_file, null);
        TextView textView = layout.findViewById(R.id.tv_title);
        textView.setText(str);
        lsvOpenFile = (ListView) layout.findViewById(R.id.listView_open);
        lsvOpenFile.setAdapter(new ArrayAdapter<String>(this, R.layout.item_file, listFileName));
        lsvOpenFile.setChoiceMode(s);
//        lsvOpenFile.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lsvOpenFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //switch (position) {
                //lsvOpenFile.setSelection(position);

                //}

                //window.dismiss();
            }
        });
        popupOpenFile = new PopupWindow(layout, 640, 900);
        //window.setAnimationStyle(R.style.popup_window_anim);
        //popupMenuOther.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
//        ColorDrawable dw = new ColorDrawable(-00000);
//        popupOpenFile.setBackgroundDrawable(dw);
        popupOpenFile.setFocusable(true);
        popupOpenFile.setOutsideTouchable(true);
        popupOpenFile.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(DelayPrjActivity.this, 1.0f);
            }
        });

        Button btnConfirm = layout.findViewById(R.id.bt_confirm);
        btnConfirm.setOnClickListener(onClickListener);
        Button btnCancel = layout.findViewById(R.id.bt_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupOpenFile.dismiss();
            }
        });
        popupOpenFile.update();
        popupOpenFile.showAtLocation(findViewById(R.id.view_pager), Gravity.CENTER, 0, 0);
        setBackgroundAlpha(this, 0.5f);
    }

    public void showEditDialog(int type, int row) {
        serialPortUtils.onKeyDataListener = null;
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putInt("row", detonatorArea);
        FragmentDelete fragmentDelete = new FragmentDelete();
        fragmentDelete.setArguments(bundle);
        fragmentDelete.setCancelable(true);
        fragmentDelete.show(getSupportFragmentManager(), "ConnectDialog");
    }

    public void startScan(int n) {
        if (scanning)
            return;
        if (n != 4)
            findDetonatorValues(n);
        if (f1f2Mode == 0) {
            if (powerOn) {
                battEnable = false;
                checkOnline = false;
                commTask.running = false;
                commTask.cancel(true);
                commTask = new CommTask(DelayPrjActivity.this);
                commTask.setDetonatorValues(detonatorArea, detonatorHole, detonatorDelay, detonatorTime);
                commTask.waitPublish = true;
                commTask.execute(4, COMM_IDLE, COMM_SCAN, COMM_READ_UID, COMM_WAIT_PUBLISH, COMM_WRITE_AREA, COMM_CHECK_ONLINE);
                tvStatus.setText("请接入雷管");
            } else {
                InfoDialog powerDialog;
                powerDialog = new InfoDialog();
                powerDialog.setLogoColor(0);
                powerDialog.setTitle("单发注册");
                powerDialog.setProgressEnable(false);
                powerDialog.setMessage(String.format("是否打开电源单发注册？", 0));
                powerDialog.setCancelable(false);
                powerDialog.setBtnEnable(true);
                powerDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, String str) {
                        if (index == 1) {
                            battEnable = false;
                            checkOnline = false;
                            commTask.running = false;
                            commTask.cancel(true);
                            commTask = new CommTask(DelayPrjActivity.this);
                            commTask.setDetonatorValues(detonatorArea, detonatorHole, detonatorDelay, detonatorTime);
                            commTask.waitPublish = true;
                            commTask.execute(4, COMM_IDLE, COMM_POWER_9V, COMM_DELAY, COMM_SCAN, COMM_READ_UID, COMM_WAIT_PUBLISH, COMM_WRITE_AREA, COMM_CHECK_ONLINE);
                            powerOn = true;
                            tvStatus.setText("请接入雷管");
                        }
                    }
                });
                powerDialog.show(getSupportFragmentManager(), "info");
            }
        } else {
            if (!threadF1Run)
                scanUtil.scan();
        }

    }


    @SuppressLint("SetTextI18n")
    public void findDetonatorValues(int n) {
        SharedPreferences mShare;
        mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        if (delayTimeSelect == 4)
            detonatorArea = mShare.getInt("fixArea", 0);
        else
            detonatorArea = mShare.getInt("area", 0);
        switch (n) {
            case 0:
                if (delayTimeSelect == 0 || delayTimeSelect == 2)
                    detonatorDelay = mShare.getInt("f1", 50);
                else
                    detonatorDelay = mShare.getInt("f2", 50);
                break;
            case 1:
                detonatorDelay = mShare.getInt("f1", 50);
                if (delayTimeSelect >= 2)
                    delayTimeSelect = 2;
                else
                    delayTimeSelect = 0;
                break;
            case 2:
                detonatorDelay = mShare.getInt("f2", 50);
                if (delayTimeSelect >= 2)
                    delayTimeSelect = 3;
                else
                    delayTimeSelect = 1;
                break;
        }
        if (delayTimeSelect >= 2)
            detonatorDelay *= (-1);

        int i = dataViewModel.detonatorDatas.size();
        detonatorHole = 0;
        if (i == 0) {
            detonatorTime = detonatorDelay;
        } else {
            detonatorTime = dataViewModel.detonatorDatas.get(i - 1).getBlasterTime() + detonatorDelay;
            if (detonatorTime < 0)
                detonatorTime = 0;
            detonatorHole = -1;
            for (DetonatorData d : dataViewModel.detonatorDatas) {
                if ((d.getRowNum() == detonatorArea) && (d.getHoleNum() > detonatorHole))
                    detonatorHole = d.getHoleNum();
            }
            detonatorHole++;
        }
        if (delayTimeSelect == 4) {
            tvDelay.setText("固定" + delayTimeString[delayTimeSelect]);
            detonatorTime=Integer.parseInt(delayTimeString[delayTimeSelect]);
        }
        else
            tvDelay.setText("时差" + delayTimeString[delayTimeSelect]);
    }


    public void addDetonatorData() {
        DetonatorData data = new DetonatorData();
        data.setDelay(detonatorDelay);
        data.setBlasterTime(detonatorTime);
        data.setHoleNum(detonatorHole);
        data.setRowNum(detonatorArea);
        data.setId(scanID);
        data.setUuid(uuidStr);
        dataViewModel.detonatorDatas.add(data);
        dataViewModel.detonatorList.get(0).add(data);
        dataViewModel.updateList.get(0).postValue(dataViewModel.detonatorDatas.size() - 1);
        dataViewModel.dataChanged = true;
        //FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
        updateCount();
    }

    public void showSettingDialog() {
        FragmentSetDelay fragmentSetDelay = new FragmentSetDelay();

        fragmentSetDelay.onSetDelayData = new FragmentSetDelay.OnSetDelayData() {
            @Override
            public void onUpdateDelayData(int f1, int f2, int area, int mode) {
                SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
                SharedPreferences.Editor mEdit = mShare.edit();
                mEdit.putInt("f1", f1);
                mEdit.putInt("f2", f2);
                mEdit.putInt("f1f2mode", mode);
                mEdit.putInt("area", area - 1);
                mEdit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadSetting();
                    }
                });

            }
        };
        fragmentSetDelay.setCancelable(false);
        fragmentSetDelay.show(getSupportFragmentManager(), "setDelayDialog");

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void loadSetting() {
        int i;
        String str = "";
        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
//        i = mShare.getInt("f1", 100);
//        str = String.format("F1时差 +%d", i);
//        tvF1Delay.setText(str);
//        i = mShare.getInt("f2", 100);
//        str = String.format("F2时差 +%d", i);
//        tvF2Delay.setText(str);
        sortType = mShare.getInt("sort type", 0);
        sortUpDown = mShare.getInt("sort up down", 0);
        f1f2Mode = mShare.getInt("f1f2mode", 0);
        delayTimeSelect=mShare.getInt("delayTimeSelect", 0);
        //myReceiver.converKey = false;
        if (f1f2Mode == 0)
            str = "脚线";
        if (f1f2Mode == 1) {
            str = "条码";
            //  myReceiver.converKey = true;
        }
        if (f1f2Mode == 2)
            str = "手动";
        tvType.setText(str);
        delayTimeString[0] = "+" + String.valueOf(mShare.getInt("f1", 50));
        delayTimeString[1] = "+" + String.valueOf(mShare.getInt("f2", 50));
        delayTimeString[2] = "-" + String.valueOf(mShare.getInt("f1", 50));
        delayTimeString[3] = "-" + String.valueOf(mShare.getInt("f2", 50));
        delayTimeString[4] = String.valueOf(mShare.getInt("fixDelay", 50));
        if(delayTimeSelect==4)
        {
            tvDelay.setText("固定" + delayTimeString[delayTimeSelect]);
            detonatorArea = mShare.getInt("fixArea", 0);
            }
        else {
            tvDelay.setText("时差" + delayTimeString[delayTimeSelect]);
            detonatorArea = mShare.getInt("area", 0);
        }
        tvArea.setText(String.valueOf(detonatorArea + 1) + "区");
    }

    @Override
    public void OnItemSelected(int item, int n) {
        itemSelect = item;
        switch (n) {
            case 1:
                if (item == -1)
                    break;
                dataViewModel.detonatorDatas.remove(itemSelect);
                dataViewModel.detonatorList.get(0).remove(itemSelect);
                dataViewModel.dataChanged = true;
                fragments.get(0).detonatorAdapter.notifyDataSetChanged();
                updateCount();
                break;
            case 2:
                if (item == -1)
                    break;
                oldIndex = item;
                Bundle bundle = new Bundle();
                bundle.putInt("time", dataViewModel.detonatorDatas.get(item).getBlasterTime());
                bundle.putInt("row", dataViewModel.detonatorDatas.get(item).getRowNum());
                bundle.putInt("hole", dataViewModel.detonatorDatas.get(item).getHoleNum());
                bundle.putString("uuid", dataViewModel.detonatorDatas.get(item).getUuid());
                FragmentEdit fragmentEdit = new FragmentEdit();
                fragmentEdit.setArguments(bundle);
                fragmentEdit.setCancelable(false);
                FragmentEdit.OnExitListener onExitListener = new FragmentEdit.OnExitListener() {
                    @Override
                    public void OnExit(int index, int row, int hole, int time, String uuid) {

                        runOnUiThread(new Runnable() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void run() {
                                switch (index) {
                                    case 0:
                                        fragments.get(0).detonatorAdapter.notifyDataSetChanged();
                                        break;
                                    case 1:
                                        detonatorTime = time;
                                        detonatorArea = row;
                                        detonatorHole = hole;
                                        scanID = 0;
                                        uuidStr = uuid;
                                        if (!checkSameDetonalor(uuidStr, oldIndex)) {
                                            dataViewModel.detonatorDatas.get(item).setBlasterTime(time);
                                            dataViewModel.detonatorDatas.get(item).setRowNum(row);
                                            dataViewModel.detonatorDatas.get(item).setHoleNum(hole);
                                            dataViewModel.detonatorDatas.get(item).setUuid(uuidStr);
                                            dataViewModel.detonatorDatas.get(item).setId(0);
                                            FragmentLoad fragmentLoad = new FragmentLoad();
                                            fragmentLoad.setCancelable(false);
                                            fragmentLoad.show(getSupportFragmentManager(), "load");
                                            dataViewModel.dataChanged = true;
                                        }
                                        break;
                                    case 2:
                                        detonatorTime = time;
                                        detonatorArea = row;
                                        detonatorHole = hole;
                                        scanID = 0;
                                        startScan(4);
                                        break;

                                }

                            }
                        });
                    }
                };
                fragmentEdit.onExitListener = onExitListener;
                fragmentEdit.show(getSupportFragmentManager(), "Edit");
                break;

            case 3:
                if (item == -1)
                    break;
                oldIndex = -1;
                bundle = new Bundle();
                bundle.putInt("time", dataViewModel.detonatorDatas.get(item).getBlasterTime());
                bundle.putInt("row", dataViewModel.detonatorDatas.get(item).getRowNum());
                bundle.putInt("hole", dataViewModel.detonatorDatas.get(item).getHoleNum());
                bundle.putString("uuid", "");
                fragmentEdit = new FragmentEdit();
                fragmentEdit.setArguments(bundle);
                fragmentEdit.setCancelable(false);
                onExitListener = new FragmentEdit.OnExitListener() {
                    @Override
                    public void OnExit(int index, int row, int hole, int time, String uuid) {

                        runOnUiThread(new Runnable() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void run() {
                                switch (index) {
                                    case 0:
                                        fragments.get(0).detonatorAdapter.notifyDataSetChanged();
                                        break;
                                    case 1:
                                        detonatorTime = time;
                                        detonatorArea = row;
                                        detonatorHole = hole;
                                        scanID = 0;
                                        uuidStr = uuid;
                                        if (!checkSameDetonalor(uuidStr, -1)) {
                                            DetonatorData detonatorData = new DetonatorData();
                                            detonatorData.setBlasterTime(time);
                                            detonatorData.setRowNum(row);
                                            detonatorData.setHoleNum(hole);
                                            detonatorData.setUuid(uuid);
                                            detonatorData.setId(0);
                                            dataViewModel.detonatorDatas.add(item, detonatorData);
                                            FragmentLoad fragmentLoad = new FragmentLoad();
                                            fragmentLoad.setCancelable(false);
                                            fragmentLoad.show(getSupportFragmentManager(), "load");
                                            dataViewModel.dataChanged = true;
                                        }
                                        break;
                                    case 2:
                                        detonatorTime = time;
                                        detonatorArea = row;
                                        detonatorHole = hole;
                                        scanID = 0;
                                        startScan(4);
                                        break;

                                }

                            }
                        });
                    }
                };
                fragmentEdit.onExitListener = onExitListener;
                fragmentEdit.show(getSupportFragmentManager(), "Edit");
                break;

        }
    }

    public boolean checkSameDetonalor(String str, int old) {
        boolean same = false;
        int n = 0;
        for (DetonatorData d : dataViewModel.detonatorDatas) {
            if (uuidStr.equals(d.getUuid()) && n != old) {
                same = true;
                break;
            }
            n++;
        }
        if (same) {
            fragments.get(0).detonatorAdapter.opened = -1;
            fragments.get(0).detonatorAdapter.selection = n;
            dataViewModel.updateList.get(0).postValue(n);
            tvStatus.setText("雷管重复");
            try {
                mMediaPlayer2.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent("same.uuid");
            sendBroadcast(intent);
        } else {
            try {
                mMediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return same;
    }


    @SuppressLint("StaticFieldLeak")
    private class CommTask extends CommDetonator {

        public CommTask(DelayPrjActivity context) {
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
                case COMM_READ_UID:
                    if (values[1] == -1) {
                        tvStatus.setText("雷管错误");
                        try {
                            mMediaPlayer2.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        uuidStr = stringUuid;
                        if (checkSameDetonalor(uuidStr, -1))
                            running = false;
                    }
                    waitPublish = false;
                    break;
                case COMM_WRITE_AREA:
//                    if (values[1] != 1) {
//                        tvStatus.setText("雷管错误");
//                        try {
//                            mMediaPlayer2.start();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    break;
//                case COMM_WRITE_TIME:
                    if (values[1] != 1) {
                        tvStatus.setText("雷管错误");
                    } else {
                        try {
                            mMediaPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tvStatus.setText(String.format("添加雷管 %s", uuidStr));
                        if (isFocus) {
                            addDetonatorData();
                        } else {
                            Intent intent = new Intent("add.uuid");
                            intent.putExtra("barcode", uuidStr);
                            sendBroadcast(intent);
                        }
                    }
                    break;
                case COMM_CHECK_ONLINE:
                    breaking = true;
//                    if (values[1] != 1) {
//                        breaking = true;
//                        tvStatus.setText("请接入雷管");
//                        restartScanDetonator();
//                    }
                    break;
                case COMM_BLIND_SCAN:
                    break;
                case COMM_BLIND_SCAN_PROGRESS:
                    try {
                        infoDialog.progressBar.setMax(100);
                        int i = (values[2] >> 24) & 0x0ff;
                        int j = (values[2] >> 16) & 0x0ff;
                        int d = values[2] & 0x0ffff;
                        float f = j;
                        switch (i) {
                            case 0:

                                break;
                            case 1:
                                infoDialog.progressBar.setProgress((float) (f * 40.0 / 16));
                                break;
                            case 2:
                                infoDialog.progressBar.setProgress((float) (f * 20.0 / 16 + 40));
                                break;
                            case 3:
                                infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 60));
                                break;
                            case 4:
                                infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 70));
                                break;
                            case 5:
                                infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 80));
                                break;
                            case 6:
                                infoDialog.progressBar.setProgress((float) (f * 10.0 / 16 + 90));
                                break;
                            case 7:
                                infoDialog.progressBar.setProgress(100);
                                setTotalCount(d, 0);
                                if (d == 0) {
                                    //running = false;
                                    job.set(stepCount + 1, false);
                                    job.set(stepCount + 2, false);
                                    infoDialog.setCancelable(true);
                                    infoDialog.dismissAllowingStateLoss();
                                }
                                breaking = true;
                                break;
                        }
                        infoDialog.setMessageTxt(String.format("发现%d发雷管(%d/7)", d, i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case COMM_GET_UUID_BUFFER:
                    if (values[1] == 1) {
                        try {
                            infoDialog.progressBar.setMax(values[3]);
                            infoDialog.progressBar.setProgress(values[2]);
                            infoDialog.setMessageTxt(String.format("读取数据(%d/%d)", values[2] / 2, values[3] / 2));
                            if (values[2].equals(values[3])) {
                                maxCount = values[3] / 2;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int maxRow = 0;
                                        int maxHole = 0;
                                        int i, d1, d2;
                                        dataViewModel.detonatorDatas.clear();
                                        DetonatorData detonatorData;
                                        for (i = 0; i < maxCount; i++) {
                                            detonatorData = new DetonatorData();
                                            UuidData uuidData = new UuidData();
                                            getUuidData(dataViewModel.uuidBuffer, 16 * i, uuidData);
                                            detonatorData.setId(uuidData.getId());
                                            detonatorData.setRowNum(uuidData.getArea());
                                            detonatorData.setHoleNum(uuidData.getNum());
                                            detonatorData.setBlasterTime(uuidData.getDelay() - BLASTER_TIMER_DELAY);
                                            detonatorData.setUuid(uuidData.getUuid());

//                                            if (dataViewModel.areaBuffer[i] == 0xffffffff)
//                                                dataViewModel.areaBuffer[i] = 0;
//                                            detonatorData.setRowNum(dataViewModel.areaBuffer[i] >>> 16);
//                                            detonatorData.setHoleNum(dataViewModel.areaBuffer[i] & 0x00ffff);
//                                            if (dataViewModel.timerBuffer[i] == 0xffffffff)
//                                                dataViewModel.timerBuffer[i] = 62000;
//                                            detonatorData.setBlasterTime(dataViewModel.timerBuffer[i] - 2000);
                                            if (detonatorData.getRowNum() > maxRow)
                                                maxRow = detonatorData.getRowNum();
                                            if (detonatorData.getHoleNum() > maxHole)
                                                maxHole = detonatorData.getHoleNum();
                                            dataViewModel.detonatorDatas.add(detonatorData);
                                            Collections.sort(dataViewModel.detonatorDatas, new DetonatorDataComparator(sortType, sortUpDown));
                                        }
                                        dataViewModel.detonatorSetting.setHole(String.valueOf(maxHole + 1));
                                        dataViewModel.detonatorSetting.setRow(maxRow + 1);
//                                        maxRow = -1;
//                                        d1 = 0;
//                                        for (i = 0; i < maxCount; i++) {
//                                            if (dataViewModel.detonatorDatas.get(i).getRowNum() != maxRow) {
//                                                d1 = dataViewModel.detonatorDatas.get(i).getBlasterTime();
//                                                dataViewModel.detonatorDatas.get(i).setDelay(d1);
//                                                maxRow = dataViewModel.detonatorDatas.get(i).getRowNum();
//                                            } else {
//                                                d2 = dataViewModel.detonatorDatas.get(i).getBlasterTime();
//                                                dataViewModel.detonatorDatas.get(i).setDelay(d2 - d1);
//                                                d1 = d2;
//                                            }
//                                        }
                                        //FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                                        dataViewModel.dataChanged = true;

//                                        if(!mRealm.isInTransaction()) {
//                                            mRealm.executeTransaction(new Realm.Transaction() {
//                                                @Override
//                                                public void execute(Realm realm) {
//                                                    DailyData dailyData = realm.createObject(DailyData.class, FileFunc.getDate());
//                                                    dailyData.setAct("扫描");
//                                                    dailyData.setMemo("成功 " + "添加" + dataViewModel.detonatorDatas.size() + "发雷管");
//                                                }
//                                            });
//                                        }

//                                        mRealm.beginTransaction();//开启事务
//
//
//                                        DailyData dailyData = mRealm.createObject(DailyData.class, FileFunc.getDate());
//                                        dailyData.setAct("扫描");
//                                        dailyData.setMemo("成功 " + "添加" + dataViewModel.detonatorDatas.size() + "发雷管");
//                                        mRealm.commitTransaction();//提交事务
                                        reStart();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;


            }
            commEnable = true;

        }

        @Override
        protected void onPostExecute(Integer integer) {
//            super.onPostExecute(integer);
//            Log.e("commTask", "exit thread " + String.valueOf(cmdType));
            switch (integer) {
                case 0:
                case -1:
                    break;

            }
            commEnable = true;
            if (step != COMM_GET_BATT) {
                battEnable = true;
            }
            scanning = false;

        }

        @Override
        protected void onCancelled() {
//            super.onCancelled();
            Log.e("commTask", "cancel thread " + String.valueOf(cmdType));
            notSendErr = true;
            commEnable = true;
            if (step != COMM_GET_BATT) {
                battEnable = true;
            }
            scanning = false;
        }

        @Override
        protected void onCancelled(Integer integer) {
//            super.onCancelled(integer);
            Log.e("commTask", "cancel thread " + String.valueOf(cmdType));
            notSendErr = true;
            commEnable = true;
            if (step != COMM_GET_BATT) {
                battEnable = true;
            }
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

    /**
     * 设置页面的透明度
     *
     * @param bgAlpha 1表示不透明
     */
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

    @SuppressLint("DefaultLocale")
    public void updateCount() {
        tvCount.setText(String.format("总数：%d", dataViewModel.detonatorDatas.size()));
    }

    public void getDelayString() {
        SharedPreferences mShare;
        mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        delayTimeString[0] = "+" + String.valueOf(mShare.getInt("f1", 50));
        delayTimeString[1] = "+" + String.valueOf(mShare.getInt("f2", 50));
        delayTimeString[2] = "-" + String.valueOf(mShare.getInt("f1", 50));
        delayTimeString[3] = "-" + String.valueOf(mShare.getInt("f2", 50));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KEYCODE_POWER) {
            Toast.makeText(getApplicationContext(), "电源按下！", Toast.LENGTH_SHORT).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
//        if (dataViewModel.dataChanged) {
//            FragmentLoad fragmentLoad = new FragmentLoad();
//            fragmentLoad.loadFile = false;
//            onExitListener = new FragmentLoad.OnExitListener() {
//                @Override
//                public void OnExit() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dataViewModel.dataChanged = false;
//                            finish();
//                        }
//                    });
//                }
//            };
//            fragmentLoad.onExitListener = onExitListener;
//            fragmentLoad.show(getSupportFragmentManager(), "save");
//        } else
        super.onBackPressed();
    }

    public void reStart() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanUtil == null) {
            scanUtil = new ScanUtil(DelayPrjActivity.this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanUtil.setTimeout("2000");

        }
        scanUtil.setScanMode(0);
    }

    @Override
    protected void onPause() {
        //saveDataChange();
        shiftRecord();
        if (scanUtil != null) {
            scanUtil.setScanMode(1);
//            scanUtil.close();
//            scanUtil = null;
        }
        super.onPause();

    }

    private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    System.out.println("home键监听");
//                    saveDataChange();
                }
            }
        }
    };

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                //         saveDataChange();
                System.out.println("电源键监听");
            }
        }
    };

    private void saveDataChange() {
        if (dataViewModel.dataChanged) {
            FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
            dataViewModel.dataChanged = false;
            dataViewModel.dataShift = true;
        }
    }

    private void shiftRecord() {
        File file = new File(getSDPath() + "//xdyBlaster//history//");
        if (!file.exists()) {
            file.mkdir();
        }
        if (dataViewModel.dataChanged || dataViewModel.dataShift) {
            if (dataViewModel.dataChanged)
                FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
            dataViewModel.dataChanged = false;
            dataViewModel.dataShift = false;
            if (dataViewModel.detonatorDatas.size() != 0) {
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h8.net", getSDPath() + "//xdyBlaster//history//h9.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h7.net", getSDPath() + "//xdyBlaster//history//h8.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h6.net", getSDPath() + "//xdyBlaster//history//h7.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h5.net", getSDPath() + "//xdyBlaster//history//h6.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h4.net", getSDPath() + "//xdyBlaster//history//h5.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h3.net", getSDPath() + "//xdyBlaster//history//h4.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h2.net", getSDPath() + "//xdyBlaster//history//h3.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h1.net", getSDPath() + "//xdyBlaster//history//h2.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//history//h0.net", getSDPath() + "//xdyBlaster//history//h1.net");
                copySdcardFile(getSDPath() + "//xdyBlaster//default.net", getSDPath() + "//xdyBlaster//history//h0.net");
            }
        }
    }

    public int copySdcardFile(String fromFile, String toFile) {
        try {

            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[4096];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }


}



