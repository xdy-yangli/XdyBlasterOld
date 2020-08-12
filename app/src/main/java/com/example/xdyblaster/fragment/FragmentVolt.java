package com.example.xdyblaster.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anlia.progressbar.CircleBarView;
import com.anlia.utils.LinearGradientUtil;
import com.example.xdyblaster.R;
import com.example.xdyblaster.util.BatteryView;
import com.example.xdyblaster.util.DataViewModel;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import utils.SerialPortUtils;

public class FragmentVolt extends Fragment {
    SerialPortUtils serialPortUtils;

    Unbinder unbinder;
    //    @BindView(R.id.textView8)
//    TextView textView8;
    @BindView(R.id.horizontalBattery)
    public BatteryView horizontalBattery;
    //    @BindView(R.id.divider3)
//    View divider3;
    @BindView(R.id.pie_voltage)
    CircleBarView pieVoltage;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.tv_voltage)
    public TextView tvVoltage;
    @BindView(R.id.pie_current)
    CircleBarView pieCurrent;
    @BindView(R.id.textView6)
    TextView textView6;
    @BindView(R.id.tv_current)
    TextView tvCurrent;
    @BindView(R.id.lt_volt_current)
    LinearLayout ltVoltCurrent;
    @BindView(R.id.tvBatteryPercent)
    public TextView tvBatteryPercent;
    @BindView(R.id.tvVolt)
    public TextView tvVolt;
    @BindView(R.id.tvCurr)
    public TextView tvCurr;
    @BindView(R.id.ivLocation)
    ImageView ivLocation;
    private DataViewModel dataViewModel;
    public int showCurrentAni = 0, showVoltAni = 0, showBattAni = 0;
    public boolean isPowerOn = false;
    public boolean locationOnOff;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_volt, container, false);
        unbinder = ButterKnife.bind(this, root);
        pieVoltage.setMaxNum(32);
        pieVoltage.setTextView(tvVoltage);
        pieCurrent.setMaxNum(50);
        pieCurrent.setTextView(tvCurrent);
        ltVoltCurrent.setVisibility(View.GONE);
        if (serialPortUtils.newLatlng) {
            ivLocation.setColorFilter(ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), R.color.green));
            ivLocation.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_location_on_white_48dp));
            locationOnOff = true;
        } else {
            ivLocation.setColorFilter(ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), R.color.red));
            ivLocation.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_location_off_white_48dp));
            locationOnOff = false;

        }
        pieVoltage.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                return String.valueOf(updateNum) + "V";
            }

            @Override
            public void howTiChangeProgressColor(Paint paint, float interpolatedTime, float updateNum, float maxNum) {

            }
        });
        pieCurrent.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                return String.valueOf(updateNum) + "mA";
            }

            @Override
            public void howTiChangeProgressColor(Paint paint, float interpolatedTime, float progressNum, float maxNum) {
                LinearGradientUtil linearGradientUtil = new LinearGradientUtil(Color.GREEN, Color.RED);
                paint.setColor(linearGradientUtil.getColor(progressNum / maxNum * interpolatedTime));
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void setPieVoltage(Float aFloat, int time) {
        if (!pieVoltage.isShow)
            pieVoltage.setProgressNum(aFloat, time);

    }

    public void setLocationIcon() {
        if (serialPortUtils.newLatlng) {
            if (!locationOnOff) {
                ivLocation.setColorFilter(ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), R.color.green));
                ivLocation.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_location_on_white_48dp));
                locationOnOff = true;
            }
        } else {
            if (locationOnOff) {
                ivLocation.setColorFilter(ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), R.color.red));
                ivLocation.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_location_off_white_48dp));
                locationOnOff = false;
            }

        }

    }

//    public void setVoltCurrent(boolean b) {
//        if (b) {
//            ltVoltCurrent.setVisibility(View.VISIBLE);
//        } else {
//            ltVoltCurrent.setVisibility(View.GONE);
//        }
//    }

//    public void setPowerObserve(DataViewModel dataViewModel)
//    {
//        dataViewModel.batt.observe(this, new Observer<Float>() {
//            @Override
//            public void onChanged(@Nullable Float aFloat) {
//                int battery;
//                String str;
//                if (aFloat >= 500f) {
//                    tvBatteryPercent.setText("100%");
//                    horizontalBattery.setPower(100);
//                } else if (aFloat > 200f) {
//                    tvBatteryPercent.setText("充电");
//                    battery = Math.round(aFloat - 200f);
//                    battery-=5;
//                    if(battery<0)
//                        battery=0;
//                    if (battery >= 100)
//                        battery = 99;
//                    horizontalBattery.setPower(battery);
//                } else {
//                    battery = Math.round(aFloat);
//                    str = String.format("%d%%", battery);
//                    horizontalBattery.setPower(battery);
//                    tvBatteryPercent.setText(str);
//                }
//            }
//        });
//        dataViewModel.volt.observe(this, new Observer<Float>() {
//            @Override
//            public void onChanged(@Nullable Float aFloat) {
//                if (!pieVoltage.isShow)
//                    pieVoltage.setProgressNum(aFloat, showVoltAni);
//                tvVolt.setText(String.format("电压 %.1fv",aFloat));
//
////                if ((aFloat != 0) && (!isPowerOn))
////                    setPowerOnIcon();
////                if ((aFloat == 0) && (isPowerOn))
////                    setPowerOffIcon();
////
//
//                showVoltAni = 0;
//            }
//        });
//        dataViewModel.current.observe(this, new Observer<Float>() {
//            @Override
//            public void onChanged(@Nullable Float aFloat) {
//                if (!pieCurrent.isShow)
//                    pieCurrent.setProgressNum(aFloat, showCurrentAni);
//                tvCurr.setText(String.format("电流 %.1fma",aFloat));
//                showCurrentAni = 0;
//            }
//        });
//
//        dataViewModel.overCurrent.observe(this, new Observer<Integer>() {
//            @Override
//            public void onChanged(@Nullable Integer integer) {
//                if (integer == 1) {
////                    btPower.setChecked(false);
//                    serialPortUtils.uartData.volt = 0;
//                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                            .setCancelable(false)
//                            .setTitle("警告！")
//                            .setMessage("设备过流保护，请检查线路是否短路！")
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            }).show();
//                }
//
//            }
//        });
//        dataViewModel.romErr.observe(this, new Observer<Integer>() {
//            @Override
//            public void onChanged(@Nullable Integer integer) {
//                if (integer == 1) {
////                    btPower.setChecked(false);
//                    serialPortUtils.uartData.volt = 0;
//                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                            .setCancelable(false)
//                            .setTitle("警告！")
//                            .setMessage("设备ROM错误，请升级！")
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            }).show();
//                }
//            }
//        });
//
//
//    }

}
