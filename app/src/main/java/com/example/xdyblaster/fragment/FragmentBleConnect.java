package com.example.xdyblaster.fragment;


import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.xdyblaster.R;
import com.example.xdyblaster.ble.BleManager;
import com.wang.avi.AVLoadingIndicatorView;


public class FragmentBleConnect extends DialogFragment implements BleManager.OnBleListener {
    private Handler handler;
    public ConnectComplete connectComplete;
    public BluetoothDevice device;
    public BleManager bleManager;
    private TextView textView;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private boolean stopConnect;
    ImageView imageViewFail, imageViewSuccess;

    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_connect, container);
        //getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        textView = (TextView) view.findViewById(R.id.textView_connect_Status);
        avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.avi);
        imageViewFail = (ImageView) view.findViewById(R.id.imageView_fail);
        imageViewSuccess = (ImageView) view.findViewById(R.id.imageView_success);
        imageViewFail.setVisibility(View.GONE);
        imageViewSuccess.setVisibility(View.GONE);
        handler = new Handler();
        bleManager.onBleListener = this;
        bleManager.ConnectBle(device);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis() + 32000;
                stopConnect = false;
                while (!stopConnect) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (time < System.currentTimeMillis())
                        break;
                }
                if (!stopConnect)
                    OnBleConnected("", 0);
            }
        });
        thread.start();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //去除半透明阴影
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        layoutParams.dimAmount = 0.4f;
        win.setAttributes(layoutParams);


        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.CENTER;
//        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        int h = dm.widthPixels / 5 * 2;
        params.width = h;//ViewGroup.LayoutParams.MATCH_PARENT;

        params.height = h;// dm.widthPixels / 3;//ViewGroup.LayoutParams.WRAP_CONTENT;
//          params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//          params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //seekbar.getThumb().setColorFilter(Color.parseColor("#ec6a88"), PorterDuff.Mode.SRC_ATOP);

        params.alpha = 0.8f;//0.7f;f
        win.setAttributes(params);
    }


    @Override
    public void OnBleScanDevice(BluetoothDevice device, int rssi) {
    }

    @Override
    public void OnBleStopScan() {
    }

    @Override
    public void OnBleConnected(String name, int status) {
        stopConnect=true;
        if (status == 1) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    avLoadingIndicatorView.hide();
                    imageViewSuccess.setVisibility(View.VISIBLE);
                    textView.setText("连接成功");
//                    textView.setTextColor(0xff00ff00);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    avLoadingIndicatorView.hide();
                    imageViewFail.setVisibility(View.VISIBLE);
                    textView.setText("连接失败");
//                    textView.setTextColor(0xffff0000);
                }
            });
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connectComplete != null)
                    connectComplete.ConnectFinish(status, device.getName());
                dismissAllowingStateLoss();
            }
        }, 1000);
    }

    @Override
    public void OnBleReceiveData(byte[] data) {

    }


    public interface ConnectComplete {
        void ConnectFinish(int status, String name);

    }
}
