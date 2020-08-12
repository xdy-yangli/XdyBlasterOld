package com.example.xdyblaster.system;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xdyblaster.R;
import com.example.xdyblaster.ble.BleAdapter;
import com.example.xdyblaster.ble.BleData;
import com.example.xdyblaster.ble.BleManager;
import com.example.xdyblaster.fragment.FragmentBleConnect;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import pub.devrel.easypermissions.EasyPermissions;
import utils.SerialPortUtils;

import static com.example.xdyblaster.MainActivity.action;


public class BleActivity extends AppCompatActivity implements CustomAdapt, EasyPermissions.PermissionCallbacks, BleManager.OnBleListener, BleAdapter.OnItemClickListener, FragmentBleConnect.ConnectComplete {
    public static final int REQUEST_CODE_CAMERA = 0x0000c0de;
    public final static String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @BindView(R.id.tv_connect)
    TextView tvConnect;
    @BindView(R.id.tool_switch)
    Switch toolSwitch;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.tv_device)
    TextView tvDevice;
    @BindView(R.id.tv_device_status)
    TextView tvDeviceStatus;
    @BindView(R.id.tv_scanning)
    TextView tvScanning;
    @BindView(R.id.rv_ble)
    RecyclerView rvBle;
    public BleManager bleManager;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.divider2)
    View divider2;
    @BindView(R.id.layout_device)
    RelativeLayout layoutDevice;
    @BindView(R.id.ivSkip)
    ImageView ivSkip;
    private BleAdapter bleAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<BleData> bleDatas;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private String bleName, bleFullName;
    private int bleScan;
    public Handler handler;
    long lastClick = 0;
    boolean isAlive;
    String openFileName;
    public boolean boot, canConnect;

    public int deviceMode, dcdcVersion = 0, plcVersion = 0, amdVersion = 0;
    public String receiveString;
    private SerialPortUtils serialPortUtils;
    Intent result = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openFileName = "";
        boot = true;
        canConnect = false;
        deviceMode = 3;
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_ble);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);

        if (EasyPermissions.hasPermissions(BleActivity.this, perms)) {  //已经获取所要申请的权限，进行下一步处理
            bleManager = BleManager.getInstance(BleActivity.this);
        } else {                    //没有获取，申请定位权限
            EasyPermissions.requestPermissions(BleActivity.this, "请求获得设备权限", 1, perms);
        }
        toolSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!CheckBle()) {
                    toolSwitch.setChecked(false);
                    return;
                }
                if (isChecked) {
                    bleScan = 0;
                    avi.show();
                    devices.clear();
                    bleDatas.clear();
                    bleAdapter.notifyDataSetChanged();
                    bleManager.onBleListener = BleActivity.this;
                    bleManager.StartScan();
                    HideDeviceStatus();
                    ShowDeviceScan();
                } else {
                    avi.hide();
                    bleManager.StopScan();
                    HideDeviceScan();
                    HideDeviceStatus();
                    bleManager.DisConnect();
                }
            }
        });

        handler = new Handler();
        bleDatas = new ArrayList<>();
        bleAdapter = new BleAdapter(this, bleDatas);

        linearLayoutManager = new LinearLayoutManager(this);
        rvBle.setLayoutManager(linearLayoutManager);
        rvBle.setAdapter(bleAdapter);
        bleAdapter.setOnItemClickListener(this);

        rvBle.setVisibility(View.GONE);
        tvScanning.setVisibility(View.GONE);

        HideDeviceStatus();

        HideDeviceScan();

        isAlive = true;

        setResult(RESULT_CANCELED, result);

    }

    public boolean CheckBle() {
        if (!EasyPermissions.hasPermissions(BleActivity.this, perms)) {  //已经获取所要申请的权限，进行下一步处理
            EasyPermissions.requestPermissions(BleActivity.this, "请求获得设备权限", 1, perms);
            return false;
        }
        if (bleManager == null) {
            bleManager = BleManager.getInstance(BleActivity.this);
            if (bleManager == null)
                return false;
        }
        if (bleManager.bleService == null) {
            bleManager.connectService();
            if (bleManager.bleService == null)
                return false;
        }
        if (!bleManager.bleService.bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(bleManager.bleService.bluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            return false;
        }
        if (bleManager.bleService.bluetoothLeScanner == null) {
            bleManager.setScanner();
            if (bleManager.bleService.bluetoothLeScanner == null)
                return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);      //调用easypermission结果监听返回
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);  //easypermission 权限结果监听回调
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        CheckBle();
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
    protected void onResume() {
        super.onResume();
        if (bleManager != null)
            bleManager.onBleListener = this;
        isAlive = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    @Override
//    public Resources getResources() {
//        Resources resources = super.getResources();
//        Configuration configuration = new Configuration();
//        configuration.setToDefaults();
//        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
//        return resources;
//    }

    @OnClick({R.id.layout_device, R.id.ivSkip})
    public void onViewClicked(View view) {
        if (bleManager == null) {
            if (EasyPermissions.hasPermissions(BleActivity.this, perms)) {  //已经获取所要申请的权限，进行下一步处理
                bleManager = BleManager.getInstance(BleActivity.this);
            } else {                    //没有获取，申请定位权限
                EasyPermissions.requestPermissions(BleActivity.this, "请求获得设备权限", 1, perms);
            }
        }
        if (bleManager == null)
            return;
        if (!bleManager.bleService.bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(bleManager.bleService.bluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            return;
        }
        // 弹对话框的形式提示用户开启蓝牙
        switch (view.getId()) {
            case R.id.ivSkip:
                deviceMode = 3;
            case R.id.layout_device:
                StartMonitor();
                break;

        }
    }


    @Override
    public void OnBleScanDevice(BluetoothDevice device, int rssi) {
        if (bleScan == 0) {
            if (!devices.contains(device)) {  //判断是否已经添加
                devices.add(device);
                BleData bleData = new BleData(device.getName(), device.getAddress(), String.valueOf(rssi));
                bleDatas.add(bleData);
                bleAdapter.notifyItemInserted(bleDatas.size());
            }
        } else {
            String name = device.getName();
            if ((name == null))
                return;
            if (name.length() != 12)
                return;
            name = name.substring(2);
            if (bleName.equals(name)) {
                bleScan = 0;
                Intent intent = new Intent(action);
                intent.putExtra("ok", 1);
                intent.putExtra("name", "扫描蓝牙成功");
                sendBroadcast(intent);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        bleManager.StopScan();
                        FragmentBleConnect dialog = new FragmentBleConnect();
                        dialog.device = device;
                        dialog.bleManager = bleManager;
                        dialog.connectComplete = BleActivity.this;
                        dialog.setCancelable(false);
                        dialog.show(getSupportFragmentManager(), "ConnectDialog");
                    }
                });

            }
        }
    }

    @Override
    public void OnBleStopScan() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (bleScan == 0)
                    avi.hide();
                else {
                    Intent intent = new Intent(action);
                    intent.putExtra("ok", 3);
                    intent.putExtra("name", "扫描蓝牙失败");
                    sendBroadcast(intent);
                }

            }
        });

    }

    @Override
    public void OnBleConnected(String name, int status) {


    }

    @Override
    public void OnBleReceiveData(byte[] data) {

    }

    @Override
    public void onItemClick(View v, int position) {
        if (System.currentTimeMillis() - lastClick <= 2000) {
            return;
        }
        lastClick = System.currentTimeMillis();
        avi.hide();
        HideDeviceStatus();
        bleManager.StopScan();
        FragmentBleConnect dialog = new FragmentBleConnect();
        dialog.device = devices.get(position);
        dialog.bleManager = bleManager;
        dialog.connectComplete = this;
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "ConnectDialog");

    }

    @Override
    public void onItemLongClick(View v) {

    }

    @Override
    public void ConnectFinish(int status, String name) {
        if (status == 1) {
            bleFullName = name;
            canConnect = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowDeviceStatus();
                    serialPortUtils.bleManager = BleManager.getInstance(BleActivity.this);
                    serialPortUtils.bleOk = true;
                    Intent result = new Intent();
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
        }
    }

    public void DelayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.d("AAA", e.toString());
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


    private void HideDeviceStatus() {
        tvDevice.setVisibility(View.GONE);
        tvDeviceStatus.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
    }

    private void ShowDeviceStatus() {
        tvDevice.setText(bleFullName);
        tvDeviceStatus.setText("已连接");
        tvDevice.setVisibility(View.VISIBLE);
        tvDeviceStatus.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
    }

    private void HideDeviceScan() {
        avi.hide();
        rvBle.setVisibility(View.GONE);
        tvScanning.setVisibility(View.GONE);
        divider2.setVisibility(View.GONE);
    }

    private void ShowDeviceScan() {
        rvBle.setVisibility(View.VISIBLE);
        tvScanning.setVisibility(View.VISIBLE);
        divider2.setVisibility(View.VISIBLE);
    }

    private void StartMonitor() {
    }


    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 400;
    }
}