package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.xdyblaster.Adapter.DetonatorAdapter;
import com.example.xdyblaster.Adapter.SectionsPagerAdapter;
import com.example.xdyblaster.fragment.FragmentData;
import com.example.xdyblaster.fragment.FragmentEdit;
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
import com.example.xdyblaster.util.SharedPreferencesUtils;
import com.example.xdyblaster.util.UuidData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;
import static com.example.xdyblaster.util.AppConstants.BLASTER_TIMER_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_CHECK_NET;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_DETONATE_PROGRESS;
import static com.example.xdyblaster.util.CommDetonator.COMM_DOWNLOAD_DATA;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_ID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_UUID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_ON;
import static com.example.xdyblaster.util.CommDetonator.COMM_PUT_AREA_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_PUT_ID_BUFFER;
import static com.example.xdyblaster.util.CommDetonator.COMM_RESET_DETONATOR;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_OUTPUT;
import static com.example.xdyblaster.util.CommDetonator.COMM_WAIT_PUBLISH;
import static com.example.xdyblaster.util.FileFunc.getUuidData;
import static com.example.xdyblaster.util.FileFunc.loadDetonatorFile;

public class NetActivity extends AppCompatActivity implements CustomAdapt, DetonatorAdapter.OnItemSelectedListener, SerialPortUtils.OnKeyDataListener {

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvArea)
    TextView tvArea;
    @BindView(R.id.tvIndex)
    TextView tvIndex;
    @BindView(R.id.tvID)
    TextView tvID;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.bt_start)
    Button btStart;
    @BindView(R.id.bt_view)
    Button btView;
    @BindView(R.id.bt_download)
    Button btDownload;

    private DataViewModel dataViewModel;
    private List<FragmentData> fragments = new ArrayList<>();
    private SectionsPagerAdapter sectionsPagerAdapter;
    private int vpCount;
    private int vpNum;
    private int index;
    private boolean firstBoot;
    private SerialPortUtils serialPortUtils;
    public int newDetonator;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (battEnable && commEnable) {
                        commTask.cancel(true);
                        commTask = new CommTask(NetActivity.this);
                        commTask.execute(1, COMM_GET_BATT);

                    }
                    break;
                case 131:
                case 132:
                    if (msg.arg1 == 1 && hasFocus)
                        btStart.performClick();
                    break;
            }
        }
    };
    FragmentVolt frVolt;
    CommTask commTask;
    Timer battTimer = null;
    TimerTask timerTask;
    int detonatorIndex = 0, detonatorDelay = 0, detonatorTime = 0, detonatorRow = 0, detonatorHole = 0;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    MediaPlayer mMediaPlayer2 = new MediaPlayer();
    int scanID, scanStatus;
    long keyTime = 0;
    int f1f2Mode;
    PopupWindow popupErrorReport;
    ListView lsvOpenFile;
    //KeyReceiver myReceiver;
    boolean commEnable, battEnable = true, hasFocus = false;
    InfoDialog infoDialog;
    int viewFlag = 0x03f;
    boolean showResult = false;
    FragmentLoad.OnExitListener onExitListener;
    ObservVolt observVolt;
    public long debounceTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_net);
        ButterKnife.bind(this);

        vpNum = 0;
        firstBoot = true;
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        //dataViewModel.InitData(fileName);
        //dataViewModel.resetDataList();
        initFragment();
        //myReceiver = new KeyReceiver(this, handler,dataViewModel);
        dataViewModel.keyHandler = handler;
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), fragments, vpCount);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(vpCount - 1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                vpNum = i;
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
        commTask = new CommTask(this);
        commTask.execute(2, COMM_IDLE, COMM_GET_BATT);
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
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (showResult) {
                    btView.performClick();
                    showResult = false;
                }
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
                if (integer == 2000) {
                    dataViewModel.exit.setValue(1);
                    finish();
                }
            }
        });

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
            bundle.putBoolean("click", false);
            bundle.putInt("view", viewFlag);
            fragmentData[i].setArguments(bundle);
            fragments.add(fragmentData[i]);
            fragmentData[i].setItemClickListener(this);
        }
    }


    @Override
    public boolean onKeyPush(int key, int status) {
        boolean b = false;
        if (status == 1) {
            b = true;
            switch (key) {
                case KEYCODE_1:
                case KEYCODE_2:
                case KEYCODE_3:
                case KEYCODE_4:
                case KEYCODE_5:
                case KEYCODE_6:
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            btSaveFile.performClick();
//                        }
//                    });
                    break;
                case KEYCODE_PAGE_DOWN:
                    dataViewModel.updateList.get(vpNum).postValue(-2);
                    break;
                case KEYCODE_PAGE_UP:
                    dataViewModel.updateList.get(vpNum).postValue(-3);
                    break;
                case KEYCODE_DPAD_DOWN:
                    dataViewModel.updateList.get(vpNum).postValue(-5);
                    break;
                case KEYCODE_DPAD_UP:
                    dataViewModel.updateList.get(vpNum).postValue(-4);
                    break;
                case KEYCODE_DPAD_LEFT:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            vpNum--;
                            if (vpNum < 0)
                                vpNum = 0;
                            viewPager.setCurrentItem(vpNum);
                        }
                    });
                    break;
                case KEYCODE_DPAD_RIGHT:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (vpNum < vpCount - 1)
                                vpNum++;
                            viewPager.setCurrentItem(vpNum);
                        }
                    });
                    break;
                default:
                    b = false;
            }
        }
        return b;
    }

    @Override
    public void onBarcodeScan(String barCode) {

    }

    @Override
    protected void onResume() {
        serialPortUtils.onKeyDataListener = this;
        super.onResume();
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
        dataViewModel.keyHandler = null;
        //unregisterReceiver(myReceiver);
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
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }
            };
            battTimer = new Timer();
            battTimer.schedule(timerTask, 1000, 3000);
        }
        this.hasFocus = hasFocus;
        super.onWindowFocusChanged(hasFocus);
    }


    @OnClick({R.id.bt_start, R.id.bt_view, R.id.bt_download})
    public void onViewClicked(View view) {
        Bundle bundle;
        if ((System.currentTimeMillis() - debounceTime) > 1000)
            debounceTime = System.currentTimeMillis();
        else
            return;

        switch (view.getId()) {
            case R.id.bt_start:
                enableButton(false);
                battEnable = false;
                infoDialog = new InfoDialog();
                infoDialog.setLogoColor(0);
                infoDialog.setVoltEnable(true);
                infoDialog.setTitle("组网测试");
                infoDialog.setMessage("传输数据");
                infoDialog.setProgressEnable(true);
                infoDialog.setCancelable(false);
                infoDialog.setBtn2Enable(true);
                infoDialog.setChronometerEnable(true);
                infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, String str) {
                        commTask.cancel(true);
                        commTask = new CommTask(NetActivity.this);
                        commTask.execute(2, COMM_IDLE, COMM_STOP_OUTPUT);
                        battEnable = true;
                    }
                });
                infoDialog.show(getSupportFragmentManager(), "info");
                if (dataViewModel.dataChanged) {
                    int len = dataViewModel.detonatorDatas.size() - 1;
                    for (int i = len; i >= 0; i--) {
                        switch (dataViewModel.detonatorDatas.get(i).getColor()) {
                            case 0x00:
                                break;
                            case 0x40:
                                dataViewModel.detonatorDatas.remove(i);
                                dataViewModel.dataChanged = true;
                                break;
                            default:
                                dataViewModel.dataChanged = true;
                                dataViewModel.detonatorDatas.get(i).setColor(0);
                                break;
                        }
                    }
                    FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                    dataViewModel.dataChanged = false;
                } else
                    loadDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                for (DetonatorData d : dataViewModel.detonatorDatas) {
                    d.setColor(0);
                }
                commTask.cancel(true);
                commTask = new CommTask(NetActivity.this);
                commTask.setTotalCount(dataViewModel.detonatorDatas.size(), 0);
                commTask.execute(8, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_RESET_DETONATOR,
                        COMM_PUT_AREA_BUFFER, COMM_CHECK_NET, COMM_DETONATE_PROGRESS, COMM_GET_ID_BUFFER, COMM_WAIT_PUBLISH, COMM_GET_UUID_BUFFER, COMM_WAIT_PUBLISH, COMM_IDLE, COMM_STOP_OUTPUT);
                viewFlag = 0x0ff;
                showResult = true;

                break;
            case R.id.bt_view:
                FragmentResult fragmentResult = new FragmentResult();
                bundle = new Bundle();
                bundle.putInt("view", viewFlag);
                fragmentResult.setArguments(bundle);
                fragmentResult.onButtonClickListener = new FragmentResult.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, int flag, int n) {
                        viewFlag = flag;
                        if (n != 0)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int color;
                                    fragments.get(0).detonatorAdapter.clearAllItem();
                                    for (DetonatorData d : dataViewModel.detonatorDatas) {
                                        color = d.getColor();
                                        switch (color) {
                                            case 0x80:
                                                if ((viewFlag & 0x01) != 0)
                                                    fragments.get(0).detonatorAdapter.addItem(d);
                                                break;
                                            case 0x1f:
                                                if ((viewFlag & 0x02) != 0)
                                                    fragments.get(0).detonatorAdapter.addItem(d);
                                                break;
                                            case 0x40:
                                                if ((viewFlag & 0x20) != 0)
                                                    fragments.get(0).detonatorAdapter.addItem(d);
                                                break;
                                            default:
                                                if ((color & 0x01) != 0 && (viewFlag & 0x10) != 0) {
                                                    fragments.get(0).detonatorAdapter.addItem(d);
                                                    break;
                                                }
                                                if ((color & 0x02) != 0 && (viewFlag & 0x08) != 0) {
                                                    fragments.get(0).detonatorAdapter.addItem(d);
                                                    break;
                                                }
                                                if ((color & 0x04) != 0 && (viewFlag & 0x04) != 0) {
                                                    fragments.get(0).detonatorAdapter.addItem(d);
                                                    break;
                                                }

                                                break;
                                        }
                                    }
                                    fragments.get(0).detonatorAdapter.notifyDataSetChanged();
                                }
                            });
                    }
                };
                fragmentResult.show(getSupportFragmentManager(), "result");
                break;
            case R.id.bt_download:
                enableButton(false);
                battEnable = false;
                infoDialog = new InfoDialog();
                infoDialog.setTitle("同步数据");
                infoDialog.setMessage("传输数据");
                infoDialog.setProgressEnable(true);
                infoDialog.setCancelable(false);
                infoDialog.setBtn2Enable(true);
                infoDialog.setChronometerEnable(true);
                infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int index, String str) {
                        commTask.cancel(true);
                        commTask = new CommTask(NetActivity.this);
                        commTask.execute(2, COMM_IDLE, COMM_STOP_OUTPUT);
                        battEnable = true;
                    }
                });
                infoDialog.show(getSupportFragmentManager(), "info");
                commTask.cancel(true);
                commTask = new CommTask(NetActivity.this);
                commTask.execute(8, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_DOWNLOAD_DATA, COMM_IDLE, COMM_STOP_OUTPUT);
                enableButton(false);
                break;
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


        @SuppressLint("DefaultLocale")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int i;
            float p;
            switch (values[0]) {
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
                                infoDialog.progressBar.setMax(100);
                                infoDialog.progressBar.setProgress(100);
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
                            infoDialog.progressBar.setMax(values[3]);
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
                                dataViewModel.dataChanged = true;
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
                                    @Override
                                    public void run() {
                                        battEnable = true;
                                        infoDialog.dismissAllowingStateLoss();
                                        FragmentLoad fragmentLoad = new FragmentLoad();
                                        fragmentLoad.setCancelable(false);
                                        fragmentLoad.show(getSupportFragmentManager(), "load");
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case COMM_DOWNLOAD_DATA:
                    if (values[1] == 1) {
                        try {
                            infoDialog.progressBar.setMax(values[2]);
                            infoDialog.progressBar.setProgress(values[3]);
                            infoDialog.setMessageTxt(String.format("传送数据(%d/%d)", values[3], values[2]));
                            Log.e("read ", String.valueOf(values[2]) + " " + String.valueOf(values[3]));
                            if (values[2].equals(values[3])) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        infoDialog.dismissAllowingStateLoss();
                                        FragmentLoad fragmentLoad = new FragmentLoad();
                                        fragmentLoad.setCancelable(false);
                                        fragmentLoad.show(getSupportFragmentManager(), "load");
                                        battEnable = true;
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
            super.onPostExecute(integer);
            switch (integer) {
                case 0:
                case -1:
                    break;

            }
            commEnable = true;
            enableButton(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            commEnable = true;
            enableButton(true);
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);
            commEnable = true;
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
            enableButton(true);
        }

    }


    @Override
    public void onBackPressed() {
//        dataViewModel.fileLoaded = false;
        int len = dataViewModel.detonatorDatas.size() - 1;
        for (int i = len; i >= 0; i--) {
            switch (dataViewModel.detonatorDatas.get(i).getColor()) {
                case 0x00:
                    break;
                case 0x40:
                    dataViewModel.detonatorDatas.remove(i);
                    dataViewModel.dataChanged = true;
                    break;
                default:
                    dataViewModel.dataChanged = true;
                    dataViewModel.detonatorDatas.get(i).setColor(0);
                    break;
            }
        }
        if (dataViewModel.dataChanged) {
            FragmentLoad fragmentLoad = new FragmentLoad();
            fragmentLoad.loadFile = false;
            onExitListener = new FragmentLoad.OnExitListener() {
                @Override
                public void OnExit() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataViewModel.dataChanged = false;
                            finish();
                        }
                    });
                }
            };
            fragmentLoad.onExitListener = onExitListener;
            fragmentLoad.setCancelable(false);
            fragmentLoad.show(getSupportFragmentManager(), "save");
        } else
            super.onBackPressed();
    }

    public void reStart() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
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
    public void OnItemSelected(int item, int n) {
        DetonatorData d;
        d = fragments.get(0).detonatorAdapter.getItem(item);
        if (d.getColor() == 0x40) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = new InfoDialog();
                    dialog.setTitle("添加雷管");
                    dialog.setMessage("是否添加雷管" + d.getUuid() + "?");
                    dialog.setBtnEnable(true);
                    dialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                        @Override
                        public void onButtonClick(int index, String str) {
                            if (index == 1) {
                                fragments.get(0).detonatorAdapter.setItemColor(item, 0x80);
                                fragments.get(0).detonatorAdapter.notifyDataSetChanged();
                                for (int i = 0; i < dataViewModel.detonatorDatas.size(); i++) {
                                    if (dataViewModel.detonatorDatas.get(i).getUuid().equals(d.getUuid())) {
                                        dataViewModel.detonatorDatas.get(i).setColor(0x80);
                                        dataViewModel.dataChanged = true;
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "info");
                }
            });
        }
    }

    public void enableButton(boolean t) {
        btDownload.setEnabled(t);
        btStart.setEnabled(t);
        btView.setEnabled(t);
    }

}
