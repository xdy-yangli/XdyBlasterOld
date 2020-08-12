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
import com.wang.avi.AVLoadingIndicatorView;

import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.DEV_IDLE;
import static com.example.xdyblaster.util.AppConstants._MODBUS_WRITE;


public class FragmentConnect extends DialogFragment {
    private Handler handler;
    public ConnectComplete connectComplete;
    public BluetoothDevice device;
    private TextView textView;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private boolean stopConnect, status;
    ImageView imageViewFail, imageViewSuccess;
    SerialPortUtils serialPortUtils;

    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container);
        //getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        textView = (TextView) view.findViewById(R.id.textView_connect_Status);
        avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.avi);
        imageViewFail = (ImageView) view.findViewById(R.id.imageView_fail);
        imageViewSuccess = (ImageView) view.findViewById(R.id.imageView_success);
        imageViewFail.setVisibility(View.GONE);
        imageViewSuccess.setVisibility(View.GONE);
        handler = new Handler();
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        avLoadingIndicatorView.hide();
                        imageViewSuccess.setVisibility(View.VISIBLE);
                        textView.setText("连接失败");
                        status = false;
                    }
                });
                exitConnect();
            }

            @Override
            public void ReceiveAck(byte[] data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        avLoadingIndicatorView.hide();
                        imageViewSuccess.setVisibility(View.VISIBLE);
                        textView.setText("连接成功");
                        status = true;
                    }
                });
                exitConnect();
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DelayMs(2000);
                serialPortUtils.SsendCommand(DEV_IDLE, _MODBUS_WRITE, (byte) 1);
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
//        params.width = h;//ViewGroup.LayoutParams.MATCH_PARENT;
//
//        params.height = h;// dm.widthPixels / 3;//ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //seekbar.getThumb().setColorFilter(Color.parseColor("#ec6a88"), PorterDuff.Mode.SRC_ATOP);

        params.alpha = 0.8f;//0.7f;f
        win.setAttributes(params);
    }

    public void exitConnect() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connectComplete != null)
                    connectComplete.ConnectFinish(status);
                dismissAllowingStateLoss();
            }
        }, 1000);
    }


    public interface ConnectComplete {
        void ConnectFinish(boolean status);

    }

    public void DelayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
