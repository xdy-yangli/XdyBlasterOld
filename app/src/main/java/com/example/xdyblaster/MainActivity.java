package com.example.xdyblaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.example.xdyblaster.ble.BleManager;
import com.example.xdyblaster.fragment.FragmentVolt;
import com.example.xdyblaster.http.OKHttpUpdateHttpService;
import com.example.xdyblaster.system.BleActivity;
import com.example.xdyblaster.system.SystemActivity;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorSetting;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.KeyReceiver;
import com.example.xdyblaster.util.ObservVolt;
import com.example.xdyblaster.util.SharedPreferencesUtils;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.XHttpSDK;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPortFinder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import okhttp3.OkHttpClient;
import pub.devrel.easypermissions.EasyPermissions;
import utils.SerialPortUtils;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static com.example.xdyblaster.util.AppConstants.ID_FACTORY;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_DEV_ID;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_DEV_VER;
import static com.example.xdyblaster.util.CommDetonator.COMM_RESET;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_OUTPUT;
import static com.example.xdyblaster.util.CommDetonator.COMM_UPDATE;
import static com.example.xdyblaster.util.FileFunc.checkFileExists;
import static com.example.xdyblaster.util.FileFunc.getSystemModel;
import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity implements CustomAdapt, EasyPermissions.PermissionCallbacks, SerialPortUtils.OnDataReceiveListener, SerialPortUtils.OnKeyDataListener {


    public static final int REQUESTCODE_FROM_ACTIVITY = 1000;
    public static final int REQUESTCODE_NEW_FILE = 1001;
    public static final int REQUESTCODE_GET_PASSWORD = 1002;
    public static final int REQUEST_CODE_SCAN = 0x0000c0de;
    public static final int REQUESTCODE_BLE_CONNECT = 1003;
    public static final String action = "broadcast.action";
    public static final String actionScan = "broadcast.scan.barcode";
    public static final String actionStartScan = "broadcast.start.scan";
    public static final String actionKeyF3Push = "broadcast.key.f3.push";
    public static final String actionKeyF3Release = "broadcast.key.f3.release";
    public static final int batteryLow = 30;

    public final static String[] perms = {
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    //    @BindView(R.id.imageView)
//    ImageView imageView;

    @BindView(R.id.textView_file)
    TextView textViewFile;

    @BindView(R.id.layout_delay_prj)
    LinearLayout layoutDelayPrj;
    @BindView(R.id.layout_single_test)
    LinearLayout layoutSingleTest;
    @BindView(R.id.layout_net)
    LinearLayout layoutNet;
    @BindView(R.id.layout_charge)
    LinearLayout layoutCharge;
    @BindView(R.id.layout_authorize)
    LinearLayout layoutAuthorize;
    @BindView(R.id.layout_setting)
    LinearLayout layoutSetting;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.divider6)
    View divider6;
    @BindView(R.id.icon5)
    ImageView icon5;
    @BindView(R.id.icon6)
    ImageView icon6;
    @BindView(R.id.divider7)
    View divider7;
    @BindView(R.id.tvVersion)
    TextView tvVersion;
//    @BindView(R.id.layout_update)
//    LinearLayout layoutUpdate;


    private SerialPortUtils serialPortUtils;
    private BleManager bleManager;
    private KeyReceiver keyReceiver;
    private LinkedHashMap<String, String> params;
    private DataViewModel dataViewModel;
    private SerialPortFinder serialPortFinder;
    boolean f1, f2;
    public int lcdWidth;
    InfoDialog infoDialog;
    public byte[] fileData;
    public int fileLen;
    public boolean updateEnable = true;

    FragmentVolt frVolt;
    CommTask commTask;
    int commErr = 0;
    Timer battTimer = null;
    TimerTask timerTask;
    LocationClient mLocClient;
    // public MyLocationListenner myListener = new MyLocationListenner();
    LatLng latLng = null;
    LocationClientOption option = new LocationClientOption();

    LocationManager locationManager;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (!commTask.running) {
                        commTask.cancel(true);
                        commTask = new CommTask(MainActivity.this);
                        if (dataViewModel.devId.isEmpty())
                            commTask.execute(1, COMM_STOP_OUTPUT, COMM_READ_DEV_ID);
                        else if (dataViewModel.devVer.equals("xxxx"))
                            commTask.execute(1, COMM_STOP_OUTPUT, COMM_READ_DEV_VER);
                        else if (dataViewModel.devVer.isEmpty())
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDialog();
                                }
                            });
                            //commTask.execute(1, COMM_STOP_OUTPUT, COMM_READ_DEV_VER);
                        else
                            commTask.execute(1, COMM_STOP_OUTPUT);
                    }
                    break;
                case 131:
                    f1 = (msg.arg1 == 1);
                    break;

                case 132:
                    f2 = (msg.arg1 == 1);
                    break;
            }
        }
    };
    ObservVolt observVolt;
    boolean firstBoot;
    private SDKReceiver mReceiver;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("start", "Boot 开机自动启动");
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        serialPortUtils = SerialPortUtils.getInstance(this);


//        serialPortFinder = new SerialPortFinder();
//        String[] strings = serialPortFinder.getAllDevices();
        if (getSystemModel().equals("ax6737_65_n") || getSystemModel().equals("SG7100") || getSystemModel().equals("k71v1_64_bsp")) {
            try {
                if (getSystemModel().equals("k71v1_64_bsp")) {
                    serialPortUtils.comPort = 0;
                    serialPortUtils.ioPort = 166;
                } else {
                    serialPortUtils.comPort = 13;
                    serialPortUtils.ioPort = 96;
                }
                //mSerialPort.setGPIOlow(96);
                //mSerialPort.power3v3on();
                //mSerialPort.power_3v3off();
                //mSerialPort.close(13);
            } catch (Exception e) {
                e.printStackTrace();

            }
            lcdWidth = 500;
            serialPortUtils.openSerialPortBlaster("/dev/ttyMT1", "/dev/ttyMT2");

        } else {
            serialPortUtils.openSerialPortBlaster("/dev/ttyS2", "/dev/ttyS3");
            lcdWidth = 500;
        }
        super.onCreate(savedInstanceState);
        //alPortUtils.serialPortOk=false;

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //setNavigationBarVisible(this, true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        serialPortUtils.setOnDataReceiveListener(this);
        serialPortUtils.onKeyDataListener = this;
        serialPortUtils.mActivity = this;

        dataViewModel.dataChanged = false;
        dataViewModel.overCurrent.setValue(0);
        dataViewModel.romErr.setValue(0);
        dataViewModel.setFileName("");
        dataViewModel.enMonitorVolt = true;
        dataViewModel.battStatus = 0;
        dataViewModel.fileLoaded = false;
        dataViewModel.ver = serialPortUtils.comPort;
        dataViewModel.devVer = "xxxx";
        dataViewModel.devId = "";
//        try {
//            InputStream is = getAssets().open("code.bin");
//            int fileLen = ((is.available() + 7) / 8) * 8;
//            byte[] fileData = new byte[12];
//            if (fileLen > 0x600) {
//                is.skip(0x500);
//                is.read(fileData, 0, 12);
//                is.close();
//                dataViewModel.devVer = new String(fileData);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //comm = Comm.getInstance(this);
        //comm.setDataViewModel(dataViewModel);

//        dataViewModel.drawableG = ContextCompat.getDrawable(this, R.mipmap.a5);
//        dataViewModel.drawableR = ContextCompat.getDrawable(this, R.mipmap.a5);
//        dataViewModel.drawableB = ContextCompat.getDrawable(this, R.mipmap.a5);
//        dataViewModel.drawableGray = ContextCompat.getDrawable(this, R.mipmap.a5);
//        dataViewModel.drawableG = tintDrawable(dataViewModel.drawableG, ContextCompat.getColor(this, R.color.colorGreen));
//        dataViewModel.drawableR = tintDrawable(dataViewModel.drawableR, ContextCompat.getColor(this, R.color.colorRed));
//        dataViewModel.drawableB = tintDrawable(dataViewModel.drawableB, ContextCompat.getColor(this, R.color.colorBlue));
//        dataViewModel.drawableGray = tintDrawable(dataViewModel.drawableGray, ContextCompat.getColor(this, R.color.colorBlack3));

        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {  //已经获取所要申请的权限，进行下一步处理
            bleManager = BleManager.getInstance(MainActivity.this);
            serialPortUtils.bleManager = BleManager.getInstance(MainActivity.this);
            serialPortUtils.initLocation();
        } else {                    //没有获取，申请定位权限
            EasyPermissions.requestPermissions(MainActivity.this, "请求获得设备权限", 1, perms);
        }

        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = mShare.edit();

        int v = mShare.getInt("file", 0);
        if (v != 0) {
            if (!checkFileExists(mShare.getString("filename", "default.net")))
                v = 0;
        }
        if (v == 0) {
            DetonatorSetting setting = new DetonatorSetting();
            setting.setHole("0");
            setting.setHoleDelay("100");
            setting.setRow("1");
            setting.setRowDelay("0");
            setting.setCnt("1");
            setting.setRowSequence(false);
            mEdit.putInt("file", 1);
            mEdit.putString("filename", "default.net");
            mEdit.apply();
            String str = mShare.getString("filename", "default.net");
            setOpenFileName(str);
        } else {
            String str = mShare.getString("filename", "default.net");
            setOpenFileName(str);
        }
        v = mShare.getInt("f1f2mode", -1);
        if (v == -1) {
            mEdit.putInt("f1", 50);
            mEdit.putInt("f2", 100);
            mEdit.putInt("f1f2mode", 0);
            mEdit.putInt("area", 0);
            mEdit.apply();
        }
        float volt = mShare.getFloat("volt", 0.0f);
        if (volt == 0) {
            mEdit.putFloat("volt", 24.0f);
            mEdit.apply();
            volt = 24.0f;
        }
        dataViewModel.defaultVolt = (int) (volt * 100.0);

        FragmentManager fragMe = getSupportFragmentManager();
        frVolt = (FragmentVolt) fragMe.findFragmentById(R.id.fr_volt);

        observVolt = new ObservVolt(this, frVolt, getSupportFragmentManager());
        dataViewModel.volt.observe(this, observVolt);
        commTask = new CommTask(this);
        commTask.execute(1, COMM_STOP_OUTPUT, COMM_READ_DEV_ID, COMM_READ_DEV_VER, COMM_GET_BATT);
        dataViewModel.volt.setValue(100);
        firstBoot = true;

        // 注册 SDK 广播监听者
//        IntentFilter iFilter = new IntentFilter();
//        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
//        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
//        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
//        mReceiver = new SDKReceiver();
//        registerReceiver(mReceiver, iFilter);

//        mLocClient = new LocationClient(this);
//        mLocClient.registerLocationListener(myListener);
//        option.setOpenGps(true); // 打开gps
//        option.setCoorType("bd09ll"); // 设置坐标类型
//        option.setScanSpan(1000);
//        mLocClient.setLocOption(option);
//        mLocClient.start();

//        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
//            // 转到手机设置界面，用户设置GPS
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        }
        dataViewModel.keyHandler = handler;
        dataViewModel.keyHandler = null;
        keyReceiver = new KeyReceiver(this, handler, dataViewModel);
        //     keyThread.start();
        tvVersion.setText("软件版本: " + UpdateUtils.getVersionName(this));
        initXHttp();
        initOKHttpUtils();
        initUpdate();
        String str = (String) SharedPreferencesUtils.getParam(MainActivity.this, "devId", "");
        if(str.equals(ID_FACTORY)) {
            Intent intent = new Intent(MainActivity.this, SystemActivity.class);
            startActivity(intent);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        serialPortUtils.onKeyDataListener = this;
        dataViewModel.keyHandler = handler;
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        serialPortUtils.closeSerialPort();
        if (bleManager != null) {
            bleManager.destroy();
//            bleManager.DisConnect();
        }
        System.exit(0);
        mLocClient.stop();
        unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            serialPortUtils.onKeyDataListener = MainActivity.this;
            String tmp = dataViewModel.getFileName();
            tmp = tmp.substring(0, tmp.lastIndexOf('.'));
            //textViewFile.setText(getResources().getText(R.string.file_name) + tmp);
            if (firstBoot) {
                firstBoot = false;
                if (!serialPortUtils.serialPortOk) {
                    Intent i = new Intent(MainActivity.this, BleActivity.class);
                    startActivityForResult(i, REQUESTCODE_BLE_CONNECT);
                }
            }


        } else {
            serialPortUtils.onKeyDataListener = null;
            //commTask.cancel(true);
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
            battTimer.schedule(timerTask, 100, 4000);
        }
    }

    public static boolean checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            activity.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName())), 0);
            return false;
        }
        return true;
    }

    @OnClick({R.id.layout_single_test, R.id.layout_delay_prj, R.id.layout_net, R.id.layout_authorize, R.id.layout_setting, R.id.layout_charge})
    public void onViewClicked(View view) {
        int viewId = view.getId();
        if (!checkPermission(this))
            return;
        //dataViewModel.battPercent=90;
        if (viewId != R.id.layout_authorize && viewId != R.id.layout_setting) {
            if (dataViewModel.battPercent < batteryLow) {
                InfoDialog battInfo = new InfoDialog();
                battInfo.setTitle("警告");
                battInfo.setMessage("电量低于30%，请充电！");
                battInfo.setCancelable(true);
                battInfo.show(getSupportFragmentManager(), "info");
                return;

            }

        }
        switch (view.getId()) {
            case R.id.layout_single_test:
                Intent intent = new Intent(MainActivity.this, SingleActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_delay_prj:
                intent = new Intent(MainActivity.this, DelayPrjActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_net:
                intent = new Intent(MainActivity.this, NetActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_charge:
                intent = new Intent(MainActivity.this, DetonateActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_authorize:
                intent = new Intent(MainActivity.this, AuthorizeActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_setting:
                intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);      //调用easypermission结果监听返回
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);  //easypermission 权限结果监听回调
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setMessage("请前往设置->应用->PermissionDemo->权限中打开相关权限，否则功能无法正常运行！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 1);
                    }
                }).show();
    }

    @Override
    public void ReceiveAck(byte[] data) {

    }

    @Override
    public void ReceiveNoAck(boolean sendStop) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setOpenFileName(String str) {
        dataViewModel.setFileName(str);
        dataViewModel.InitData(dataViewModel.fileName);
    }

    @Override
    public boolean onKeyPush(int key, int status) {
        boolean b = false;
        if (status == 1) {
            switch (key) {
                case KEYCODE_1:
                case KEYCODE_2:
                case KEYCODE_3:
                case KEYCODE_4:
                case KEYCODE_5:
                case KEYCODE_6:
                case KEYCODE_7:
                case KEYCODE_8:
                case KEYCODE_9:
                    b = true;
                    break;
            }
            if (b)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (key) {
                            case KEYCODE_1:
                                layoutSingleTest.performClick();
                                break;
                            case KEYCODE_2:
                                layoutDelayPrj.performClick();
                                break;
                            case KEYCODE_3:
                                layoutNet.performClick();
                                break;
                            case KEYCODE_4:
                                layoutCharge.performClick();
                                break;
                            case KEYCODE_5:
                                layoutAuthorize.performClick();
                                break;
                            case KEYCODE_6:
                                layoutSetting.performClick();
                                break;

                        }
                    }
                });
        }
        return b;
    }

    @Override
    public void onBarcodeScan(String barCode) {

    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return lcdWidth;
    }


    private class CommTask extends CommDetonator {

        public CommTask(Context context) {
            this.serialPortUtils = SerialPortUtils.getInstance(context);
            this.dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
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
                        textViewFile.setText("ID:" + dataViewModel.devId);
                        textViewFile.setVisibility(View.VISIBLE);
                        String str;
                        str = (String) SharedPreferencesUtils.getParam(MainActivity.this, "devId", "");
                        if (!str.equals(dataViewModel.devId))
                            SharedPreferencesUtils.setParam(MainActivity.this, "devId", dataViewModel.devId);
                    }
                    waitPublish = false;
                    break;
                case COMM_READ_DEV_VER:
                    if (values[1] == 1) {
                        byte[] b = new byte[12];
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
                        b[11] = (byte) ((values[4] >> 24) & 0x0ff);
                        dataViewModel.devVer = new String(b);
                        try {
                            InputStream is;
                            if (UpdateUtils.getVersionCode(MainActivity.this) > 100)
                                is = getAssets().open("code.bin");
                            else
                                is = getAssets().open("code2.bin");
                            int fileLen = ((is.available() + 7) / 8) * 8;
                            byte[] fileData = new byte[12];
                            if (fileLen > 0x600) {
                                is.skip(0x500);
                                is.read(fileData, 0, 12);
                                is.close();
                                String v = new String(fileData);
                                if (!v.equals(dataViewModel.devVer))
                                    dataViewModel.devVer = "";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        waitPublish = false;
                    }
                    break;
                case COMM_UPDATE:
                    if (values[1] == 1) {
                        if (values[3] == 0) {
                            runOnUiThread(new Runnable() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void run() {
                                    try {
                                        infoDialog.progressBar.setMax(fileLen);
                                        infoDialog.progressBar.setProgress(values[2]);
//                                        infoDialog.setMessageTxt(String.format("升级中（%d/%d)", values[2], fileLen));
                                        //    infoDialog.setMessageTxt("更新固件");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                        if (values[3] == 2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    infoDialog.setMessageTxt("升级完成！");
                                    infoDialog.setCancelable(true);
                                }
                            });
                        }
                    }
                    waitPublish = false;
                    break;
                case COMM_RESET:
                    if (values[1] == -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoDialog.setMessageTxt("升级失败！");
                                infoDialog.setCancelable(true);
                            }
                        });
                    }
                    break;

            }
            //super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch (integer) {
                case 0:
                case -1:
                    break;

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }
    }


    //    //设置字体为默认大小，不随系统字体大小改而改变
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        if (newConfig.fontScale != 1)//非默认值
//            getResources();
//        super.onConfigurationChanged(newConfig);
//    }
//
//
//    @Override
//    public Resources getResources() {
//        Resources res = super.getResources();
//        if (res.getConfiguration().fontScale != 1) {//非默认值
//            Configuration newConfig = new Configuration();
//            newConfig.setToDefaults();//设置默认
//            res.updateConfiguration(newConfig, res.getDisplayMetrics());
//        }
//        return res;
//    }
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    @Override
    public void onAttachedToWindow() {
//关键：在onAttachedToWindow中设置FLAG_HOMEKEY_DISPATCHED
        this.getWindow().addFlags(FLAG_HOMEKEY_DISPATCHED);
        super.onAttachedToWindow();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_DPAD_UP && event.getAction() == ACTION_DOWN) {
            if (f1 & f2)
                finish();
            return false;
        }

        return super.onKeyDown(keyCode, event);
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

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            title.setTextColor(Color.RED);
            switch (Objects.requireNonNull(action)) {
                case SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR:
                    // 开放鉴权错误信息描述
                    title.setText("key 验证出错! 错误码 :"
                            + intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                            + " ; 错误信息 ："
                            + intent.getStringExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_MESSAGE));
                    break;
                case SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK:
                    title.setText("key 验证成功! 功能可以正常使用");
                    title.setTextColor(Color.GREEN);
                    break;
                case SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR:
                    title.setText("网络出错");
                    break;
            }
        }
    }

    private void initUpdate() {
        XUpdate.get()
                .debug(true)
                .isWifiOnly(false)                                               //默认设置只在wifi下检查版本更新
                .isGet(true)                                                    //默认设置使用get请求检查版本
                .isAutoMode(false)                                              //默认设置非自动模式，可根据具体使用配置
                .param("versionCode", UpdateUtils.getVersionCode(this))  //设置默认公共请求参数
                .param("appKey", getPackageName())
                .setOnUpdateFailureListener(new OnUpdateFailureListener() { //设置版本更新出错的监听
                    @Override
                    public void onFailure(UpdateError error) {
                        error.printStackTrace();
                        if (error.getCode() == CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                            Toast.makeText(getApplicationContext(), "不需要更新！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .supportSilentInstall(false)                                     //设置是否支持静默安装，默认是true
                .setIUpdateHttpService(new OKHttpUpdateHttpService())           //这个必须设置！实现网络请求功能。
                .init(this.getApplication());                                          //这个必须初始化

    }

    private void initXHttp() {
        XHttpSDK.init(this.getApplication());   //初始化网络请求框架，必须首先执行
        XHttpSDK.debug("XHttp");  //需要调试的时候执行
        XHttp.getInstance().setTimeout(20000);
    }

    private void initOKHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    private void updateDialog() {
        if (updateEnable) {
            try {
                InputStream is;
                if (UpdateUtils.getVersionCode(MainActivity.this) > 100)
                    is = getAssets().open("code.bin");
                else
                    is = getAssets().open("code2.bin");
                //InputStream is = getAssets().open("code.bin");
                fileLen = ((is.available() + 7) / 8) * 8;
                fileData = new byte[fileLen];
                if (is.read(fileData) != 0) {
                    dataViewModel.devVer = "xxxx";
                    infoDialog = new InfoDialog();
                    infoDialog.setTitle("请勿关闭电");
                    infoDialog.setMessage("升级固件");
                    infoDialog.setProgressEnable(true);
                    infoDialog.setCancelable(false);
                    infoDialog.show(getSupportFragmentManager(), "info");
                    commTask.cancel(true);
                    commTask = new CommTask(MainActivity.this);
                    commTask.setFileData(fileData, fileLen);
                    commTask.execute(4, COMM_RESET, COMM_DELAY, COMM_DELAY, COMM_UPDATE, COMM_DELAY, COMM_DELAY, COMM_RESET);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}




