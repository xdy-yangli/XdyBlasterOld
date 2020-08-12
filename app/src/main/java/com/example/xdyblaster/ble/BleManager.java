package com.example.xdyblaster.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BleManager implements BleService.BleServiceListener {

    public String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private volatile static BleManager mInstance = null;
    private Context mContext;
    private Activity mActivity;
    public BleService bleService = null;
    private long scanTime = 0;
    public boolean mBound = false;
    public boolean connect = false;
    public boolean newConnect = false;
    public boolean monitorEnable = false;
    public BluetoothDevice device;
    public int sendMore = 0;
    public byte[] sendData;
    public boolean stopScan = false, scanning = false;


    List<ScanFilter> bleScanFilters = new ArrayList<>();


    public OnBleListener onBleListener = null;
    private Handler handler = new Handler();
    ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build();
    //    private Runnable DelayForStopScan = new Runnable() {
//        @Override
//        public void run() {
//
//
//            bleService.bluetoothLeScanner.stopScan(scanCallback);
//            if (onBleListener != null)
//                onBleListener.OnBleStopScan();
//        }
//    };
    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BleService.LocalBinder binder = (BleService.LocalBinder) service;
            bleService = binder.getService();
            Log.e("ble", "success");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    private BleManager(Context context) {
        mContext = context;
        mActivity = (Activity) context;
        connectService();

//        ScanFilter.Builder builder = new ScanFilter.Builder();
//        builder.setServiceUuid(ParcelUuid.fromString(service_UUID));
//        ScanFilter scanFilter = builder.build();
//        bleScanFilters.add(scanFilter);
    }

    public void connectService() {
        Intent intent = new Intent(mContext, BleService.class);
        //mActivity.startService(intent);
        mActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        MyApplication.getInstance().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public static BleManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (BleManager.class) {
                if (mInstance == null) {
                    mInstance = new BleManager(context);
                }
            }
        }
        return mInstance;
    }

    public void DisConnect() {
        if (bleService.state == BluetoothProfile.STATE_CONNECTED) {
            if (bleService.bluetoothGatt != null) {
                bleService.bluetoothGatt.disconnect();
                bleService.state = BluetoothProfile.STATE_DISCONNECTING;
            }
        }
    }

    public void setScanner() {
        bleService.bluetoothLeScanner = bleService.bluetoothAdapter.getBluetoothLeScanner();
    }

    public void StartScan() {
        stopScan = true;
        if (bleService == null)
            connectService();
        if (bleService.bluetoothLeScanner == null)
            bleService.bluetoothLeScanner = bleService.bluetoothAdapter.getBluetoothLeScanner();
        if (bleService.bluetoothLeScanner == null)
            return;
        bleService.bluetoothLeScanner.stopScan(scanCallback);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    Method localMethod = bleService.bluetoothGatt.getClass().getMethod("refresh");
//                    if (localMethod != null) {
//                        localMethod.invoke(bleService.bluetoothGatt);
//                    }
//                } catch (Exception localException) {
//                    Log.e("refreshServices()", "An exception occured while refreshing device");
//                }
                if (bleService.state == BluetoothProfile.STATE_CONNECTED) {
                    if (bleService.bluetoothGatt != null) {
                        bleService.bluetoothGatt.disconnect();
                        bleService.state = BluetoothProfile.STATE_DISCONNECTING;
                    }
                }
//                handler.removeCallbacks(DelayForStopScan);
                while (scanning) {
                    DelayMs(1);
                }
                stopScan = false;
                scanning = true;
                long time = System.currentTimeMillis();
                while (time - scanTime < 7000) {
                    DelayMs(1);
                    time = System.currentTimeMillis();
                    if (stopScan == true) {
                        scanning = false;
                        return;
                    }
                }
                scanTime = System.currentTimeMillis();
                time = scanTime;
                bleService.bluetoothLeScanner.startScan(bleScanFilters, settings, scanCallback);
                while ((time - scanTime) < 10000) {
                    DelayMs(10);
                    time = System.currentTimeMillis();
                    if (stopScan)
                        break;
                }
                StopScan();
                scanning = false;
                if (onBleListener != null)
                    onBleListener.OnBleStopScan();

            }
        });
        thread.start();
    }

    public void StopScan() {
//        handler.removeCallbacks(DelayForStopScan);
        stopScan = true;
        if (bleService.bluetoothLeScanner != null)
            bleService.bluetoothLeScanner.stopScan(scanCallback);

    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult results) {
            super.onScanResult(callbackType, results);// callbackType：触发类型 ，result：包括4.3版本的蓝牙信息，信号强度rssi，和广播数据scanRecord
            BluetoothDevice device = results.getDevice();
            if (onBleListener != null)
                onBleListener.OnBleScanDevice(device, results.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void ConnectBle(BluetoothDevice device) {
        bleService.bleServiceListener = this;
        StopScan();
        stopScan = true;
//        handler.removeCallbacks(DelayForStopScan);
        this.device = device;
        if (bleService.state == BluetoothProfile.STATE_DISCONNECTED) {
            bleService.bluetoothDevice = device;
            //bleService.bluetoothDevice.getUuids();
            //Log.d(TAG, "Device service UUIDs: " + bleService.bluetoothDevice.getUuids());
//            if(bleService.bluetoothGatt!=null)
//                bleService.bluetoothGatt.close();
            bleService.bluetoothGatt = bleService.bluetoothDevice.connectGatt(mActivity, false, bleService.BluetoothGattCallback);
            bleService.state = BluetoothProfile.STATE_CONNECTING;
        } else {
            if (bleService.state == BluetoothProfile.STATE_CONNECTED) {
                if (bleService.bluetoothDevice == device) {
                    if (onBleListener != null)
                        onBleListener.OnBleConnected(device.getName(), 1);
                    return;
                }
                bleService.state = BluetoothProfile.STATE_DISCONNECTING;
                bleService.bluetoothGatt.disconnect();
                newConnect = true;
            }
        }
    }

    public void SendData(byte[] data) {
        if (data.length < 21) {
            sendMore = 0;
            bleService.SendData(data);
        } else {
            sendData = data;
            byte[] bytes = new byte[20];
            sendMore = 20;
            for (int i = 0; i < 20; i++)
                bytes[i] = sendData[i];
            bleService.SendData(bytes);
        }
    }

    @Override
    public void ConnectSuccess(String name) {
        if (onBleListener != null)
            onBleListener.OnBleConnected(name, 1);
        connect = true;
    }

    @Override
    public void ConnectLost() {
        if (newConnect) {
            bleService.bluetoothDevice = device;
            bleService.bluetoothGatt = bleService.bluetoothDevice.connectGatt(mActivity, false, bleService.BluetoothGattCallback);
            bleService.state = BluetoothProfile.STATE_CONNECTING;
            newConnect = false;
        } else {
            onBleListener.OnBleConnected("", 0);
        }

    }

    @Override
    public void SendSuccess(int status) {
        if (sendMore != 0) {
            int j = 0;
            byte[] bytes;
            if (sendData.length - sendMore < 20) {
                bytes = new byte[sendData.length - sendMore];
                for (int i = sendMore; i < sendData.length; i++)
                    bytes[j++] = sendData[i];
                sendMore = 0;
                bleService.SendData(bytes);
            } else {
                bytes = new byte[20];
                for (int i = sendMore; i < sendMore + 20; i++)
                    bytes[j++] = sendData[i];
                sendMore += 20;
                bleService.SendData(bytes);

            }

        }

    }

    @Override
    public void ReceiveData(byte[] data) {
        if (onBleListener != null)
            onBleListener.OnBleReceiveData(data);

    }

    public void destroy() {
        if (bleService == null) {
            return;
        }
        //断开蓝牙,释放资源
        stopScan = true;
        bleService.destroy();
        //取消绑定服务
        if (mConnection != null)
            mActivity.unbindService(mConnection);
        bleService = null;
    }


    public interface OnBleListener {
        void OnBleScanDevice(BluetoothDevice device, int rssi);

        void OnBleStopScan();

        void OnBleConnected(String name, int status);

        void OnBleReceiveData(byte[] data);
    }

    public void DelayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.e("DELAY", "error");
        }
    }
}
