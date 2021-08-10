package com.example.xdyblaster.fragment;

import android.content.Context;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;
import com.example.xdyblaster.util.FileFunc;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import utils.SerialPortUtils;

public class FragmentLoad extends DialogFragment {
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.imageView_fail)
    ImageView imageViewFail;
    @BindView(R.id.imageView_success)
    ImageView imageViewSuccess;
    @BindView(R.id.textView_connect_Status)
    TextView textViewConnectStatus;
    Unbinder unbinder;
    boolean status;

    SerialPortUtils serialPortUtils;
    Handler handler;
    DataViewModel dataViewModel;
    public boolean loadFile = true;

    public interface OnExitListener {
        public void OnExit();
    }

    public  OnExitListener onExitListener = null;

    @Override


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container);
        //getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        unbinder = ButterKnife.bind(this, view);
        avi.show();
        handler = new Handler();
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        if (loadFile) {
            textViewConnectStatus.setText("载入数据");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!dataViewModel.fileLoaded) {
                        FileFunc.loadDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                        dataViewModel.fileLoaded = true;
                    }
                    loadDetonatorData(dataViewModel);
                    for (int i = 0; i < dataViewModel.detonatorList.size(); i++) {
                        dataViewModel.updateList.get(i).postValue(-1);
                        dataViewModel.ok = false;
                        while (!dataViewModel.ok) {
                            dataViewModel.ok = true;
                            DelayMs(100);
                        }
                    }
                    dataViewModel.totalCount.postValue(dataViewModel.detonatorDatas.size());
                    try {
                        dismissAllowingStateLoss();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            textViewConnectStatus.setText("保存数据");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
                    dataViewModel.dataShift=true;
                    if (onExitListener != null)
                        onExitListener.OnExit();
                    dismissAllowingStateLoss();
                }
            });
            thread.start();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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

    public void DelayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDetonatorData(DataViewModel dataViewModel) {
        int r = 0;
        for (int i = 0; i < dataViewModel.detonatorList.size(); i++)
            dataViewModel.detonatorList.get(i).clear();
        for (DetonatorData d : dataViewModel.detonatorDatas) {
//            if (d.getRowNum() != r) {
//                r = d.getRowNum();
//            }
            dataViewModel.detonatorList.get(r).add(d);
        }
    }

}
