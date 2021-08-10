package com.example.xdyblaster.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.xdyblaster.DelayPrjActivity;
import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.SharedPreferencesUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pda.scan.BarcodeReceiver;
import cn.pda.scan.ScanUtil;
import utils.SerialPortUtils;

import static com.example.xdyblaster.MainActivity.actionKeyF3Push;
import static com.example.xdyblaster.MainActivity.actionKeyF3Release;
import static com.example.xdyblaster.util.AppConstants.ACTION_SCAN_INIT;

//import com.example.xdyblaster.util.AllCapTransformationMethod;

public class FragmentAuthInput extends DialogFragment {

    Unbinder unbinder;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btConfirm)
    Button btConfirm;
    @BindView(R.id.etUuid)
    EditText etUuid;
    @BindView(R.id.etCount)
    EditText etCount;
    @BindView(R.id.etEach)
    EditText etEach;
    @BindView(R.id.btScan)
    Button btScan;

    private SerialPortUtils serialPortUtils;
    private DataViewModel dataViewModel;
    public OnButtonClickListener onButtonClickListener = null;
    String uuid;
    int count, each;
    LocalBroadcastManager broadcastManager;
    ScanUtil scanUtil;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    MediaPlayer mMediaPlayer2 = new MediaPlayer();


    public BroadcastReceiver scanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
//                String barcode = Tools.Bytes2HexString(data, data.length);
                String barcode = new String(data);
                if (etUuid != null) {
                    if (barcode.length() >= 18) {
                        if (barcode.substring(0, 3).equals("I53")) {
                            String stringBuilder = "53" +
                                    barcode.substring(9,18) +
                                    "00";
                            etUuid.setText(stringBuilder);
                        }
                    } else
                        etUuid.setText(barcode);
                    try {
                        mMediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    };

    public BroadcastReceiver keyF3Push = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanUtil.scan();
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_input, container);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        assert bundle != null;
        uuid = bundle.getString("uuid");
        //etUuid.setTransformationMethod(new AllCapTransformationMethod());
        etUuid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //下面这种方法才是真正的将输入的小写字母转换为大写字母
                etUuid.removeTextChangedListener(this);
                etUuid.setText(s.toString().toUpperCase());
                //在输入完毕后定位到光标的末尾
                //mEdtCarNo.setSelection(s.length());
                /**在输入完毕后定位到当前修改的末尾 = start + count*/
                etUuid.setSelection(start + count);
                etUuid.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        each = (int) SharedPreferencesUtils.getParam(getActivity(), "each", 1);
        count = (int) SharedPreferencesUtils.getParam(getActivity(), "box", 1);

        etUuid.setText(uuid);
        etCount.setText(String.valueOf(count));
        etEach.setText(String.valueOf(each));

//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.rfid.SCAN");
//        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
//        broadcastManager.registerReceiver(scanReceiver, filter);
//        Intent intent = new Intent();
//        intent.setAction(ACTION_SCAN_INIT);
//        broadcastManager.sendBroadcast(intent);


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
        int h = dm.widthPixels / 7 * 6;
        params.width = h;//ViewGroup.LayoutParams.MATCH_PARENT;
//
//        params.height = h;// dm.widthPixels / 3;//ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //seekbar.getThumb().setColorFilter(Color.parseColor("#ec6a88"), PorterDuff.Mode.SRC_ATOP);

        params.alpha = 1.0f;//0.7f;f
        win.setAttributes(params);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        getActivity().registerReceiver(scanReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(actionKeyF3Push);
        getActivity().registerReceiver(keyF3Push, filter);

        Intent intent = new Intent();
        intent.setAction(ACTION_SCAN_INIT);
        getActivity().sendBroadcast(intent);
        scanUtil = new ScanUtil(requireActivity());
        //we must set mode to 0 : BroadcastReceiver mode
        scanUtil.setTimeout("2000");
        scanUtil.setScanMode(0);
        try {
            AssetFileDescriptor fd = getActivity().getAssets().openFd("9414.wav");
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AssetFileDescriptor fd = getActivity().getAssets().openFd("702.wav");
            mMediaPlayer2.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer2.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
//        broadcastManager.registerReceiver(scanReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scanUtil.stopScan();
        scanUtil.setScanMode(1);
        requireActivity().unregisterReceiver(scanReceiver);
        requireActivity().unregisterReceiver(keyF3Push);
        //broadcastManager.unregisterReceiver(scanReceiver);
        mMediaPlayer.release();
        mMediaPlayer2.release();
        unbinder.unbind();
    }


    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btExit, R.id.btConfirm, R.id.btScan})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btExit:
                dismissAllowingStateLoss();
                break;
            case R.id.btScan:
                scanUtil.scan();
                break;
            case R.id.btConfirm:
                if (etUuid.getText().toString().length() != 13) {
                    showError("编码长度不是13位！");
                    break;
                }
                uuid = etUuid.getText().toString();
                if (!FileFunc.checkUuidString(uuid)) {
                    showError("编码错误！");
                    break;
                }

                if (etCount.getText().toString().length() == 0) {
                    count = 1;
                } else
                    count = Integer.parseInt(etCount.getText().toString());
                if (etEach.getText().toString().length() == 0) {
                    each = 1;
                } else
                    each = Integer.parseInt(etEach.getText().toString());
                SharedPreferencesUtils.setParam(requireActivity(), "box", count);
                SharedPreferencesUtils.setParam(requireActivity(), "each", each);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //隐藏软键盘 //
                imm.hideSoftInputFromWindow(etUuid.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etCount.getWindowToken(), 0);
//                //显示软键盘
//                imm.showSoftInputFromInputMethod(tv.getWindowToken(), 0);
//                //切换软键盘的显示与隐藏
//                imm.toggleSoftInputFromWindow(tv.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);


                if (onButtonClickListener != null)
                    onButtonClickListener.onButtonClick(1, uuid, count, each);
                dismissAllowingStateLoss();
                break;
        }
    }

    private void showError(String str) {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getParentFragmentManager(), "info");
    }


    public interface OnButtonClickListener {
        void onButtonClick(int index, String uuid, int count, int each);
    }
}
