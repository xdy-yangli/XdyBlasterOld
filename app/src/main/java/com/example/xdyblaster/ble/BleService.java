package com.example.xdyblaster.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BleService extends Service {
    public String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    String characteristic_UUID1 = "0000ffe1-0000-1000-8000-00805f9b34fb";
    String characteristic_UUID2 = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private final IBinder mBinder = new LocalBinder();
    public int state = BluetoothProfile.STATE_DISCONNECTED;
    public String macAddress = "00:00:00:00:00";
    public BleServiceListener bleServiceListener;
    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;
    public BluetoothLeScanner bluetoothLeScanner;
    public BluetoothDevice bluetoothDevice = null;
    public BluetoothGatt bluetoothGatt;
    public BluetoothGattService bluetoothGattService;
    public BluetoothGattCharacteristic bleGattReceive, bleGattSend;


    public android.bluetooth.BluetoothGattCallback BluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {//状态变已连接
                Log.e(TAG, "连接成功");
                if (state != newState) {
                    macAddress = gatt.getDevice().getAddress();
                    gatt.discoverServices();//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                }
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) { //状态变为未连接
                Log.e(TAG, "连接断开");
                if (bluetoothGatt != null)
                    bluetoothGatt.close();
                state = newState;
                if (bleServiceListener != null)
                    bleServiceListener.ConnectLost();
                return;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == gatt.GATT_SUCCESS) { // 发现服务成功
                bluetoothGattService = gatt.getService(UUID.fromString(service_UUID));
                if (bluetoothGattService == null) {
                    Log.e(TAG, "获取服务失败");
                    if (gatt != null) gatt.disconnect();
                    return;
                }
                bleGattReceive = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID1));
                bleGattSend = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID2));
                if (bleGattReceive != null) {
                    Log.e(TAG, "成功获取特征");
                    state = BluetoothProfile.STATE_CONNECTED;
                    if (bleServiceListener != null)
                        bleServiceListener.ConnectSuccess(gatt.getDevice().getName());
                    //gatt.setCharacteristicNotification(bluetoothGattCharacteristic1, true);
                    //gatt.setCharacteristicNotification(bluetoothGattCharacteristic2, true);
                    boolean isEnableNotification = gatt.setCharacteristicNotification
                            (bleGattReceive, true);
                    if (isEnableNotification) {
                        List<BluetoothGattDescriptor> descriptorList =
                                bleGattReceive.getDescriptors();
                        if (descriptorList != null && descriptorList.size() > 0) {
                            for (BluetoothGattDescriptor descriptor : descriptorList) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "获取特征失败");
                    if (gatt != null) gatt.disconnect();
                }
            } else {
                Log.e(TAG, "发现服务失败");
                if (gatt != null) gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            int i;
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] receive = characteristic.getValue();
            if (bleServiceListener != null)
                bleServiceListener.ReceiveData(receive);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            if (bleServiceListener != null)
                bleServiceListener.SendSuccess(status);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("BLE", "START");
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
//        if (!bluetoothAdapter.isEnabled()) {
//            context.startActivityForResult(new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE), 0)
//            ;  // 弹对话框的形式提示用户开启蓝牙
//        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }


    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Kathy", "onStartCommand - startId = " + startId + ", Thread ID = " + Thread.currentThread().getId());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BleService getService() {
            return BleService.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void SendData(byte[] sendData) {
        int i;
        if (bleGattSend != null) {
            bleGattSend.setValue(sendData);
            bluetoothGatt.writeCharacteristic(bleGattSend);
        }
    }


    public interface BleServiceListener {
        void ConnectSuccess(String name);

        void ConnectLost();

        void SendSuccess(int status);

        void ReceiveData(byte[] data);
    }

    public void destroy() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

}
